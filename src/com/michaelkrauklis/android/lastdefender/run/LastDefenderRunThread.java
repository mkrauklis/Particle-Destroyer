package com.michaelkrauklis.android.lastdefender.run;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Paint.Align;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Vibrator;
import android.text.TextPaint;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;

import com.michaelkrauklis.android.lastdefender.LastDefender;
import com.michaelkrauklis.android.lastdefender.R;
import com.michaelkrauklis.android.lastdefender.model.Bomb;
import com.michaelkrauklis.android.lastdefender.model.Missile;
import com.michaelkrauklis.android.lastdefender.model.MissileSilo;
import com.michaelkrauklis.android.lastdefender.model.Nuke;
import com.michaelkrauklis.android.lastdefender.preferences.Preferences;
import com.michaelkrauklis.android.thread.RunThread;

public class LastDefenderRunThread extends RunThread {
	private static Drawable myImage;
	
	private Set<Missile> incomingMissiles;
	private Set<Missile> outgoingMissiles;
	private List<MissileSilo> silos;

	private long lastTimeElapsedSinceLastUpdate;
	private float lastFPS;
	private int level = 1;
	private long score = 0;
	private long missileFiringLatency = 5000;

	private long shotsFired = 0;
	private long missilesDestroyed = 0;

	private int missilesToCreate;
	private long msRemainderSinceLastUpdate;
	private long msMissileLatency = 1000;

	private long timeToShowMessage = 3000;
	private long timeShowingMessage = 0;
	private String message;

	private float difficultyModifier = 1;

	public LastDefenderRunThread(SurfaceHolder surfaceHolder, Context context,
			Handler handler) {
		super(surfaceHolder, context, handler, 10);
	}

	@Override
	protected void doDraw(Canvas canvas) {
		if (this.width != canvas.getWidth()
				|| this.height != canvas.getHeight()) {
			initialize(canvas.getHeight(), canvas.getWidth());
		}

		canvas.drawColor(Color.BLACK);
		
		drawBackground(canvas);

		drawFPS(canvas);

		drawScore(canvas);

		drawLevel(canvas);

		drawPercentage(canvas);

		for (MissileSilo silo : silos) {
			silo.draw(canvas);
		}

		for (Missile missile : incomingMissiles) {
			missile.draw(canvas);
		}
		for (Missile missile : outgoingMissiles) {
			missile.draw(canvas);
		}

		if (isKilled()) {
			drawGameOver(canvas);
		} else {
			drawMessage(canvas);
		}
	}

	private void drawBackground(Canvas canvas) {
		if (myImage == null) {
			Resources res = context.getResources();
			myImage = res.getDrawable(R.drawable.lunar_surface);
		}
		myImage.setBounds(new Rect(
				0,
				canvas.getHeight()-25,
				canvas.getWidth(),
				canvas.getHeight()));
		myImage.draw(canvas);
	}

	private void drawGameOver(Canvas canvas) {
		final String HIGH_SCORE_KEY = "HighScore";

		SharedPreferences sharedPreferences = context.getSharedPreferences(
				LastDefender.class.getName(), Context.MODE_PRIVATE);

		long highScore = sharedPreferences.getLong(HIGH_SCORE_KEY, 0);
		if (score > highScore) {
			highScore = score;
			Editor editor = sharedPreferences.edit();

			editor.putLong(HIGH_SCORE_KEY, highScore);
			editor.commit();
		}

		Paint paint = new Paint();
		paint.setColor(Color.WHITE);
		paint.setTextAlign(Align.CENTER);
		paint.setTextSize(30);
		canvas.drawText("Game Over", width / 2, height / 2, paint);
		paint.setTextSize(17);
		canvas.drawText("Score: " + score, width / 2, height / 2 + 20, paint);
		canvas.drawText("High Score: " + highScore, width / 2, height / 2 + 40,
				paint);
		canvas.drawText("Shots Fired: " + shotsFired, width / 2,
				height / 2 + 60, paint);
		canvas.drawText("Missiles Defended: " + missilesDestroyed, width / 2,
				height / 2 + 80, paint);
	}

