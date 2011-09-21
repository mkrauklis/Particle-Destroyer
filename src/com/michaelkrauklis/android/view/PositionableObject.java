package com.michaelkrauklis.android.view;

public abstract class PositionableObject implements DrawableObject {
	private float x;
	private float y;

	public PositionableObject(float x, float y) {
		this.setX(x);
		this.setY(y);
	}

	public void setX(float x) {
		this.x = x;
	}

	public float getX() {
		return x;
	}

	public void setY(float y) {
		this.y = y;
	}

	public float getY() {
		return y;
	}
}
