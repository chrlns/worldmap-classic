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
import wandersmann.io.Location;
import wandersmann.io.TileCache;
import wandersmann.io.TileCacheManager;
import wandersmann.io.TileLoadingObserver;
import wandersmann.osmbugs.Bug;
import wandersmann.osmbugs.BugReceiver;
import wandersmann.osmbugs.OpenStreetBugs;
import wandersmann.util.Config;
import wandersmann.util.Math2;

/**
 * Main draw canvas (Map).
 * 
 * @author Christian Lins
 */
public class Map extends Canvas implements CommandListener, BugReceiver, TileLoadingObserver {

    public static final int         MAXBUGS            = 32;

    private final Command           cmdHelp            = new Command("Help", Command.ITEM, 0);
    private final Command           cmdExit            = new Command("Exit", Command.EXIT, 1);
    private final Command           cmdBugreport       = new Command("Map Error",
                                                               "Report Map Error", Command.ITEM, 1);
    private final Command           cmdShowBugs        = new Command("Show Bugs", "Show Map Bugs",
                                                               Command.ITEM, 1);
    private final Command           cmdSwitchCycleMap  = new Command("Cycle Map",
                                                               "Switch to OpenCycleMap",
                                                               Command.ITEM, 1);
    private final Command           cmdSwitchStreetMap = new Command("Street Map",
                                                               "Switch to OpenStreetMap",
                                                               Command.ITEM, 1);
    private final Command           cmdFollow          = new Command("Follow", "Follow position",
                                                               Command.ITEM, 1);
    private final Command           cmdUnfollow        = new Command("Unfollow",
                                                               "Unfollow position", Command.ITEM, 1);
    private final Command           cmdAddBTGPS        = new Command("Blue. GPS",
                                                               "Attach Bluetooth GPS",
                                                               Command.ITEM, 1);
    private final Command           cmdDebug           = new Command("Debug", Command.ITEM, 10);
    private final Command           cmdAbout           = new Command("About", Command.HELP, 1);
    private boolean                 follow             = false;
    private int                     zoom               = 1;
    private int[]                   centerTileNumbers;
    private final Location          gpsPos;
    private Location                scrollPos;
    private BLUElet                 bluelet            = null;
    private final Bug[]             bugs               = new Bug[MAXBUGS];
    private int                     bugPnt             = 0;
    private int                     mapSource          = TileCache.SOURCE_OPENSTREETMAP;
    private final WandersmannMIDlet midlet;