	private void drawMessage(Canvas canvas) {
		if (timeShowingMessage < timeToShowMessage) {
			Paint paint = new Paint();
			paint.setColor(Color.WHITE);
			paint.setTextAlign(Align.CENTER);
			paint.setTextSize(30);
			canvas.drawText(message, width / 2, height / 2, paint);
		}
	}

	private void drawFPS(Canvas canvas) {
		if (lastTimeElapsedSinceLastUpdate > 0) {
			lastFPS = (lastFPS * 20 + 1000 / lastTimeElapsedSinceLastUpdate) / 21;
		}

		Paint textPaint = new TextPaint();
		textPaint.setColor(Color.WHITE);
		canvas.drawText("FPS:   " + lastFPS, 2, 60, textPaint);
	}

	private void drawScore(Canvas canvas) {
		Paint textPaint = new TextPaint();
		textPaint.setColor(Color.WHITE);
		canvas.drawText("Score: " + score, 2, 30, textPaint);
	}

	private void drawLevel(Canvas canvas) {
		Paint textPaint = new TextPaint();
		textPaint.setColor(Color.WHITE);
		canvas.drawText("Level: " + (level - 1), 2, 15, textPaint);
	}

	private void drawPercentage(Canvas canvas) {
		Paint textPaint = new TextPaint();
		textPaint.setColor(Color.WHITE);
		canvas.drawText("Effectiveness: "
				+ (shotsFired == 0 ? "--" : ""
						+ (((float) missilesDestroyed) / shotsFired * 100))
				+ "%", 2, 45, textPaint);
	}

	@Override
	public void initialize(int height, int width) {
		this.height = height;
		this.width = width;

		SharedPreferences sharedPreferences = context.getSharedPreferences(
				LastDefender.class.getName(), Context.MODE_PRIVATE);

		String difficultyModifier = sharedPreferences.getString(
				Preferences.DIFFICULTY, "1");
		if ("1".equals(difficultyModifier)) {
			this.difficultyModifier = 0.5f;
		} else if ("2".equals(difficultyModifier)) {
			this.difficultyModifier = 1f;
		} else if ("3".equals(difficultyModifier)) {
			this.difficultyModifier = 1.5f;
		} else if ("4".equals(difficultyModifier)) {
			this.difficultyModifier = 2.5f;
		}

		lastTimeElapsedSinceLastUpdate = 0;
		lastFPS = 0;
		level = 1;
		score = 0;
		missileFiringLatency = 3000;

		shotsFired = 0;
		missilesDestroyed = 0;

		missilesToCreate = 0;
		msRemainderSinceLastUpdate = 0;
		msMissileLatency = 1000;

		incomingMissiles = new HashSet<Missile>();
		outgoingMissiles = new HashSet<Missile>();
		silos = new ArrayList<MissileSilo>();

		createSilos();
	}

	private void createSilos() {
		final int numberOfSilos = 10;
		final int w = 20;
		final int h = 30;
		final int leftX = w / 2 + 3;
		final int rightX = width - (w / 2 + 9);
		final int Y = height - 5 - h / 2;

		for (int i = 0; i < numberOfSilos; i++) {
			silos.add(new MissileSilo(context, leftX
					+ ((rightX - leftX) / (numberOfSilos - 1) * i), Y, h, w));
		}
	}

	@Override
	protected boolean onKeyDown(int keyCode, KeyEvent msg) {
		return false;
	}

	@Override
	protected boolean onKeyUp(int keyCode, KeyEvent msg) {
		return false;
	}

	@Override
	protected void onPause() {
	}

	@Override
	protected void onUnPause() {
	}

