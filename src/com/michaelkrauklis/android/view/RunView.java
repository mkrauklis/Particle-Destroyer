package com.michaelkrauklis.android.view;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.michaelkrauklis.android.thread.RunThread;

public abstract class RunView extends SurfaceView implements
		SurfaceHolder.Callback {
	protected Context context;
	protected RunThread thread;

	public RunView(Context context, AttributeSet attrs) {
		super(context, attrs);

		this.context = context;

		SurfaceHolder surfaceHolder = getHolder();
		surfaceHolder.addCallback(this);

		createThread(surfaceHolder, context);
	}

	protected abstract void createThread(SurfaceHolder surfaceHolder,
			Context context);

	/**
	 * Standard override to get key-press events.
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent msg) {
		return thread.doKeyDown(keyCode, msg);
	}

	/**
	 * Standard override for key-up. We actually care about these, so we can
	 * turn off the engine or stop rotating.
	 */
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent msg) {
		return thread.doKeyUp(keyCode, msg);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		return thread.doTouchEvent(event);
	}

	/**
	 * Standard window-focus override. Notice focus lost so we can pause on
	 * focus lost. e.g. user switches to take a call.
	 */
	@Override
	public void onWindowFocusChanged(boolean hasWindowFocus) {
		if (!thread.isInitial()) {
			if (hasWindowFocus) {
				thread.unPause();
			} else {
				thread.pause();
			}
		}
	}

	/* Callback invoked when the surface dimensions change. */
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		// thread.setSurfaceSize(width, height);
	}

	boolean surfaceHasBeenSet = false;

	public void surfaceCreated(SurfaceHolder surfaceHolder) {
		Canvas canvas = null;
		try {
			canvas = surfaceHolder.lockCanvas(null);

			if (!surfaceHasBeenSet) {
				thread.setSurfaceSize(canvas.getHeight(), canvas.getWidth());
				surfaceHasBeenSet = true;
			}
		} finally {
			if (canvas != null && surfaceHolder != null) {
				surfaceHolder.unlockCanvasAndPost(canvas);
			}
		}
		thread.unPause();
	}

	/*
	 * Callback invoked when the Surface has been destroyed and must no longer
	 * be touched. WARNING: after this method returns, the Surface/Canvas must
	 * never be touched again!
	 */
	public void surfaceDestroyed(SurfaceHolder holder) {
		thread.pause();
	}

	public void reinit() {
		thread.reinit();
	}
}