    /**
     * Map constructor.
     */
    public Map(WandersmannMIDlet midlet) {
        this.midlet = midlet;
        this.gpsPos = new Location(midlet);
        try {
            // Set up this canvas to listen to command events
            setCommandListener(this);

            addCommand(cmdExit);

            addCommand(cmdBugreport);
            addCommand(cmdShowBugs);
            addCommand(cmdSwitchCycleMap);
            addCommand(cmdFollow);
            addCommand(cmdAddBTGPS);
            addCommand(cmdDebug);
            addCommand(cmdAbout);
            addCommand(cmdHelp);

            float x = midlet.getConfig().get(Config.POS_X, gpsPos.getX());
            float y = midlet.getConfig().get(Config.POS_Y, gpsPos.getY());
            scrollPos = new Location(x, y);
            this.zoom = midlet.getConfig().get(Config.ZOOM, zoom);
            String lastMapType = midlet.getConfig().get(Config.LASTMAPTYPE, "osm");
            if (lastMapType.equals("ocm")) { // OpenCycleMap
                removeCommand(this.cmdSwitchCycleMap);
                addCommand(this.cmdSwitchStreetMap);
                mapSource = TileCache.SOURCE_OPENCYCLEMAP;
            }

            centerTileNumbers = Math2.tileNumbers(scrollPos.getX(), scrollPos.getY(), zoom);
            this.gpsPos.enableUpdateTimer(5);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void drawImage(Graphics g, int x, int y, int offX, int offY) throws IOException {
        Image img = TileCacheManager.loadImage(zoom, x, y, mapSource, null);
        if (img != null) {
            g.drawImage(img, offX, offY, Graphics.TOP | Graphics.LEFT);
        } else {
            // If img IS null, then a repaint() is later called
            g.setColor(0, 0, 0);
            g.drawString("Loading...", offX, offY, Graphics.TOP | Graphics.LEFT);

            TileCacheManager.loadImage(zoom, x, y, mapSource, this);
        }
    }

    private void drawGPSDot(Graphics g, int x, int y) {
        if (this.gpsPos.hasLocationProvider()) {
            for (int n = 2; n < zoom * 8 / this.gpsPos.getSatellites(); n += 6) {
                g.setColor(0, 25, 255);
                g.drawArc(x - (n >> 1), y - (n >> 1), n, n, 0, 360);
            }
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
            int offX = -tileNumbers[2] + getWidth() / 2; // We want to transform
                                                         // the origin to the
                                                         // center
            int offY = -tileNumbers[3] + getHeight() / 2; // of the screen

            System.out.println(offX + " " + offY);

            // Draw center image
            drawImage(g, tileNumbers[0], tileNumbers[1], offX, offY);

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
            for (int n = 0; n < MAXBUGS; n++) {
                Bug bug = this.bugs[n];
                if (bug != null) {
                    // Draw it! Now!
                    int[] pos = posOnScreen(bug.getX(), bug.getY());
                    g.fillArc(pos[0] - 3, pos[1] - 3, 6, 6, 0, 360);
                }
            }

            // Draw GPS position
            this.gpsPos.updateLocation();
            int[] pos = posOnScreen(gpsPos.getX(), gpsPos.getY());
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
            g.drawString(scrollPosStr, 1, getHeight() - 1, Graphics.BOTTOM | Graphics.LEFT);

            // Draw GPS-Status

        } catch (Exception ex) {
            ex.printStackTrace();
            midlet.getDebugDialog().addMessage("Exception", ex.getMessage());
            g.setColor(255, 0, 0);
            g.drawString("An error occurred", 0, 30, Graphics.TOP | Graphics.LEFT);
        }
    }

    private int[] posOnScreen(float lon, float lat) {
        int[] tileNumbers = centerTileNumbers;
        int offX = -tileNumbers[2] + getWidth() / 2; // We want to transform the
                                                     // origin to the center
        int offY = -tileNumbers[3] + getHeight() / 2; // of the screen
        int[] pos = Math2.tileNumbers(lon, lat, zoom);
        int x = (pos[0] - tileNumbers[0]) * 256 + (pos[2] + offX);
        int y = (pos[1] - tileNumbers[1]) * 256 + (pos[3] + offY);
        return new int[] { x, y };
    }

    /**
     * Called when a key is pressed.
     */
    protected void keyPressed(int keyCode) {
        float zs = 10.0f / (1 << zoom);
        switch (keyCode) {
        case -1: // Up
        case 50:
            // offY += 10;
            scrollPos.shift(0, zs);
            // gpsShf.shift(0.05 / zoom, 0);
            // lat += 0.05 / zoom;
            break;
        case -2: // Down
        case 56:
            // offY -= 10;
            scrollPos.shift(0, -zs);
            // gpsShf.shift(-0.05 / zoom, 0);
            // lat -= 0.05 / zoom;
            break;
        case -3: // Left
        case 52:
            // offX += 10;
            // lon -= 0.05 / zoom;
            scrollPos.shift(-zs, 0);
            // gpsShf.shift(0, -0.05 / zoom);
            break;
        case -4: // Right
        case 54:
            // offX -= 10;
            // lon += 0.05 / zoom;
            scrollPos.shift(zs, 0);
            // gpsShf.shift(0, 0.05 / zoom);
            break;
        case -5: // ENTER
        case 53: // '5' to zoom in
            if (zoom < 18)
                zoom++;
            break;
        case 48: // '0' to zoom out
            if (zoom > 1)
                zoom--;
            break;
        case 49: // '1' center view on GPS position
            // this.scrollPos =
            // new Location(this.gpsPos.getX(), this.gpsPos.getY());
            repaint();
            break;
        default:
            System.out.println(keyCode);
        }

        centerTileNumbers = Math2.tileNumbers(scrollPos.getX(), scrollPos.getY(), zoom);
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
        if (keyCode < 0) {
            keyPressed(keyCode);
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

    public void shutdown() {
        midlet.getConfig().set(Config.POS_X, scrollPos.getX());
        midlet.getConfig().set(Config.POS_Y, scrollPos.getY());
        midlet.getConfig().set(Config.ZOOM, zoom);
        TileCacheManager.shutdown();
    }

    /**
     * Called when action should be handled
     */
    public void commandAction(Command command, Displayable displayable) {
        if (command.equals(this.cmdExit)) {
            midlet.destroyApp(false);
            midlet.notifyDestroyed();
        } else if (command.equals(this.cmdBugreport)) {
            Display.getDisplay(midlet).setCurrent(
                    new ReportMapErrorDialog(midlet, this.scrollPos, this));
        } else if (command.equals(this.cmdShowBugs)) {
            float[] rpp = Math2.radPerPixel(zoom);
            float xmin = (scrollPos.getX() - rpp[0] * getWidth() / 2);
            float xmax = (scrollPos.getX() + rpp[0] * getWidth() / 2);
            float ymin = (scrollPos.getY() - rpp[1] * getHeight() / 2);
            float ymax = (scrollPos.getY() + rpp[1] * getHeight() / 2);
            OpenStreetBugs.getBugs(xmin, xmax, ymin, ymax, this);
        } else if (command.equals(this.cmdSwitchStreetMap)) {
            removeCommand(this.cmdSwitchStreetMap);
            addCommand(this.cmdSwitchCycleMap);
            mapSource = TileCache.SOURCE_OPENSTREETMAP;
            repaint();
            midlet.getConfig().set(Config.LASTMAPTYPE, "osm");
        } else if (command.equals(this.cmdSwitchCycleMap)) {
            removeCommand(this.cmdSwitchCycleMap);
            addCommand(this.cmdSwitchStreetMap);
            mapSource = TileCache.SOURCE_OPENCYCLEMAP;
            repaint();
            midlet.getConfig().set(Config.LASTMAPTYPE, "ocm");
        } else if (command.equals(this.cmdFollow)) {
            removeCommand(cmdFollow);
            addCommand(cmdUnfollow);
            follow = true;
        } else if (command.equals(this.cmdUnfollow)) {
            removeCommand(cmdUnfollow);
            addCommand(cmdFollow);
            follow = false;
        } else if (command.equals(this.cmdAddBTGPS)) {
            // Start BLUElet to discover Bluetooth devices
            bluelet = new BLUElet(midlet, this);
            bluelet.startApp();
            bluelet.startInquiry(DiscoveryAgent.GIAC, new UUID[] { new UUID(0x1101) });
            Display.getDisplay(midlet).setCurrent(bluelet.getUI());
        } else if (command.equals(BLUElet.BACK)) {
            bluelet.destroyApp(false);
            Display.getDisplay(midlet).setCurrent(midlet.getMap());
        } else if (command.equals(BLUElet.COMPLETED)) {
            // RemoteDevice btDev = bluelet.getSelectedDevice();
            ServiceRecord serviceRecord = bluelet.getFirstDiscoveredService();
            if (serviceRecord != null) {
                String url = serviceRecord.getConnectionURL(ServiceRecord.NOAUTHENTICATE_NOENCRYPT,
                        false);
                this.gpsPos.attachBTGPS(url);
            } else {
                midlet.getDebugDialog().addMessage("Note", "No BT ServiceRecord found!");
            }
            Display.getDisplay(midlet).setCurrent(midlet.getMap());
        } else if (command.equals(BLUElet.SELECTED)) {

        } else if (command.equals(this.cmdDebug)) {
            midlet.getDebugDialog().show();
        } else if (command.equals(this.cmdAbout)) {
            Display.getDisplay(midlet).setCurrent(new AboutForm(midlet));
        } else if (command.equals(cmdHelp)) {
            Display.getDisplay(midlet).setCurrent(new HelpForm(midlet));
        }
    }

    public void tileLoaded(Image img, int zoom, int x, int y, int mapSource, byte[] raw) {
        if (img != null) {
            repaint();
        }
    }

    /**
     * Called by a timer in Location class when the location changes. Redraws
     * this Map.
     */
    public void locationUpdated() {
        if (this.follow) {
            keyPressed(49); // Press '1'
        }
        repaint();
    }

}