	@Override
	protected boolean updateModel(long timeElapsedSinceLastUpdate,
			long currentTime) {
		for (Missile missile : incomingMissiles) {
			missile.updateSize(timeElapsedSinceLastUpdate);
		}
		for (Missile missile : outgoingMissiles) {
			missile.updateSize(timeElapsedSinceLastUpdate);
		}
		timeShowingMessage += timeElapsedSinceLastUpdate;

		detectCollisions(timeElapsedSinceLastUpdate, currentTime);

		lastTimeElapsedSinceLastUpdate = timeElapsedSinceLastUpdate;
		for (Missile missile : incomingMissiles) {
			missile.setX(missile.getX()
					+ (missile.getDx() * timeElapsedSinceLastUpdate / 1000));
			missile.setY(missile.getY()
					+ (missile.getDy() * timeElapsedSinceLastUpdate / 1000));
		}
		for (Missile missile : outgoingMissiles) {
			missile.setX(missile.getX()
					+ (missile.getDx() * timeElapsedSinceLastUpdate / 1000));
			missile.setY(missile.getY()
					+ (missile.getDy() * timeElapsedSinceLastUpdate / 1000));

			if (missile.getY() < missile.getTargetY()) {
				missile.setX(missile.getTargetX());
				missile.setY(missile.getTargetY());
				missile.setExploding(true);
			}
		}

		if (incomingMissiles.size() == 0) {
			missilesToCreate = (int) ((difficultyModifier * level / 2f) * 3);
			msRemainderSinceLastUpdate = msMissileLatency;
			level++;
			msMissileLatency -= 20;

			showLevelUpMessage();

			for (MissileSilo silo : silos) {
				if (silo.getLastCollision() != 0) {
					silo.setLastCollision(0);
					break;
				}
			}
		}

		Iterator<Missile> missileIterator = incomingMissiles.iterator();
		while (missileIterator.hasNext()) {
			if (missileIterator.next().isDead()) {
				missileIterator.remove();
			}
		}
		missileIterator = outgoingMissiles.iterator();
		while (missileIterator.hasNext()) {
			if (missileIterator.next().isDead()) {
				missileIterator.remove();
			}
		}

		boolean liveSilos = false;
		for (MissileSilo silo : silos) {
			if (silo.getLastCollision() == 0) {
				liveSilos = true;
				break;
			}
		}

		if (!liveSilos) {
			kill();
		}

		for (MissileSilo silo : silos) {
			silo.setPercentageReady(Math.min(1,
					(System.currentTimeMillis() - silo.getLastFired())
							/ (float) missileFiringLatency));
		}

		if (missilesToCreate > 0) {
			msRemainderSinceLastUpdate += timeElapsedSinceLastUpdate;
			while (msRemainderSinceLastUpdate - msMissileLatency > 0
					&& missilesToCreate > 0) {
				incomingMissiles.add(createNewIncomingMissile());
				msRemainderSinceLastUpdate -= msMissileLatency;
				missilesToCreate--;
			}
		}

		return true;
	}

	private void detectCollisions(long timeElapsed, long currentTime) {
		detectMissileExplosionCollisions(timeElapsed, currentTime);
		detectMissileSiloCollisions(timeElapsed, currentTime);
		detectSiloExplosionCollisions(timeElapsed, currentTime);
	}

	private Missile createNewMissile(float startX, float startY, float endX,
			float endY, float size, float speedConstant, int color) {
		return createNewMissile(startX, startY, endX, endY, size,
				speedConstant, color, false, false);
	}

	private Missile createNewMissile(float startX, float startY, float endX,
			float endY, float size, float speedConstant, int color,
			boolean isBomb, boolean isNuke) {
		final float speed = speedConstant
				* (2 + (difficultyModifier * level / 20f));
		final float deltax = (endX - startX);
		final float deltay = (endY - startY);
		final float distance = (float) Math.sqrt(deltax * deltax + deltay
				* deltay);
		final float dx = speed * deltax / distance;
		final float dy = speed * deltay / distance;

		if (isBomb) {
			if (isNuke) {
				return new Nuke(startX, startY, dx, dy, size, endX, endY, color);
			} else {
				return new Bomb(startX, startY, dx, dy, size, endX, endY, color);
			}
		}
		return new Missile(startX, startY, dx, dy, size, endX, endY, color);
	}

