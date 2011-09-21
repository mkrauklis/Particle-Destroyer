package com.michaelkrauklis.android.lastdefender.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.SurfaceHolder;

import com.michaelkrauklis.android.lastdefender.run.LastDefenderRunThread;
import com.michaelkrauklis.android.view.RunView;

public class LastDefenderRunView extends RunView {

	public LastDefenderRunView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void createThread(SurfaceHolder surfaceHolder, Context context) {
		thread = new LastDefenderRunThread(surfaceHolder, context, getHandler());
	}
}
