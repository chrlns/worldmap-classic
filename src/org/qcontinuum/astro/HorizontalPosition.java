// J2ME Compass
// Copyright (C) 2006 Dana Peters
// http://www.qcontinuum.org/compass

package org.qcontinuum.astro;

public class HorizontalPosition {

	private float mAzimuth = 0;
	private float mElevation = 0;

	public HorizontalPosition() {
	}

	public HorizontalPosition(int azimuth, int elevation) {
		mAzimuth = (float) azimuth;
		mElevation = (float) elevation;
	}

	public HorizontalPosition(float azimuth, float elevation) {
		mAzimuth = azimuth;
		mElevation = elevation;
	}

	public float getAzimuthFloat() {
		return mAzimuth;
	}

	public float getElevationFloat() {
		return mElevation;
	}

	public void setAzimuth(float azimuth) {
		mAzimuth = azimuth;
	}

	public void setElevation(float elevation) {
		mElevation = elevation;
	}
}
