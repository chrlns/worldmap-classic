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
import java.util.Enumeration;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;

/**
 *
 * @author Christian Lins
 */
public class RootSelectionDialog extends Form implements CommandListener {

	private Command cmdExit = new Command("Exit", Command.EXIT, 1);
	private Command cmdOkay = new Command("OK", Command.OK, 1);

	public RootSelectionDialog() {
		super("Corona: Setup");

		append("Choose a directory for the Map cache:");
		try {
			Enumeration drives = TileCache.getRoots();
			int numRoots = 0;
			while(drives.hasMoreElements()) {
				append((String)drives.nextElement());
				numRoots++;
			}
			if(numRoots == 0) {
				append("No appropriate directory found!");
				append("Press OK to disable caching.");
			}
		} catch(Throwable ex) {
			ex.printStackTrace();
			append("Exception: " + ex.getMessage());
		}
		
		addCommand(cmdExit);
		addCommand(cmdOkay);
		setCommandListener(this);
	}

	public void commandAction(Command cmd, Displayable disp) {
		if(cmd.equals(this.cmdExit)) {
			CoronaMIDlet.getInstance().destroyApp(false);
		} else if(cmd.equals(this.cmdOkay)) {
			TileCache.getInstance().disableFileCache();
			Display.getDisplay(CoronaMIDlet.getInstance())
					.setCurrent(CoronaMIDlet.getInstance().getMap());
		}
	}

}