	private Missile createNewIncomingMissile() {
		MissileSilo silo = silos.get((int) (silos.size() * Math.random()));

		final float speedConstant = 20;
		final float x = (float) ((float) width * Math.random());
		final float y = 0;
		final float size = 1;

		if (Math.random() < 0.015) {
			return createNewNuke(x, y, silo.getX(), silo.getY(), size,
					speedConstant, Color.RED);
		} else if (Math.random() < Math.min(0.03 * difficultyModifier * level,
				0.25)) {
			return createNewBomb(x, y, silo.getX(), silo.getY(), size,
					speedConstant, Color.RED);
		}
		return createNewMissile(x, y, silo.getX(), silo.getY(), size,
				speedConstant, Color.RED);
	}

	private Missile createNewBomb(float startX, float startY, float endX,
			float endY, float size, float speedConstant, int color) {
		return createNewMissile(startX, startY, endX, endY, size,
				speedConstant, color, true, false);
	}

	private Missile createNewNuke(float startX, float startY, float endX,
			float endY, float size, float speedConstant, int color) {
		return createNewMissile(startX, startY, endX, endY, size,
				speedConstant, color, true, true);
	}

	@Override
	protected void onSetSurfaceSize(int width, int height) {
		if (this.width != width || this.height != height) {
			initialize(height, width);
		}
	}

	private void detectSiloExplosionCollisions(long timeElapsed,
			long currentTime) {
		// TODO Auto-generated method stub

	}

	private void detectMissileSiloCollisions(long timeElapsed, long currentTime) {
		for (int i = 0; i < silos.size(); i++) {
			MissileSilo silo = silos.get(i);
			for (Missile missile : incomingMissiles) {
				if (!missile.isDead() && !missile.isExploding()) {
					if (missile.getX() > silo.getX() - silo.getWidth() / 2
							&& missile.getX() < silo.getX() + silo.getWidth()
									/ 2
							&& missile.getY() > silo.getY() - silo.getHeight()
									/ 2
							&& missile.getY() > silo.getY() - silo.getHeight()
									/ 2 || missile.getY() > height) {
						missile.setExploding(true);
						silo.setLastCollision(currentTime);

						if (missile instanceof Bomb) {
							if (i > 0) {
								silos.get(i - 1).setLastCollision(currentTime);
							}
							if (i < silos.size() - 2) {
								silos.get(i + 1).setLastCollision(currentTime);
							}
							if (missile instanceof Nuke) {
								if (i > 1) {
									silos.get(i - 2).setLastCollision(
											currentTime);
								}
								if (i < silos.size() - 3) {
									silos.get(i + 2).setLastCollision(
											currentTime);
								}
							}
						}

						vibrate();
					}
				}
			}
		}
	}

	private void vibrate() {
		SharedPreferences sharedPreferences = context.getSharedPreferences(
				LastDefender.class.getName(), Context.MODE_PRIVATE);

		boolean vibrateOn = sharedPreferences.getBoolean(
				Preferences.VIBRATE_ON, false);
		if (vibrateOn) {
			((Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE))
					.vibrate(300);
		}
	}

