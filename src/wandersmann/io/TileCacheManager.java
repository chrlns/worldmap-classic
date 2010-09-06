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
 * Controls the caching. This class has only static methods for performance
 * reasons.
 * @author Christian Lins
 */
public final class TileCacheManager {

	private TileCacheManager() {
		// Is never called
	}

	private static final TileCache memoryTileCache = new MemoryTileCache(
			new RMSTileCache(
					new OnlineFileSource()));

	public static void initialize() {
		TileCacheManager.memoryTileCache.initialize();
		TileLoader.Instance.start();
	}

	public static Image loadImage(int zoom, int x, int y, int mapSource, TileLoadingObserver obs) {
		Image img = memoryTileCache.loadImage(zoom, x, y, mapSource, false, null);
		if(img == null && obs != null) {
			TileLoadingTask task = new TileLoadingTask(zoom, x, y, mapSource, memoryTileCache, obs);
			TileLoader.Instance.addTask(task);
			return null;
		} else {
			return img;
		}
	}

	public static void shutdown() {
		memoryTileCache.shutdown();
	}

}
