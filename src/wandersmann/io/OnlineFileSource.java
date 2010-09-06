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

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Vector;
import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;
import javax.microedition.lcdui.Image;
import wandersmann.DebugDialog;

/**
 * Load tiles from a tile source.
 * @author Christian Lins
 */
class OnlineFileSource implements TileCache {

	public static final String	OSM_URL		= "http://tile.openstreetmap.org/";
	public static final String	OCM_URL		= "http://tile.opencyclemap.org/cycle/";

	public OnlineFileSource() {
	}

	public boolean initialize() {
		return true;
	}

	public boolean isEnabled() {
		return true;
	}

	/**
	 * Inefficient way to read the image data from the InputStream but
	 * necessary for some servers.
	 * @param in
	 * @param out
	 * @return
	 * @throws IOException
	 */
	private Image loadImage(InputStream in, ByteArrayOutputStream out) throws IOException {
		int b;
		while ((b = in.read()) != -1) {
			out.write(b);
		}
		byte[] buf = out.toByteArray();
		return Image.createImage(buf, 0, buf.length);
	}

	public Image loadImage(int zoom, int x, int y, int mapSource, boolean goDown, Vector obs) {
		String url;
		if(mapSource == TileCache.SOURCE_OPENCYCLEMAP) {
			url = OCM_URL;
		} else {
			url = OSM_URL;
		}
		url += zoom + "/" + x + "/" + y + ".png";
		DebugDialog.getInstance().addMessage("Note", "Loading " + url);

		HttpConnection conn = null;
		DataInputStream ins = null;
		try {
			conn = (HttpConnection)Connector.open(url);
			conn.setRequestMethod(HttpConnection.GET);
			if (conn.getResponseCode() != HttpConnection.HTTP_OK) {
				System.out.println(conn.getResponseCode());
				return null;
			}

			ins = conn.openDataInputStream();
			int slen = (int)conn.getLength();

			Image img;
			byte[] raw;
			if (slen == -1) {
				ByteArrayOutputStream buf = new ByteArrayOutputStream();
				img = loadImage(ins, buf);
				raw = buf.toByteArray();
			} else {
				raw = new byte[slen];
				ins.readFully(raw);
				img = Image.createImage(raw, 0, raw.length);
			}

			// Notify observer
			if(obs != null) {
				for(int n = 0, os = obs.size(); n < os; n++) {
					TileLoadingObserver observer = (TileLoadingObserver)obs.elementAt(n);
					observer.tileLoaded(img, zoom, x, y, mapSource, raw);
				}
			}

			return img;
		} catch(Exception ex) {
			DebugDialog.getInstance().addMessage("Excp", ex.getMessage());
			ex.printStackTrace();
		} finally {
			try {
				if(conn != null) {
					conn.close();
					conn = null;
				}
			} catch(IOException ex) {
				ex.printStackTrace();
			}
			try {
				if(ins != null) {
					ins.close();
					ins = null;
				}
			} catch(IOException ex) {
				ex.printStackTrace();
			}
		}
		return null;
	}

	public void lowMemAction() {
	}

	public void shutdown() {
	}

}
