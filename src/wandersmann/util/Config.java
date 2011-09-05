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

package wandersmann.util;

import wandersmann.DebugDialog;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import javax.microedition.rms.RecordEnumeration;
import javax.microedition.rms.RecordStore;
import javax.microedition.rms.RecordStoreException;
import org.qcontinuum.gpstrack.StringTokenizer;
import wandersmann.WandersmannMIDlet;

/**
 * Central app config. Stores data in "config" RecordStore.
 * @author Christian Lins
 */
public class Config {

	public static final String POS_X       = "PosX";
	public static final String POS_Y       = "PosY";
	public static final String LASTMAPTYPE = "LastMapType";
	public static final String ZOOM	       = "Zoom";

	private Hashtable keys = new Hashtable();
	private WandersmannMIDlet midlet;

	public Config(WandersmannMIDlet midlet) {
		this.midlet = midlet;
		try {
			RecordStore config = RecordStore.openRecordStore("config", true);
			
			RecordEnumeration configKeys = config.enumerateRecords(null, null, false);
			while(configKeys.hasNextElement()) {
				String kv = new String(configKeys.nextRecord());
				Vector vkv = StringTokenizer.getVector(kv, "=");
				keys.put(vkv.elementAt(0), vkv.elementAt(1));
			}
			
			configKeys.destroy();
			config.closeRecordStore();
		} catch(RecordStoreException ex) {
			midlet.getDebugDialog().addMessage("Exception", ex.getMessage());
		}
	}

	public String get(String key, String def) {
		if(this.keys.containsKey(key)) {
			Object obj = this.keys.get(key);
			return (String)obj;
		} else {
			return def;
		}
	}

	public float get(String key, float def) {
		String f = get(key, null);
		if(f != null) {
			return Float.parseFloat(f);
		} else {
			return def;
		}
	}

	public int get(String key, int def) {
		try {
			String f = get(key, null);
			if(f != null) {
				return Integer.parseInt(f);
			} else {
				return def;
			}
		} catch(NumberFormatException ex) {
			ex.printStackTrace();
			return def;
		}
	}

	public void set(String key, String value) {
		this.keys.put(key, value);
		store();
	}

	public void set(String key, float value) {
		set(key, Float.toString(value));
	}

	public void set(String key, int value) {
		set(key, Integer.toString(value));
	}

	private void store() {
		try {
			RecordStore.deleteRecordStore("config");
			RecordStore config = RecordStore.openRecordStore("config", true);

			Enumeration k = this.keys.keys();
			while(k.hasMoreElements()) {
				String key = (String)k.nextElement();
				byte[] buf = (key + "=" + (String)this.keys.get(key)).getBytes();
				config.addRecord(buf, 0, buf.length);
			}

			config.closeRecordStore();
		} catch(RecordStoreException ex) {
			ex.printStackTrace();
		}
	}

}
