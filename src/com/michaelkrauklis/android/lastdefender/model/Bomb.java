package com.michaelkrauklis.android.lastdefender.model;

import android.graphics.Color;
import android.graphics.Paint;

public class Bomb extends Missile {

	public Bomb(float x, float y, float dx, float dy, float size,
			float targetX, float targetY, int lineColor) {
		super(x, y, dx, dy, size, targetX, targetY, lineColor);

		this.size *= 2;
		this.dsize *= 2.5;

		this.maxSize *= 3;
	}

	@Override
	protected Paint getPaint() {
		float redConversionFactor = Math.min(200 * size / maxSize, 255);
		float greenConversionFactor = Math.min(200 * size / maxSize, 255);
		float blueConversionFactor = Math.min(0 * size / maxSize, 255);
		float alpha = Math.min(255 * size / maxSize, 255);

		Paint paint = new Paint();
		paint.setColor(Color.argb((int) (255 - alpha),
				(int) (200 - redConversionFactor),
				(int) (200 - greenConversionFactor),
				(int) (255 - blueConversionFactor)));

		return paint;
	}
}
