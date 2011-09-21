package com.michaelkrauklis.android.lastdefender.model;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import com.michaelkrauklis.android.view.PositionableObject;

public class Missile extends PositionableObject {
	protected float maxSize = 40;
	protected float dsize = 20;

	protected float size = 1;

	protected float dy = 0;
	protected float dx = 0;

	protected float startingX;
	protected float startingY;

	protected float targetX = 0;
	protected float targetY = 0;

	protected Missile exploder = null;
	protected int chainCount = 1;

	protected boolean isExploding = false;

	protected int lineColor = Color.YELLOW;

	public Missile(float x, float y, float dx, float dy, float size,
			float targetX, float targetY) {
		this(x, y, dx, dy, size, targetX, targetY, Color.YELLOW);
	}

	public Missile(float x, float y, float dx, float dy, float size,
			float targetX, float targetY, int lineColor) {
		super(x, y);
		this.startingX = x;
		this.startingY = y;
		this.dx = dx;
		this.dy = dy;
		this.size = size;
		this.targetX = targetX;
		this.targetY = targetY;
		this.lineColor = lineColor;
	}

	@Override
	public void draw(Canvas canvas) {
		Paint paint = null;

		if (!isExploding) {
			paint = new Paint();
			paint.setColor(lineColor);
			paint.setAlpha(150);
			canvas.drawLine(startingX, startingY, getX(), getY(), paint);
		}

		paint = getPaint();

		canvas.drawCircle(getX(), getY(), size, paint);
	}

	public int getChainCount() {
		return chainCount;
	}

	public float getDx() {
		return dx;
	}

	public float getDy() {
		return dy;
	}

	public Missile getExploder() {
		return exploder;
	}

	protected Paint getPaint() {
		float redConversionFactor = Math.min(100 * size / maxSize, 255);
		float greenConversionFactor = Math.min(100 * size / maxSize, 255);
		float blueConversionFactor = Math.min(255 * size / maxSize, 255);
		float alpha = Math.min(255 * size / maxSize, 255);

		Paint paint = new Paint();
		paint.setColor(Color.argb((int) (255 - alpha),
				(int) (255 - redConversionFactor),
				(int) (255 - greenConversionFactor),
				(int) (255 - blueConversionFactor)));

		return paint;
	}

	public float getSize() {
		return size;
	}

	public float getTargetX() {
		return targetX;
	}

	public float getTargetY() {
		return targetY;
	}

	public boolean isDead() {
		return size > maxSize;
	}

	public boolean isExploding() {
		return isExploding;
	}

	public void setChainCount(int chainCount) {
		this.chainCount = chainCount;
	}

	public void setDx(float dx) {
		this.dx = dx;
	}

	public void setDy(float dy) {
		this.dy = dy;
	}

	public void setExploder(Missile exploder) {
		this.exploder = exploder;
	}

	public void setExploding(boolean isExploding) {
		this.isExploding = isExploding;

		if (isExploding) {
			dy = 0;
			dx = 0;
		}
	}

	public void setSize(float size) {
		this.size = size;
	}

	public void setTargetX(float targetX) {
		this.targetX = targetX;
	}

	public void setTargetY(float targetY) {
		this.targetY = targetY;
	}

	public void updateSize(long timeElapsed) {
		if (isExploding) {
			size += timeElapsed / 1000f * dsize;
		}
	}
}
