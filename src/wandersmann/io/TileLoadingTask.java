/*
 *  WANDERSMANN - J2ME OpenStreetMap Client
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

package wandersmann.io;

import javax.microedition.lcdui.Image;

/**
 * Thread that loads a tile asynchronously.
 * @author Christian Lins
 */
public class TileLoadingTask implements Runnable {

	public String URL;

	private TileCache cache;
	private TileLoadingObserver observer;
	private int x, y, zoom, mapSource;
	private long creationTime = System.currentTimeMillis();

	public TileLoadingTask(int zoom, int x, int y, int mapSource,
			TileCache cache, TileLoadingObserver observer) {
		if(cache == null || observer == null) {
			throw new IllegalArgumentException();
		}

		this.URL = mapSource + "/" + zoom + "/" + x + "/" + y;
		this.cache = cache;
		this.observer = observer;
		this.x = x;
		this.y = y;
		this.zoom = zoom;
		this.mapSource = mapSource;
	}

	public long creationTime() {
		return this.creationTime;
	}

	public void run() {
		try {
			Image img = this.cache.loadImage(zoom, x, y, mapSource, true, null);
			this.observer.tileLoaded(img, zoom, x, y, mapSource, null);
		} catch(Throwable ex) {
			ex.printStackTrace();
		}
	}

}
