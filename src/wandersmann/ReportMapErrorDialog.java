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

import wandersmann.io.Location;
import wandersmann.osmbugs.OpenStreetBugs;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.TextField;

/**
 *
 * @author Christian Lins
 */
public class ReportMapErrorDialog extends Form implements CommandListener {

	private Command cmdCancel = new Command("Cancel", "Cancel", Command.CANCEL, 1);
	private Command cmdSubmitBug = new Command("Submit", "Submit Map Error", Command.ITEM, 1);
	private Location location;
	private Map	map;
	private TextField txtUsername = new TextField("User name:", "NoName", 64, TextField.ANY);
	private TextField txtProblem = new TextField("Describe the Problem:", "", 255, TextField.ANY);

	public ReportMapErrorDialog(Location location, Map map) {
		super("Report Map Error");
		setCommandListener(this);
		
		addCommand(cmdCancel);
		addCommand(cmdSubmitBug);

		this.location = location;
		this.map = map;
		this.append("Position: " + location.getX() + " " + location.getY());

		this.append(txtProblem);
		this.append(txtUsername);
	}

	public void commandAction(Command cmd, Displayable displayable) {
		if(cmd.equals(this.cmdCancel)) {
			Display.getDisplay(CoronaMIDlet.getInstance()).setCurrent(this.map);
		} else if(cmd.equals(this.cmdSubmitBug)) {
			OpenStreetBugs.submitBug(location, txtProblem.getString(), txtUsername.getString());
			Display.getDisplay(CoronaMIDlet.getInstance()).setCurrent(this.map);
		}
	}

}
