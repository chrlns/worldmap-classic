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

import corona.io.Location;
import corona.io.TileCache;
import corona.osmbugs.Bug;
import corona.osmbugs.BugReceiver;
import corona.osmbugs.OpenStreetBugs;
import corona.util.Math2;
import java.io.IOException;
import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.ServiceRecord;
import javax.bluetooth.UUID;
import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import net.benhui.btgallery.bluelet.BLUElet;

/**
 * Main draw canvas (Map).
 * @author Christian Lins
 */
public class Map extends Canvas implements CommandListener, BugReceiver {

	// GPS extents of the map; logical extents are from 0 to 2^zoom
	public static final float MINY = -85.0511f;
	public static final float MAXY = 85.0511f;
	public static final float MINX = -180;
	public static final float MAXX = 180;
	public static final int MAXBUGS = 32;

	static float[] radPerPixel(int zoom) {
		int mapExtent = 1 << zoom;
		return new float[]
		{
			((MAXX + MAXX) / mapExtent) / 256,
			((MAXY + MAXY) / mapExtent) / 256
		};
	}

	static int[] tileNumbers(final double xl, final double yl, final int zoom) {
		double x = Math2.toOSMMercatorX(xl, zoom);
		double y = Math2.toOSMMercatorY(yl, zoom);
		double xdiff = x - Math.floor(x);
		double ydiff = y - Math.floor(y);
		return new int[] {
			(int)Math.floor(x), (int)Math.floor(y),
			(int)Math.floor(xdiff * 256), (int)Math.floor(ydiff * 256)
		};
	}

	static double tile2lon(int x, int zoom) {
		return x / Math2.pow(2.0, zoom) * 360.0 - 180;
	}

	static double tile2lat(int y, int zoom) {
		double n = Math.PI - (2.0 * Math.PI * y) / Math2.pow(2.0, zoom);
		return Math.toDegrees(Math2.atan(Math2.sinh(n)));
	}

	private Command	cmdExit	= new Command("Exit", Command.EXIT, 0);
	private Command cmdBugreport = new Command("Map Error", "Report Map Error", Command.ITEM, 1);
	private Command cmdShowBugs = new Command("Show Bugs", "Show Map Bugs", Command.ITEM, 1);
	private Command cmdSwitchMap = new Command("Switch Maps", "StreetMap <-> CycleMap", Command.ITEM, 1);
	private Command cmdAddBTGPS = new Command("Bluet. GPS", "Attach Bluetooth GPS", Command.ITEM, 0);
	private Command cmdDebug = new Command("Debug", Command.ITEM, 10);
	private int		zoom	= 12;
	private int[]	centerTileNumbers;
	private Location gpsPos = new Location();
	private Location scrollPos = new Location();
	private BLUElet bluelet = null;
	private Bug[]	bugs	= new Bug[MAXBUGS];
	private int		bugPnt	= 0;

