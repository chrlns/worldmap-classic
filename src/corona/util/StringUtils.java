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

package corona.util;

import java.util.Vector;

/**
 *
 * @author Christian Lins
 */
public final class StringUtils {

	/**
	 * J2ME implementation of String.split().
	 * @param str
	 * @param delim
	 * @return
	 */
	public static String[] split(String str, String delim) {
		Vector cache = new Vector();
		int start = 0;
		int end = 0;
		while(end < str.length()) {
			end = str.indexOf(delim, start);
			if(end == -1) {
				if(start >= str.length()) {
					start = str.length() - 1;
				}
				end = str.length();
			}
			String sub = str.substring(start, end);
			cache.addElement(sub);
			start = end + 1;
		}

		String[] array = new String[cache.size()];
		for(int n = 0; n < array.length; n++) {
			array[n] = (String)cache.elementAt(n);
		}
		return array;
	}
}
