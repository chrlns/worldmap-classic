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

package corona.util;

/**
 * Useful mathematical functions.
 * @author Christian Lins
 */
public class Math2 {

	public static double atan(double x) {
		return x / (1 + 0.28 * (x * x));
	}

	public static double pow(double base, double exp) {
		if (exp == 0) {
			return 1;
		}
		double res = base;
		for (; exp > 1; --exp) {
			res *= base;
		}
		return res;
	}

	/**
	 * Calculates the logarithmus naturalis.
	 * See http://discussion.forum.nokia.com/forum/showthread.php?t=60817
	 * @param x
	 * @return
	 */
	public static double ln(double x) {
		long l = Double.doubleToLongBits(x);
		long exp = ((0x7ff0000000000000L & l) >> 52) - 1023;
		double man = (0x000fffffffffffffL & l) / (double) 0x10000000000000L + 1.0;
		double lnm = 0.0;
		double a = (man - 1) / (man + 1);
		for (int n = 1; n < 7; n += 2) {
			lnm += pow(a, n) / n;
		}
		return 2 * lnm + exp * 0.69314718055994530941723212145818;
	}

	public static double sinh(double x) {
		return (pow(Math.E, x) - pow(Math.E, -x)) / 2;
	}

	public static double toOSMMercatorX(double lon, int zoom) {
		return (((lon + 180.0)) / 360.0) * (1 << zoom);
	}

	public static double toOSMMercatorY(double lat, int zoom) {
		//double y = 0.5 * ln((1 + Math.sin(lat) / (1 - Math.sin(lat))));
		//return ((1 - (y / Math.PI)) / 2.0) * (1 << zoom);
		return (1 - Math2.ln(Math.tan(Math.toRadians(lat)) + 1 / Math.cos(Math.toRadians(lat))) / Math.PI) / 2 * (1 << zoom);
		//double phi = Math.PI / 180 * lat;
		//double p = ln(Math.tan(phi) + 1 / Math.cos(phi));
		//return ((1 << zoom) - 1) * ((Math.PI - p) / Math.PI);
	}

}
