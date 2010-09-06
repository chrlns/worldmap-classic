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
 *
 * @author Christian Lins
 */
public interface TileLoadingObserver {

	/**
	 * Called after the image is completely loaded.
	 * @param img
	 * @param zoom
	 * @param x
	 * @param y
	 * @param mapSource
	 */
	void tileLoaded(Image img, int zoom, int x, int y, int mapSource, byte[] raw);

}
