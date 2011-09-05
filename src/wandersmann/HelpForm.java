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

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Spacer;

/**
 * Help form.
 * @author Christian Lins
 */
public class HelpForm extends Form implements CommandListener {

	public static final Command BACK = new Command("Zurück", Command.BACK, 0);
	private WandersmannMIDlet midlet;

	public HelpForm(WandersmannMIDlet midlet) {
		super("Help");
		setCommandListener(this);

		this.midlet = midlet;

		addCommand(BACK);

		append("Press '5' to zoom in.");
		append(new Spacer(getWidth(), 1));
		append("Press '0' to zoom out.");
		append(new Spacer(getWidth(), 1));
		append("Press '4' and '6' to move map west and east.");
		append(new Spacer(getWidth(), 1));
		append("Press '2' and '8' to move map north and south.");
		append(new Spacer(getWidth(), 1));
		append("Press '1' to center the map to your current location if available.");
	}

	public void commandAction(Command cmd, Displayable disp) {
		if (cmd.equals(BACK)) {
			Display.getDisplay(midlet).setCurrent(midlet.getMap());
		}
	}
}
