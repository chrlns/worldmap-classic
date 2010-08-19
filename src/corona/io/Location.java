/*
 *  CORONA - J2ME OpenStreetMap Client
 *  Copyright (C) 2010 Christian Lins <christian.lins@fh-osnabrueck.de>
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *  If you need a commercial license for this little piece of software,
 *  feel free to contact the author.
 */

package corona.io;

import corona.CoronaMIDlet;
import corona.DebugDialog;
import java.util.Timer;
import java.util.TimerTask;
import javax.microedition.location.LocationProvider;
import javax.microedition.location.QualifiedCoordinates;
import org.qcontinuum.gpstrack.Gps;

/**
 *
 * @author Christian Lins
 */
public class Location {

	private double lat, lon;
	private Gps gps = null;
	private Timer timer = null;
	private int satellites = 3;

	public Location() {
		this.lat = 52.2640; // y
		this.lon = 8.03301; // x

		updateLocation();
	}

	public Location(double lat, double lon) {
		this.lat = lat;
		this.lon = lon;
	}

	public void attachBTGPS(String url) {
		if (!url.startsWith("btspp://")) {
			url = "btspp://" + url;
		}
		this.gps = new Gps(url);
		//this.gps.setPriority(Thread.MIN_PRIORITY);
		this.gps.start();
	}

	public void enableUpdateTimer(int secInterval) {
		secInterval = secInterval * 1000;
		this.timer = new Timer();
		this.timer.schedule(new TimerTask() {

			public void run() {
				if(updateLocation()) {
					CoronaMIDlet.getInstance().getMap().repaint();
				}
			}
		}, secInterval, secInterval);
	}

	public int getSatellites() {
		return this.satellites;
	}

	public void shift(double dlon, double dlat) {
		this.lat += dlat;
		this.lon += dlon;
	}

	public double getX() {
		return getLongitude();
	}

	public double getY() {
		return getLatitude();
	}

	public double getLatitude() {
		return this.lat;
	}

	public double getLongitude() {
		return this.lon;
	}

	/**
	 * Updates the coordinates of this instance using a LocationProvider or
	 * an attached Bluetooth GPS device.
	 * @return
	 */
	public boolean updateLocation() {
		boolean updated = false;
		if(this.gps != null) {
			double newL = this.gps.getLatitude();
			if(!Double.isNaN(newL)) {
				this.lat = newL;
				updated = true;
			}
			newL = this.gps.getLongitude();
			if(!Double.isNaN(newL)) {
				this.lon = newL;
				updated = true;
			}
			this.satellites = this.gps.getSatelliteCount();
		} else {
			try {
				javax.microedition.location.Location location
						= LocationProvider.getLastKnownLocation();
				if (location != null) {
					QualifiedCoordinates coord = location.getQualifiedCoordinates();
					this.lat = coord.getLatitude();
					this.lon = coord.getLongitude();
					updated = true;
				}
			} catch (Throwable ex) {
				ex.printStackTrace();
			}
		}
		if(updated) {
			DebugDialog.getInstance().addMessage("Note", "GPS: " + this.lon + " " + this.lat);
		}
		return updated;
	}

	public String toString() {
		StringBuffer buf = new StringBuffer();
		buf.append("Location@");
		buf.append(hashCode());
		buf.append(' ');
		buf.append(this.lon);
		buf.append(' ');
		buf.append(this.lat);
		buf.append('\n');
		return buf.toString();
	}

}