	private void detectMissileExplosionCollisions(long timeElapsed,
			long currentTime) {
		for (Missile incomingMissile : incomingMissiles) {
			if (incomingMissile.isExploding()) {
				for (Missile incomingMissileCheck : incomingMissiles) {
					if (!incomingMissileCheck.isExploding()) {
						if (calculateDistance(incomingMissile.getX(),
								incomingMissile.getY(), incomingMissileCheck
										.getX(), incomingMissileCheck.getY()) <= incomingMissile
								.getSize()
								+ incomingMissileCheck.getSize()) {
							if (incomingMissile.getExploder() != null) {
								incomingMissile.getExploder().setChainCount(
										incomingMissile.getExploder()
												.getChainCount() + 1);
								missilesDestroyed++;
								score += difficultyModifier
										* level
										* incomingMissile.getExploder()
												.getChainCount()
										* missilesDestroyed / shotsFired;

								incomingMissileCheck
										.setExploder(incomingMissile
												.getExploder());

								showChainMessage(incomingMissile.getExploder()
										.getChainCount());
							}
							incomingMissileCheck.setExploding(true);
						}
					}
				}
				for (Missile outgoingMissileCheck : outgoingMissiles) {
					if (!outgoingMissileCheck.isExploding()) {
						if (calculateDistance(incomingMissile.getX(),
								incomingMissile.getY(), outgoingMissileCheck
										.getX(), outgoingMissileCheck.getY()) <= incomingMissile
								.getSize()
								+ outgoingMissileCheck.getSize()) {
							outgoingMissileCheck.setExploding(true);
						}
					}
				}
			} else {
				for (Missile outgoingMissileCheck : outgoingMissiles) {
					if (calculateDistance(incomingMissile.getX(),
							incomingMissile.getY(),
							outgoingMissileCheck.getX(), outgoingMissileCheck
									.getY()) <= incomingMissile.getSize()
							+ outgoingMissileCheck.getSize()) {
						if (!incomingMissile.isExploding()) {
							missilesDestroyed++;
							if (incomingMissile.getExploder() != null) {
								incomingMissile.getExploder().setChainCount(
										incomingMissile.getExploder()
												.getChainCount() + 1);
								score += difficultyModifier
										* level
										* incomingMissile.getExploder()
												.getChainCount()
										* missilesDestroyed / shotsFired;

								showChainMessage(incomingMissile.getExploder()
										.getChainCount());
							} else {
								incomingMissile
										.setExploder(outgoingMissileCheck);
								score += difficultyModifier
										* level
										* incomingMissile.getExploder()
												.getChainCount();
							}
						}

						outgoingMissileCheck.setExploding(true);
						incomingMissile.setExploding(true);
					}
				}
			}
		}
	}

	private float calculateDistance(float x, float y, float x2, float y2) {
		return (float) Math.sqrt(Math.pow(x - x2, 2) + Math.pow(y - y2, 2));
	}

	@Override
	protected boolean onTouchEvent(MotionEvent event) {
		if (event.getAction() != MotionEvent.ACTION_DOWN) {
			return false;
		}

		// first fine the closest live silo that's ready to fire
		MissileSilo silo = null;

		for (MissileSilo siloCursor : silos) {
			if (siloCursor.getLastCollision() == 0
					&& (siloCursor.getLastFired() == 0 || System
							.currentTimeMillis()
							- missileFiringLatency > siloCursor.getLastFired())) {
				if (silo == null) {
					silo = siloCursor;
				} else {
					if (Math.abs(event.getX() - siloCursor.getX()) < Math
							.abs(event.getX() - silo.getX())) {
						silo = siloCursor;
					}
				}
			}
		}

		if (silo != null) {
			silo.setLastFired(System.currentTimeMillis());
			outgoingMissiles.add(createNewMissile(silo.getX(), silo.getY()
					- silo.getHeight() / 2, event.getX(), Math.min(
					event.getY(), height - silo.getHeight() - 15), 1, 120,
					Color.GREEN));
			shotsFired++;
		}

		return true;
	}

	private void showChainMessage(int chain) {
		showMessage("Chain Bonus x" + chain);
	}

	private void showLevelUpMessage() {
		showMessage("Level Up!");
	}

	private void showMessage(String message) {
		this.message = message;
		timeShowingMessage = 0;
	}
}