	/**
	 * constructor
	 */
	public Map() {
		try {
			// Set up this canvas to listen to command events
			setCommandListener(this);

			// Add the Exit command
			addCommand(cmdExit);

			addCommand(cmdBugreport);
			addCommand(cmdShowBugs);
			addCommand(cmdSwitchMap);
			addCommand(cmdAddBTGPS);
			addCommand(cmdDebug);

			centerTileNumbers = tileNumbers(scrollPos.getX(), scrollPos.getY(), zoom);
			this.gpsPos.enableUpdateTimer(5);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void drawImage(Graphics g, int x, int y, int offX, int offY) throws IOException {
		Image img = TileCache.getInstance().loadOfflineImage(zoom, x, y);
		if(img != null) {
			g.drawImage(img, offX, offY, Graphics.TOP | Graphics.LEFT);
		} else {
			// If img IS null, then a repaint() is later called
			g.setColor(0, 0, 0);
			g.drawString("Loading...", offX, offY, Graphics.TOP | Graphics.LEFT);

			new MapPainter(this, zoom, x, y).start();
		}
	}

	private void drawGPSDot(Graphics g, int x, int y) {
		for(int n = 2; n < zoom * 6 / this.gpsPos.getSatellites(); n += 6) {
			g.setColor(0, 25, 255);
			g.drawArc(x - (n >> 1), y - (n >> 1), n, n, 0, 360);
		}
	}

	/**
	 * Draw the map.
	 */
	public void paint(Graphics g) {
		g.setColor(255, 255, 255);
		g.fillRect(0, 0, this.getWidth(), this.getHeight());

		try {
			int[] tileNumbers = centerTileNumbers;
			int offX = -tileNumbers[2] + getWidth() / 2;	// We want to transform the origin to the center
			int offY = -tileNumbers[3] + getHeight() / 2;	// of the screen

			System.out.println(offX + " " + offY);

			// Draw center image
			drawImage(g, tileNumbers[0], tileNumbers[1], offX , offY);

			// Draw image above
			drawImage(g, tileNumbers[0], tileNumbers[1] - 1, offX, offY - 256);

			// Draw image below
			drawImage(g, tileNumbers[0], tileNumbers[1] + 1, offX, offY + 256);

			// Draw left center image
			drawImage(g, tileNumbers[0] - 1, tileNumbers[1], offX - 256, offY);

			// Draw left image above
			drawImage(g, tileNumbers[0] - 1, tileNumbers[1] - 1, offX - 256, offY - 256);

			// Draw left image below
			drawImage(g, tileNumbers[0] - 1, tileNumbers[1] + 1, offX - 256, offY + 256);

			// Draw right center image
			drawImage(g, tileNumbers[0] + 1, tileNumbers[1], offX + 256, offY);

			// Draw right image above
			drawImage(g, tileNumbers[0] + 1, tileNumbers[1] - 1, offX + 256, offY - 256);

			// Draw right image below
			drawImage(g, tileNumbers[0] + 1, tileNumbers[1] + 1, offX + 256, offY + 256);

			// Draw bugs
			g.setColor(255, 0, 0);
			for(int n = 0; n < MAXBUGS; n++) {
				Bug bug = this.bugs[n];
				if(bug != null) {
					// Draw it! Now!
					int[] pos = posOnScreen(bug.getX(), bug.getY());
					g.fillArc(pos[0] - 3, pos[1] - 3, 6, 6, 0, 360);
				}
			}

			// Draw GPS position
			this.gpsPos.updateLocation();
			int[] pos = posOnScreen((float)gpsPos.getX(), (float)gpsPos.getY());
			drawGPSDot(g, pos[0], pos[1]);
			
			// Draw white bar at the bottom
			g.setColor(255, 255, 255);
			g.fillRect(0, getHeight() - 16, getWidth() / 2, getHeight() - 16);

			// Draw cursor and cursor position
			g.setColor(0, 0, 0);
			g.setFont(Font.getFont(Font.FACE_MONOSPACE, Font.STYLE_PLAIN, Font.SIZE_SMALL));
			g.fillArc(getWidth() / 2 - 2, getHeight() / 2 - 2, 4, 4, 0, 360);
			String scrollPosStr = (Double.toString(scrollPos.getX()) + "0000000").substring(0, 7)
					+ " " + (Double.toString(scrollPos.getY()) + "0000000").substring(0, 7);
			g.drawString(scrollPosStr, 1, getHeight() - 15, Graphics.TOP | Graphics.LEFT);

		} catch (Exception ex) {
			ex.printStackTrace();
			g.setColor(255, 0, 0);
			g.drawString("Exception: " + ex.getMessage(), 0, 30,
					Graphics.TOP | Graphics.LEFT);
		}
	}

	private int[] posOnScreen(float lon, float lat) {
		int[] tileNumbers = centerTileNumbers;
		int offX = -tileNumbers[2] + getWidth() / 2;	// We want to transform the origin to the center
		int offY = -tileNumbers[3] + getHeight() / 2;	// of the screen
		int[] pos = tileNumbers(lon, lat, zoom);
		int x = (pos[0] - tileNumbers[0]) * 256 + (pos[2] + offX);
		int y = (pos[1] - tileNumbers[1]) * 256 + (pos[3] + offY);
		return new int[] {x, y};
	}

	/**
	 * Called when a key is pressed.
	 */
	protected void keyPressed(int keyCode) {
		float zs = 10.0f / (1 << zoom);
		switch(keyCode) {
			case -1: // Up
				//offY += 10;
				scrollPos.shift(0, zs);
				//gpsShf.shift(0.05 / zoom, 0);
				//lat += 0.05 / zoom;
				break;
			case -2: // Down
				//offY -= 10;
				scrollPos.shift(0, -zs);
				//gpsShf.shift(-0.05 / zoom, 0);
				//lat -= 0.05 / zoom;
				break;
			case -3: // Left
				//offX += 10;
				//lon -= 0.05 / zoom;
				scrollPos.shift(-zs, 0);
				//gpsShf.shift(0, -0.05 / zoom);
				break;
			case -4: // Right
				//offX -= 10;
				//lon += 0.05 / zoom;
				scrollPos.shift(zs, 0);
				//gpsShf.shift(0, 0.05 / zoom);
				break;
			case 53: // '5' to zoom in
				if(zoom < 18)
					zoom++;
				break;
			case 48: // '0' to zoom out
				if(zoom > 1)
					zoom--;
				break;
			case 49: // '1' center view on GPS position
				this.scrollPos =
						new Location(this.gpsPos.getLatitude(), this.gpsPos.getLongitude());
				repaint();
				break;
			default:
				System.out.println(keyCode);
		}

		centerTileNumbers = tileNumbers(scrollPos.getX(), scrollPos.getY(), zoom);
		repaint();
	}

	/**
	 * Called when a key is released.
	 */
	protected void keyReleased(int keyCode) {
	}

	/**
	 * Called when a key is repeated (held down).
	 */
	protected void keyRepeated(int keyCode) {
		if(keyCode < 0) {
			keyPressed(keyCode);
		}
	}

	/**
	 * Called when the pointer is dragged.
	 */
	protected void pointerDragged(int x, int y) {
	}

	/**
	 * Called when the pointer is pressed.
	 */
	protected void pointerPressed(int x, int y) {
	}

	/**
	 * Called when the pointer is released.
	 */
	protected void pointerReleased(int x, int y) {
	}

	public void receiveBug(Bug bug) {
		System.out.println("Bug: " + bug.getText());
		this.bugs[this.bugPnt] = bug;
		this.bugPnt = (this.bugPnt + 1) % MAXBUGS;
		repaint();
	}

	/**
	 * Called when action should be handled
	 */
	public void commandAction(Command command, Displayable displayable) {
		if(command.equals(this.cmdExit)) {
			CoronaMIDlet.getInstance().notifyDestroyed();
		} else if(command.equals(this.cmdBugreport)) {
			Display.getDisplay(CoronaMIDlet.getInstance())
					.setCurrent(new ReportMapErrorDialog(this.scrollPos, this));
		} else if(command.equals(this.cmdShowBugs)) {
			float[] rpp = radPerPixel(zoom);
			float xmin = (float)(scrollPos.getX() - rpp[0] * getWidth() / 2);
			float xmax = (float)(scrollPos.getX() + rpp[0] * getWidth() / 2);
			float ymin = (float)(scrollPos.getY() - rpp[1] * getHeight() / 2);
			float ymax = (float)(scrollPos.getY() + rpp[1] * getHeight() / 2);
			OpenStreetBugs.getBugs(xmin, xmax, ymin, ymax, this);
		} else if(command.equals(this.cmdSwitchMap)) {
			if(TileCache.getInstance().getTileSuffix().equals("")) {
				TileCache.getInstance().setTileSuffix("ocm");
				TileCache.getInstance().setTileServer(TileCache.OCM_URL);
			} else {
				TileCache.getInstance().setTileSuffix("");
				TileCache.getInstance().setTileServer(TileCache.OSM_URL);
			}
			repaint();
		} else if(command.equals(this.cmdAddBTGPS)) {
			// Start BLUElet to discover Bluetooth devices
			bluelet = new BLUElet(CoronaMIDlet.getInstance(), this);
			bluelet.startApp();
			bluelet.startInquiry(DiscoveryAgent.GIAC, new UUID[]{new UUID(0x1101)});
			Display.getDisplay(CoronaMIDlet.getInstance()).setCurrent(bluelet.getUI());
		} else if(command.equals(BLUElet.BACK)) {
			bluelet.destroyApp(false);
			Display.getDisplay(CoronaMIDlet.getInstance())
					.setCurrent(CoronaMIDlet.getInstance().getMap());
		} else if (command.equals(BLUElet.COMPLETED)) {
			//RemoteDevice btDev = bluelet.getSelectedDevice();
			ServiceRecord serviceRecord = bluelet.getFirstDiscoveredService();
			if(serviceRecord != null) {
				String url = serviceRecord.getConnectionURL(
						ServiceRecord.NOAUTHENTICATE_NOENCRYPT, false);
				this.gpsPos.attachBTGPS(url);
			} else {
				DebugDialog.getInstance().addMessage("Note", "No BT ServiceRecord found!");
			}
			Display.getDisplay(CoronaMIDlet.getInstance())
					.setCurrent(CoronaMIDlet.getInstance().getMap());
		} else if (command.equals(BLUElet.SELECTED)) {

		} else if(command.equals(this.cmdDebug)) {
			DebugDialog.getInstance().show();
		}
	}
}
