/*
 *  WANDERSMANN - J2ME OpenStreetMap Client
 *  Copyright (C) 2010-2013 Christian Lins <christian@lins.me>
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
 */

package wandersmann.osmbugs;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;

import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;

/**
 * Loads Bug information from OpenStreetBugs.
 * 
 * @author Christian Lins
 */
public class BugLoader extends Thread {

    public static final int   MAX_BUGS     = 10; // Maximum amount of bugs to be
                                                 // loaded at once

    private final float       xmin, xmax, ymin, ymax;
    private final BugReceiver rec;
    private int               receivedBugs = 0;

    public BugLoader(float xmin, float xmax, float ymin, float ymax, BugReceiver rec) {
        setPriority(MIN_PRIORITY);
        this.xmin = xmin;
        this.xmax = xmax;
        this.ymin = ymin;
        this.ymax = ymax;
        this.rec = rec;
    }

    public void run() {
        // Call to
        // /api/0.1/getBugs?b=36.17496&t=61.03797&l=-9.9793&r=31.54902&ucid=1
        // Reply is something like: putAJAXMarker(552542, 6.971592, 50.810296,
        // 'Strassensystem auf Friedhof fehlt [TobiR, 2010-08-09 23:30:37
        // CEST]', 0);
        try {
            String url = OpenStreetBugs.API_URL + "getBugs?b=" + ymin + "&t=" + ymax + "&l=" + xmin
                    + "&r=" + xmax;
            HttpConnection httpConn = (HttpConnection) Connector.open(url);
            DataInputStream in = httpConn.openDataInputStream();
            ByteArrayOutputStream buf = new ByteArrayOutputStream();

            int openbrackets = 0;
            for (int b = in.read(); b != -1; b = in.read()) {
                char c = (char) b;
                if (c == ')') {
                    openbrackets--;
                    if (openbrackets == 0) {
                        Bug bug = Bug.parse(buf.toString());
                        buf.reset();
                        if (bug != null) {
                            rec.receiveBug(bug);
                            if (++receivedBugs >= MAX_BUGS) {
                                break;
                            }
                        }
                    }
                } else if (c == '(') {
                    openbrackets++;
                } else if (openbrackets > 0) { // only store bytes between '('
                                               // ')'
                    buf.write(b);
                }
            }

            in.close();
            httpConn.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

}
