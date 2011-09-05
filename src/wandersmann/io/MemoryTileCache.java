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

import java.util.Hashtable;
import java.util.Vector;
import javax.microedition.lcdui.Image;

/**
 * Cache working on volatile memory.
 * @author Christian Lins
 */
class MemoryTileCache implements TileCache {

	private int			cacheSize	= 32;
	private Hashtable	tiles		= new Hashtable(cacheSize);
	private Vector		keys		= new Vector(cacheSize);
	private TileCache	successor;

	public MemoryTileCache(TileCache successor) {
		this.successor = successor;
	}

	private void addToMemoryCache(String url, Image img) {
		if (img != null && url != null) {
			tiles.put(url, img);
			keys.addElement(url);
			trimCache();
		}
	}

	void freeCache() {
		tiles.clear();
		keys = new Vector(cacheSize);
	}

	private void trimCache() {
		while (tiles.size() >= cacheSize) {
			tiles.remove(keys.firstElement());
			keys.removeElementAt(0);
		}
	}

	public boolean initialize() {
		this.successor.initialize();
		return true;
	}

	/**
	 * @return Always true as the Memory Cache is always enabled.
	 */
	public boolean isEnabled() {
		return true;
	}

	public Image loadImage(int zoom, int x, int y, int mapSource, boolean goDown, Vector obs) {

		// Hopefully efficiently build the key string
		StringBuffer str = new StringBuffer();
		str.append(mapSource);
		str.append('/');
		str.append(zoom);
		str.append('/');
		str.append(x);
		str.append('/');
		str.append(y);
		String key = str.toString();

		if(tiles.containsKey(key)) {
			return (Image)tiles.get(key);
		} else if(goDown) {
			Image img = this.successor.loadImage(zoom, x, y, mapSource, goDown, obs);
			if(img != null) {
				addToMemoryCache(key, img);
			}
			return img;
		} else {
			return null;
		}
	}

	public void shutdown() {
		this.successor.shutdown();
	}

}
