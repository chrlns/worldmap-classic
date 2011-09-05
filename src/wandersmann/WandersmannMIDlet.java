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

package wandersmann;

import javax.microedition.lcdui.Display;
import javax.microedition.midlet.MIDlet;
import wandersmann.io.TileCacheManager;
import wandersmann.util.Config;

/**
 * The main midlet of the application.
 * @author Christian Lins
 */
public class WandersmannMIDlet extends MIDlet {

	private Config config;
	private DebugDialog debugDialog;
	private Map map;

	public WandersmannMIDlet() {
		this.config = new Config(this);
		this.debugDialog = new DebugDialog(this);
		this.map = new Map(this);

		TileCacheManager.initialize(this);
	}

	public Config getConfig() {
		return this.config;
	}

	public DebugDialog getDebugDialog() {
		return this.debugDialog;
	}

	public Map getMap() {
		return this.map;
	}

	public void startApp() {
		Display display = Display.getDisplay(this);
		display.setCurrent(this.map);
	}

	/**
	 * This method is called by the runtime when the MIDlet is paused.
	 * The MIDlet should free as many resources as possible and return from this
	 * method as soon as possible.
	 */
	public void pauseApp() {
		TileCacheManager.clearVolatileCache();
	}

	public void destroyApp(boolean unconditional) {
		this.map.shutdown();
	}

}
