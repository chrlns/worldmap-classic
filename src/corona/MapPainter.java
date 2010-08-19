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

package corona;

import corona.io.TileCache;

/**
 *
 * @author Christian Lins
 */
class MapPainter extends Thread {

	private static int numInstances = 0;

	private int zoom, x, y;
	private Map map;
	
	public MapPainter(Map map, int zoom, int x, int y) {
		this.map = map;
		this.zoom = zoom;
		this.x = x;
		this.y = y;
	}

	public void run() {
		if(numInstances >= 9) {
			return;
		} else {
			numInstances++;
		}

		try {
			// Simply warm the cache
			TileCache.getInstance().loadImage(zoom, x, y);

			// and repaint the map
			this.map.repaint();
			
		} catch(Throwable ex) {
			ex.printStackTrace();
		}
		numInstances--;
	}
}
