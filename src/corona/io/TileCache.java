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

package corona.io;

import corona.util.StringUtils;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;
import javax.microedition.io.file.FileConnection;
import javax.microedition.io.file.FileSystemRegistry;
import javax.microedition.lcdui.Image;

/**
 * Handles the tile caching.
 * @author Christian Lins
 */
public class TileCache {

	public static final String	OSM_URL		= "http://tile.openstreetmap.org/";
	public static final String	OCM_URL		= "http://tile.opencyclemap.org/cycle/";
	public static final String	FILECACHE	= "corona/tiles/";
	public static final String[] ROOTS = new String[]{"root1/", "e:/", "c:/"};

	private static TileCache instance = new TileCache();

	public static TileCache getInstance() {
		return instance;
	}

	public static Enumeration getRoots() {
		Enumeration drives = FileSystemRegistry.listRoots();
		System.out.println("The valid roots found are: ");
		while (drives.hasMoreElements()) {
			String root = (String) drives.nextElement();
			System.out.println("\t" + root);
		}
		return drives;
	}

	public  int			cacheSize	= 32;
	private boolean		error		= true;
	private boolean		fileCacheEnabled = true;
	private Hashtable	tiles		= new Hashtable(cacheSize);
	private String		tileServer	= OSM_URL;
	private Vector		keys		= new Vector(cacheSize);
	private int			rootIdx		= 0;
	private String		suffix		= "";

	private TileCache() {
		// Initialize file cache
		for(int n = 0; n < ROOTS.length; n++)
		{
			try {
				FileConnection fileConnection =
					(FileConnection)Connector.open("file:///" + ROOTS[n] + FILECACHE);

				if(!fileConnection.exists()) {
					mkdirs(ROOTS[n] + FILECACHE);
					System.out.println("Cache dir created.");
				}
				fileConnection.close();
				this.error = false;
				this.rootIdx = n;
				break;
			} catch(Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	private void addToMemoryCache(String url, Image img) {
		if(img != null && url != null) {
			tiles.put(url, img);
			keys.addElement(url);
			trimCache();
		}
	}

	public String getTileSuffix() {
		return this.suffix;
	}

	private void trimCache() {
		while (tiles.size() >= cacheSize) {
			tiles.remove(keys.firstElement());
			keys.removeElementAt(0);
		}
	}

	public void disableFileCache() {
		this.fileCacheEnabled = false;
	}

	public boolean hasErrors() {
		return this.error;
	}

	/**
	 * Create directories recursively.
	 * @param path Path to be created, without leading "file:///"
	 */
	private void mkdirs(String path) throws IOException {
		String[] dirs = StringUtils.split(path, "/");
		path = "/";
		for(int n = 0; n < dirs.length; n++) {
			FileConnection dirConn = (FileConnection)
					Connector.open("file://" + path + dirs[n] + "/");
			path += dirs[n] + "/";
			if(!dirConn.exists()) {
				dirConn.mkdir();
			}
			dirConn.close();
		}
	}

	private Image loadImage(InputStream in, int len) throws IOException {
		try {
			byte[] data = new byte[len];
			return loadImage(in, data);
		} catch(OutOfMemoryError err) {
			err.printStackTrace();
			this.cacheSize--;
			trimCache();
			return null;
		}
	}

	private Image loadImage(InputStream in, byte[] buf) throws IOException {
		try {
			DataInputStream dis = new DataInputStream(in);
			dis.readFully(buf);
			Image img = Image.createImage(buf, 0, buf.length);
			return img;
		} catch(IllegalArgumentException ex) {
			ex.printStackTrace();
			return null;
		}
	}

	private Image loadImage(InputStream in, ByteArrayOutputStream out) throws IOException {
		int b;
		while((b = in.read()) != -1) {
			out.write(b);
		}
		byte[] buf = out.toByteArray();
		return Image.createImage(buf, 0, buf.length);
	}

	public Image loadOfflineImage(int zoom, int x, int y) throws IOException {
		String url = zoom + "/" + x + "/" + y;
		if (tiles.containsKey(url)) {
			return (Image) tiles.get(url);
		} else {
			System.out.println("Memory Cache miss: " + url);

			if(this.fileCacheEnabled) {
				// Try to load from local file cache
				String fileUrl = "file:///" + ROOTS[rootIdx] + FILECACHE + url + suffix + ".png";
				FileConnection fileConnection =
						(FileConnection) Connector.open(fileUrl, Connector.READ_WRITE);
				if (fileConnection.exists()) {
					Image img = loadImage(fileConnection.openInputStream(),
							(int) fileConnection.fileSize());
					addToMemoryCache(url, img);
					return img;
				} else {
					return null;
				}
			} else {
				return null;
			}
		}
	}

	public Image loadImage(int zoom, int x, int y) throws IOException {
		String url = zoom + "/" + x + "/" + y;

		Image img = loadOfflineImage(zoom, x, y);
		if (img != null) {
			return img;
		} else {
			System.out.println("File Cache miss: " + url);
			HttpConnection httpConn = (HttpConnection)Connector.open(tileServer + url + ".png");

			if(httpConn.getResponseCode() != HttpConnection.HTTP_OK) {
				System.out.println(httpConn.getResponseCode());
				return null;
			}

			InputStream ins = httpConn.openInputStream();
			int slen = (int)httpConn.getLength();
			ByteArrayOutputStream buf;
			if(slen == -1) {
				buf = new ByteArrayOutputStream();
			} else {
				buf = new ByteArrayOutputStream(slen);
			}
			img = loadImage(ins, buf);
			if(this.fileCacheEnabled) {
				String fileUrl = "file:///" + ROOTS[rootIdx] + FILECACHE + url + suffix + ".png";
				FileConnection fileConnection =
					(FileConnection) Connector.open(fileUrl, Connector.READ_WRITE);
				// Store image to local file cache
				writeImage(fileConnection, buf.toByteArray(), ROOTS[rootIdx] + FILECACHE + zoom + "/" + x);
			}

			// Add loaded image to memory cache
			addToMemoryCache(url, img);

			return img;
		}
	}

	public void setTileServer(String server) {
		this.tileServer = server;
		this.keys = null;
		this.tiles = null;
		System.gc();
		this.keys = new Vector(cacheSize);
		this.tiles = new Hashtable(cacheSize);
	}

	public void setTileSuffix(String suffix) {
		this.suffix = suffix;
	}

	private void writeImage(FileConnection conn, byte[] buf, String path) throws IOException {
		FileConnection dirConn = (FileConnection)Connector.open("file:///" + path);
		if(!dirConn.exists()) {
			dirConn.close();
			mkdirs(path);
		}

		if(conn.exists()) {
			conn.truncate(0);
		} else {
			conn.create();
		}
		
		OutputStream out = conn.openOutputStream();
		out.write(buf);
		out.flush();
		out.close();
	}

}
