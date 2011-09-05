// J2ME GPS Track
// Copyright (C) 2006 Dana Peters
// http://www.qcontinuum.org/gpstrack
package org.qcontinuum.gpstrack;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;
import wandersmann.WandersmannMIDlet;

public class Gps extends Thread {

	private float mHeading, mSpeed, mAltitude;
	private boolean mFix;
	private int mHour, mMinute, mSecond;
	private int mDay, mMonth, mYear;
	private int mNmeaCount;
	private int mAllSatellites, mFixSatellites = 3;
	private GpsHorizontalPosition mGpsSatellites[] = new GpsHorizontalPosition[12];
	private float lat = Float.NaN;
	private float lon = Float.NaN;
	private String bluetoothURL;
	private boolean running = true;
	private WandersmannMIDlet midlet;

	public Gps(WandersmannMIDlet midlet, String url) {
		for (int i = 0; i < 12; i++) {
			mGpsSatellites[i] = new GpsHorizontalPosition();
		}
		mNmeaCount = 0;
		this.midlet = midlet;
		this.bluetoothURL = url;
	}

	public int getNmeaCount() {
		return mNmeaCount;
	}

	public GpsHorizontalPosition[] getSatellites() {
		return mGpsSatellites;
	}

	public float getLongitude() {
		return this.lon;
	}

	public float getLatitude() {
		return this.lat;
	}

	public float getHeading() {
		return mHeading;
	}

	public float getSpeed() {
		return mSpeed * 1.852f;
	}

	public float getAltitude() {
		return mAltitude;
	}

	public int getSatelliteCount() {
		return mFixSatellites;
	}

	public boolean getFix() {
		return mFix;
	}

	public boolean isRunning() {
		return this.running && isAlive();
	}

	public void run() {
		try {
			// TODO: Connection may fail if already open
			StreamConnection streamConnection = (StreamConnection) Connector.open(this.bluetoothURL);
			InputStream ins = streamConnection.openInputStream();

			ByteArrayOutputStream buf = new ByteArrayOutputStream();
			while (running) {
				int ch = 0;
				try {
					while ((ch = ins.read()) != '\n') {
						buf.write(ch);
					}
					buf.flush();
					byte[] b = buf.toByteArray();
					String line = new String(b);
// TODO: Printout line to debug
					try {
						receiveNmea(line);
					} catch (Exception ex) {
						midlet.getDebugDialog().addMessage("Excp", ex.getMessage());
					}
					buf.reset();
				} catch (IOException ex) {
					midlet.getDebugDialog().addMessage("IOExcp", ex.getMessage());
				}
			}

			ins.close();
			streamConnection.close();
			this.running = false;
		} catch (Throwable ex) {
			this.running = false;
			midlet.getDebugDialog().addMessage("Excp", ex.getMessage());
		}
	}

	private void extractData(String[] param, int a, int b, int c, int d, int e) {
		int degree, minute, fraction;
		float latitude = Float.NaN, longitude = Float.NaN;
		if (param[a].length() > 8 && param[b].length() == 1) {
			degree = Integer.parseInt(param[a].substring(0, 2));
			minute = Integer.parseInt(param[a].substring(2, 4));
			fraction = Integer.parseInt(param[a].substring(5, 9).concat("0000").substring(0, 4));
			latitude = degree + (minute / 60.0f) + (fraction / 600000.0f);
			if (param[b].charAt(0) == 'S') {
				latitude = -latitude;
			}
		}
		if (param[c].length() > 9 && param[d].length() == 1) {
			degree = Integer.parseInt(param[c].substring(0, 3));
			minute = Integer.parseInt(param[c].substring(3, 5));
			fraction = Integer.parseInt(param[c].substring(6, 10).concat("0000").substring(0, 4));
			longitude = degree + (minute / 60.0f) + (fraction / 600000.0f);
			if (param[d].charAt(0) == 'W') {
				longitude = -longitude;
			}
		}
		if (param[e].length() > 5) {
			mHour = Integer.parseInt(param[e].substring(0, 2));
			mMinute = Integer.parseInt(param[e].substring(2, 4));
			mSecond = Integer.parseInt(param[e].substring(4, 6));
		}
		if (!Float.isNaN(latitude) && !Float.isNaN(longitude)) {
			this.lat = latitude;
			this.lon = longitude;
		}
	}

	private void receiveNmea(String nmea) {
		int starIndex = nmea.indexOf('*');
		if (starIndex == -1) {
			return;
		}
		String[] param = StringTokenizer.getArray(nmea.substring(0, starIndex), ",");
		if (param[0].equals("$GPGSV")) {
			int i, j;
			mNmeaCount++;
			mAllSatellites = Integer.parseInt(param[3]);
			j = (Integer.parseInt(param[2]) - 1) * 4;
			for (i = 4; i < 17 && j < 12; i += 4, j++) {
				mGpsSatellites[j].setNumber(Integer.parseInt(param[i]));
				mGpsSatellites[j].setElevation(Integer.parseInt(param[i + 1]));
				mGpsSatellites[j].setAzimuth(Integer.parseInt(param[i + 2]));
				mGpsSatellites[j].setSnr(param[i + 3].length() > 0 ? Integer.parseInt(param[i + 3]) : 0);
			}
		} else if (param[0].equals("$GPGLL")) {
			mNmeaCount++;
			extractData(param, 1, 2, 3, 4, 5);
			//qual = param[6].charAt(0);         // 'A'
			mFix = (param[6].charAt(0) == 'A');
		} else if (param[0].equals("$GPRMC")) {
			mNmeaCount++;
			//qual = param[2].charAt(0);         // 'A'
			extractData(param, 3, 4, 5, 6, 1);
			mFix = (param[2].charAt(0) == 'A');
			mDay = Integer.parseInt(param[9].substring(0, 2));
			mMonth = Integer.parseInt(param[9].substring(2, 4));
			mYear = 2000 + Integer.parseInt(param[9].substring(4, 6));
			mSpeed = Float.parseFloat(param[7]);
			if (param[8].length() > 0) {
				mHeading = Float.parseFloat(param[8]);
			}
		} else if (param[0].equals("$GPGGA")) {
			mNmeaCount++;
			extractData(param, 2, 3, 4, 5, 1);
			//qual2 = param[2].charAt(5);         // '1'
			//fix = (qual2 > '0');
			mFixSatellites = Integer.parseInt(param[7]);
			if (param[9].length() > 0) {
				mAltitude = Float.parseFloat(param[9]);
			}
			//altunit = param[10].charAt(0);
		} else if (param[0].equals("$GPGSA")) {
			int i, j, k;
			mNmeaCount++;
			for (i = 0; i < 12; i++) {
				mGpsSatellites[i].setFix(false);
			}
			for (j = 0; j < 12; j++) {
				if (param[j + 3].length() > 0) {
					if ((k = Integer.parseInt(param[j + 3])) != 0) {
						for (i = 0; i < mAllSatellites; i++) {
							if (mGpsSatellites[i].getNumber() == k) {
								mGpsSatellites[i].setFix(true);
								break;
							}
						}
					}
				}
			}
		}
	}
}
