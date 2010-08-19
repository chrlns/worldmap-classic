/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package corona;

import java.util.Vector;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Item;
import javax.microedition.lcdui.StringItem;

/**
 *
 * @author chris
 */
public class DebugDialog extends Form implements CommandListener {

	private static final DebugDialog instance = new DebugDialog();

	public static DebugDialog getInstance() {
		return instance;
	}

	private Command cmdBack = new Command("Back", Command.BACK, 0);

	public DebugDialog() {
		super("Debug");

		setCommandListener(this);
		addCommand(cmdBack);
	}

	public void addMessage(String label, String msg) {
		this.insert(0, new StringItem(label + ": ", msg));
		while(this.size() > 50) {
			delete(50);
		}
	}

	public void commandAction(Command command, Displayable displayable) {
		if(command.equals(this.cmdBack)) {
			Display.getDisplay(CoronaMIDlet.getInstance()).setCurrent(
					CoronaMIDlet.getInstance().getMap());
		}
	}

	public void show() {
		Display.getDisplay(CoronaMIDlet.getInstance()).setCurrent(this);
	}

}
