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

package corona.osmbugs;

import corona.io.Location;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;

/**
 * Facade to the OpenStreetBugs API.
 * @author Christian Lins
 */
public class OpenStreetBugs {

	public static final String API_URL = "http://openstreetbugs.schokokeks.org/api/0.1/";

	public static void getBugs(float xmin, float xmax, float ymin, float ymax, BugReceiver rec) {
		BugLoader bloader = new BugLoader(xmin, xmax, ymin, ymax, rec);
		bloader.start();
	}

	public static void submitBug(Location location, String text, String user) {
		/*
			this.apiRequest("addPOIexec"
			+ "?lat="+encodeURIComponent(lonlat.lat)
			+ "&lon="+encodeURIComponent(lonlat.lon)
			+ "&text="+encodeURIComponent(description + " [" + this.getUserName() + "]")
			+ "&format=js"
		 */
		String url = API_URL + "addPOIexec?lat=" + location.getLatitude()
				+ "&lon=" + location.getLongitude() + "&text=" + urlEncode(text)
				+ "&format=js";
		try {
			HttpConnection httpConn = (HttpConnection)Connector.open(url);
			ByteArrayOutputStream reply = new ByteArrayOutputStream();
			InputStream in = httpConn.openDataInputStream();
			for(int b = in.read(); b != -1; b = in.read()) {
				reply.write(b);
			}
			in.close();
			System.out.println("OpenStreetBugs reply: " + reply.toString());
		} catch(IOException ex) {
			ex.printStackTrace();
		}
	}

	public static String urlEncode(String s) {
		StringBuffer sbuf = new StringBuffer();
		int ch;
		for (int i = 0; i < s.length(); i++) {
			ch = s.charAt(i);
			switch (ch) {
				case ' ': {
					sbuf.append("+");
					break;
				}
				case '!': {
					sbuf.append("%21");
					break;
				}
				case '*': {
					sbuf.append("%2A");
					break;
				}
				case '\'': {
					sbuf.append("%27");
					break;
				}
				case '(': {
					sbuf.append("%28");
					break;
				}
				case ')': {
					sbuf.append("%29");
					break;
				}
				case ';': {
					sbuf.append("%3B");
					break;
				}
				case ':': {
					sbuf.append("%3A");
					break;
				}
				case '@': {
					sbuf.append("%40");
					break;
				}
				case '&': {
					sbuf.append("%26");
					break;
				}
				case '=': {
					sbuf.append("%3D");
					break;
				}
				case '+': {
					sbuf.append("%2B");
					break;
				}
				case '$': {
					sbuf.append("%24");
					break;
				}
				case ',': {
					sbuf.append("%2C");
					break;
				}
				case '/': {
					sbuf.append("%2F");
					break;
				}
				case '?': {
					sbuf.append("%3F");
					break;
				}
				case '%': {
					sbuf.append("%25");
					break;
				}
				case '#': {
					sbuf.append("%23");
					break;
				}
				case '[': {
					sbuf.append("%5B");
					break;
				}
				case ']': {
					sbuf.append("%5D");
					break;
				}
				default:
					sbuf.append((char) ch);
			}
		}
		return sbuf.toString();
	}

}
