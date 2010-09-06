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

import java.util.Vector;
import javax.microedition.lcdui.Image;

/**
 *
 * @author Christian Lins
 */
public interface TileCache {

	public static final int SOURCE_OPENSTREETMAP = 1;
	public static final int SOURCE_OPENCYCLEMAP = 2;

	boolean initialize();

	boolean isEnabled();

	/**
	 * Loads the tile identified through the given parameter.
	 * If obs is null the image is loaded synchronously.
	 * @param zoom
	 * @param x
	 * @param y
	 * @param mapSource
	 * @param goDown
	 */
	Image loadImage(int zoom, int x, int y, int mapSource, boolean goDown, Vector observer);

	/**
	 * Causes the cache to free unused elements.
	 */
	void lowMemAction();

	void shutdown();
}
