package com.michaelkrauklis.android.thread;

import android.content.Context;
import android.graphics.Canvas;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;

public abstract class RunThread extends Thread {
	private static enum ThreadState {
		Initial, Lose, Pause, Ready, Running, Win, Killed
	}

	protected SurfaceHolder surfaceHolder;
	protected Context context;
	protected Handler handler;
	private ThreadState state = ThreadState.Initial;
	private long lastModelUpdate = 0;
	private long threadThrottleMS = -1;

	protected int height = -1;
	protected int width = -1;

	public RunThread(SurfaceHolder surfaceHolder, Context context,
			Handler handler) {
		this(surfaceHolder, context, handler, -1);
	}

	public RunThread(SurfaceHolder surfaceHolder, Context context,
			Handler handler, long threadThrottleMS) {
		setName("RunThread - " + getClass().getSimpleName());

		this.surfaceHolder = surfaceHolder;
		this.context = context;
		this.handler = handler;
		this.threadThrottleMS = threadThrottleMS;
	}

	public void pause() {
		synchronized (surfaceHolder) {
			onPause();
		}
		setState(ThreadState.Pause);
	}

	public void unPause() {
		if (ThreadState.Initial == state) {
			start();
		}
		synchronized (surfaceHolder) {
			onUnPause();
		}
		setState(ThreadState.Running);
	}

	private void setState(ThreadState state) {
		if (state == ThreadState.Running && this.state != ThreadState.Running) {
			lastModelUpdate = System.currentTimeMillis();
		}
		this.state = state;
	}

	public void kill() {
		setState(ThreadState.Killed);
	}

	public boolean isKilled() {
		return ThreadState.Killed == state;
	}

	public boolean isInitial() {
		return ThreadState.Initial == state;
	}

	public boolean doKeyDown(int keyCode, KeyEvent msg) {
		synchronized (surfaceHolder) {
			return onKeyDown(keyCode, msg);
		}
	}

	public boolean doKeyUp(int keyCode, KeyEvent msg) {
		synchronized (surfaceHolder) {
			return onKeyUp(keyCode, msg);
		}
	}

	public boolean doTouchEvent(MotionEvent event) {
		synchronized (surfaceHolder) {
			return onTouchEvent(event);
		}
	}

	protected abstract boolean onTouchEvent(MotionEvent event);

	public boolean isRunning() {
		return state == ThreadState.Running;
	}

	@Override
	public void run() {
		while (true) {
			long now = System.currentTimeMillis();
			if (!isRunning()) {
				try {
					sleep(250);
				} catch (InterruptedException e) {
				}
			}
			if (lastModelUpdate + threadThrottleMS > now) {
				try {
					Thread
							.sleep((threadThrottleMS - now + lastModelUpdate) / 2);
				} catch (InterruptedException e) {
				}
			} else {
				Canvas canvas = null;
				try {
					canvas = surfaceHolder.lockCanvas(null);
					if (canvas != null) {
						synchronized (surfaceHolder) {
							if (isRunning()) {
								long timeElapsedSinceLastUpdate = System
										.currentTimeMillis()
										- lastModelUpdate;

								lastModelUpdate = System.currentTimeMillis();

								updateModel(timeElapsedSinceLastUpdate,
										lastModelUpdate);
							}
							doDraw(canvas);
							canvas.restore();
						}
					}
				} finally {
					if (canvas != null && surfaceHolder != null) {
						surfaceHolder.unlockCanvasAndPost(canvas);
					}
				}
			}
		}
	}

	public void setSurfaceSize(int width, int height) {
		synchronized (surfaceHolder) {
			onSetSurfaceSize(width, height);
		}
	}

	public void reinit() {
		synchronized (surfaceHolder) {
			pause();
			initialize(height, width);
			unPause();
		}
	}

	public abstract void initialize(int height, int width);

	protected abstract void doDraw(Canvas canvas);

	protected abstract boolean updateModel(long timeElapsedSinceLastUpdate,
			long currentTime);

	protected abstract void onPause();

	protected abstract void onUnPause();

	protected abstract void onSetSurfaceSize(int width, int height);

	protected abstract boolean onKeyDown(int keyCode, KeyEvent msg);

	protected abstract boolean onKeyUp(int keyCode, KeyEvent msg);
}