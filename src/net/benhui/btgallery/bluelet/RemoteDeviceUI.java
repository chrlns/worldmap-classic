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

package net.benhui.btgallery.bluelet;

import javax.bluetooth.RemoteDevice;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.List;

/**
 *
 * <p>Title: Remote Device List Component</p>
 * <p>Description: This is a List screen to display a list of discovered Bluetooth
 * devices. This is a class used by BLUEletUI.
 *
 * </p>
 * @author Ben Hui (www.benhui.net)
 * @version 1.0
 *
 * LICENSE:
 * This code is licensed under GPL. (See http://www.gnu.org/copyleft/gpl.html)
 */
class RemoteDeviceUI extends List {

	public RemoteDeviceUI() {
		super("Bluetooth devices", List.IMPLICIT);

		addCommand(new Command("Select", Command.SCREEN, 1));
		addCommand(new Command("Search", Command.SCREEN, 2));
		addCommand(BLUElet.BACK);

		setCommandListener(BLUElet.instance);
	}

	/**
	 * Set a one-line message to screen.
	 * @param str String
	 */
	public void setMsg(String str) {
		//*** super.deleteAll();
		while (super.size() > 0) {
			super.delete(0);
		}
		append(str, null);

	}

	/**
	 * refresh the list with blutooth devices
	 */
	public void showui() {
		//*** super.deleteAll();
		while (super.size() > 0) {
			super.delete(0);
		}

		if (BLUElet.devices.size() > 0) {
			for (int i = 0; i < BLUElet.devices.size(); i++) {
				try {
					RemoteDevice device = (RemoteDevice) BLUElet.devices.elementAt(i);
					String name = device.getFriendlyName(false);
					append(name, null);

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} else {
			append("[No Device Found]", null);
		}
	}
}
