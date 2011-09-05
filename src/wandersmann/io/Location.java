/*
 *  WANDERSMANN - J2ME OpenStreetMap Client
 *  see AUTHORS for a list of contributors.
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

package wandersmann.io;

import java.util.Timer;
import java.util.TimerTask;
import javax.microedition.location.LocationProvider;
import javax.microedition.location.QualifiedCoordinates;
import org.qcontinuum.gpstrack.Gps;
import wandersmann.WandersmannMIDlet;

/**
 * A position on planet earth.
 * @author Christian Lins
 */
public class Location {

	private float x, y;
	private Gps gps = null;
	private Timer timer = null;
	private int satellites = 3;
	private WandersmannMIDlet midlet;

	public Location(WandersmannMIDlet midlet) {
		this.y = 52.0f; // y
		this.x = 8.0f; // x
		this.midlet = midlet;
		updateLocation();
	}

	public Location(float x, float y) {
		this.x = x;
		this.y = y;
	}

	public void attachBTGPS(String url) {
		if (!url.startsWith("btspp://")) {
			url = "btspp://" + url;
		}
		this.gps = new Gps(midlet, url);
		this.gps.start();
	}

	public void enableUpdateTimer(int secInterval) {
		secInterval = secInterval * 1000;
		this.timer = new Timer();
		this.timer.schedule(new TimerTask() {

			public void run() {
				if(updateLocation()) {
					midlet.getMap().locationUpdated();
				}
			}
		}, secInterval, secInterval);
	}

	public int getSatellites() {
		return this.satellites;
	}

	private boolean hasLocationAPI() {
		try {
			if(LocationProvider.getInstance(null) != null) {
				return true;
			}
		} catch(Throwable t) {
			t.printStackTrace();
		}
		return false;
	}

	public boolean hasLocationProvider() {
		return (this.gps != null && this.gps.isRunning()) ||
				hasLocationAPI();
	}

	public void shift(double dlon, double dlat) {
		this.y += dlat;
		this.x += dlon;
	}

	public float getX() {
		return x;
	}

	public float getY() {
		return y;
	}

	/**
	 * Updates the coordinates of this instance using a LocationProvider or
	 * an attached Bluetooth GPS device.
	 * @return
	 */
	public boolean updateLocation() {
		boolean updated = false;

		if(this.gps != null) {
			float newL = this.gps.getLatitude();
			if(!Float.isNaN(newL)) {
				this.y = newL;
				updated = true;
			}
			newL = this.gps.getLongitude();
			if(!Float.isNaN(newL)) {
				this.x = newL;
				updated = true;
			}
			this.satellites = this.gps.getSatelliteCount();
		} else {
			try {
				javax.microedition.location.Location location
						= LocationProvider.getLastKnownLocation();
				if (location != null) {
					QualifiedCoordinates coord = location.getQualifiedCoordinates();
					this.y = (float)coord.getLatitude();
					this.x = (float)coord.getLongitude();
					updated = true;
				}
			} catch (Throwable ex) {
				ex.printStackTrace();
			}
		}

		if(this.gps != null && !this.gps.isRunning()) {
			this.gps = null;
		}

		return updated;
	}

	public String toString() {
		StringBuffer buf = new StringBuffer();
		buf.append("Location@");
		buf.append(hashCode());
		buf.append(' ');
		buf.append(this.x);
		buf.append(' ');
		buf.append(this.y);
		buf.append('\n');
		return buf.toString();
	}

}
