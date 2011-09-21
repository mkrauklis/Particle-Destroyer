package com.michaelkrauklis.android.lastdefender.model;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

import com.michaelkrauklis.android.lastdefender.R;
import com.michaelkrauklis.android.view.PositionableObject;

public class MissileSilo extends PositionableObject {

	private static Drawable myImage;
	private static Drawable myImageDestroyed;

	private float height;
	private float width;

	private Context context;

	private long lastCollision = 0;
	private long lastFired = 0;

	public float percentageReady = 1;

	public MissileSilo(Context context, float x, float y, float height,
			float width) {
		super(x, y);

		this.context = context;
		this.height = height;
		this.width = width;
	}

	@Override
	public void draw(Canvas canvas) {
		final float left = getX() - (width / 2);
		final float top = getY() - (height / 2);
		final float right = getX() + (width / 2);
		final float bottom = getY() + (height / 2);

		if (myImage == null) {
			Resources res = context.getResources();
			myImage = res.getDrawable(R.drawable.missile_silo);
		}
		if (myImageDestroyed == null) {
			Resources res = context.getResources();
			myImageDestroyed = res.getDrawable(R.drawable.missile_silo2);
		}

		Drawable imageToDraw = lastCollision == 0 ? myImage : myImageDestroyed;

		imageToDraw.setBounds(new Rect((int) left, (int) top, (int) right,
				(int) bottom));
		imageToDraw.draw(canvas);

		final float meterLeft = right + 2;
		final float meterTop = top;
		final float meterRight = right + 6;
		final float meterBottom = bottom;

		Paint paint = new Paint();
		paint.setColor(lastCollision != 0 ? Color.RED : Color.WHITE);

		if (percentageReady != 100) {
			canvas.drawRect(meterLeft, meterTop + (meterBottom - meterTop)
					* (1 - percentageReady), meterRight, meterBottom, paint);
		}

		paint.setColor(Color.WHITE);
		canvas.drawLine(meterLeft, meterTop, meterRight, meterTop, paint);
		canvas.drawLine(meterLeft, meterBottom, meterRight, meterBottom, paint);
		canvas.drawLine(meterLeft, meterTop, meterLeft, meterBottom, paint);
		canvas.drawLine(meterRight, meterTop, meterRight, meterBottom, paint);
	}

	public float getHeight() {
		return height;
	}

	public long getLastCollision() {
		return lastCollision;
	}

	public long getLastFired() {
		return lastFired;
	}

	public float getPercentageReady() {
		return percentageReady;
	}

	public float getWidth() {
		return width;
	}

	public void setHeight(float height) {
		this.height = height;
	}

	public void setLastCollision(long lastCollision) {
		this.lastCollision = lastCollision;
	}

	public void setLastFired(long lastFired) {
		this.lastFired = lastFired;
	}

	public void setPercentageReady(float percentageReady) {
		this.percentageReady = percentageReady;
	}

	public void setWidth(float width) {
		this.width = width;
	}
}
