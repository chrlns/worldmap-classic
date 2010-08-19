// J2ME GPS Track
// Copyright (C) 2006 Dana Peters
// http://www.qcontinuum.org/gpstrack

package org.qcontinuum.gpstrack;

import org.qcontinuum.astro.HorizontalPosition;

public class GpsHorizontalPosition extends HorizontalPosition {

	int mNumber;
	int mSnr;
	boolean mFix;

	public GpsHorizontalPosition() {
		super();
		mNumber = 0;
		mSnr = 0;
		mFix = false;
	}

	public void setNumber(int number) {
		mNumber = number;
	}

	public void setSnr(int snr) {
		mSnr = snr;
	}

	public void setFix(boolean fix) {
		mFix = fix;
	}

	public int getNumber() {
		return mNumber;
	}

	public int getSnr() {
		return mSnr;
	}

	public boolean getFix() {
		return mFix;
	}
}
