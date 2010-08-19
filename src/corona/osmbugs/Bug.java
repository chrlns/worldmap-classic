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

/**
 *
 * @author Christian Lins
 */
public class Bug {

	public static Bug parse(String raw) {
		// raw = "552542, 6.971592, 50.810296, 'Straßensystem auf Friedhof fehlt [TobiR, 2010-08-09 23:30:37 CEST]', 0"
		try {
			Bug bug = new Bug();

			int n = raw.indexOf(",", 0);
			String id = raw.substring(0, n).trim();
			bug.id = Integer.parseInt(id);

			int m = raw.indexOf(",", n + 1);
			String x = raw.substring(n + 1, m).trim();
			bug.lon = Float.parseFloat(x);

			n = raw.indexOf(",", m + 1);
			String y = raw.substring(m + 1, n).trim();
			bug.lat = Float.parseFloat(y);

			m = raw.lastIndexOf(',');
			bug.text = raw.substring(n + 1, m).trim();

			String fixed = raw.substring(m + 1).trim();
			if("1".equals(fixed)) {
				bug.fixed = true;
			}

			return bug;
		} catch(Exception ex) {
			System.out.println("Raw: " + raw);
			ex.printStackTrace();
			return null;
		}
	}

	private int id;
	private float lon, lat; 
	private String text;
	private boolean fixed = false;

	private Bug() {
	}

	public int getID() {
		return this.id;
	}

	public float getX() {
		return this.lon;
	}

	public float getY() {
		return this.lat;
	}

	public String getText() {
		return this.text;
	}

	public boolean isFixed() {
		return this.fixed;
	}

}
