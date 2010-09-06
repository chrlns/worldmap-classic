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

package wandersmann;

import javax.microedition.lcdui.Display;
import javax.microedition.midlet.MIDlet;

/**
 * The main midlet of the application. For historic reasons the name is still
 * CoronaMIDlet.
 * @author Christian Lins
 */
public class CoronaMIDlet extends MIDlet {

	private static CoronaMIDlet instance;

	public static CoronaMIDlet getInstance() {
		return instance;
	}

	private Map map = new Map();

	public Map getMap() {
		return this.map;
	}

	public void startApp() {
		instance = this;
		Display display = Display.getDisplay(this);
		display.setCurrent(this.map);
	}

	public void pauseApp() {
	}

	public void destroyApp(boolean unconditional) {
		this.map.shutdown();
		instance.notifyDestroyed();
	}

}
