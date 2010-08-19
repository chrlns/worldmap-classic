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
package net.benhui.btgallery;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Vector;
import javax.bluetooth.DataElement;
import javax.bluetooth.DeviceClass;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.RemoteDevice;
import javax.bluetooth.ServiceRecord;
import javax.bluetooth.UUID;

/**
 * <p>Title: A utility class to dump JABWT object contents and perform object conversion</p>
 * <p>Description: A collection of print utility method that output the attributes of JABWT objects to System.out
 * Supported objects are: <br>
 * LocalDevice<br>
 * RemoteDevice<br>
 * DeviceClass<br>
 * UUID<br>
 * ServiceRecord<br>
 * DataElement<br>
 * Major Service Class <br>
 * Major, Minor Device Class<br
 * Attribute ID<br>
 * OBEXHeader (incomplete)<br>
 * </p>
 * <p>Copyright: Copyright (c) 2003</p>
 *
 * @author Ben Hui (www.benhui.net)
 * @version 1.0
 *
 * LICENSE:
 * This code is licensed under GPL. (See http://www.gnu.org/copyleft/gpl.html)
 */
public class Util {

	private Util() {
	}

	public static void printRemoteDevice(RemoteDevice dev, DeviceClass devClass) {
		try {
			System.out.println("Print Remote Device " + dev.getBluetoothAddress());
			System.out.println("Name: " + dev.getFriendlyName(false));
			System.out.println("Auth: " + dev.isAuthenticated() + " Encrypted: " + dev.isEncrypted() + " Trusted: " + dev.isTrustedDevice());

			if (devClass != null) {
				System.out.println("MajorDevice:"
						+ majorToName(devClass.getMajorDeviceClass()));
				System.out.println("MinorDevice:"
						+ minorToName(devClass.getMajorDeviceClass(),
						devClass.getMinorDeviceClass()));
				System.out.println("ServiceClass:");
				String[] str = Util.majorServiceToName(devClass.getServiceClasses());
				for (int i = 0; i < str.length; i++) {
					System.out.println("  " + str[i]);
				}
			}
		} catch (IOException e) {
		}
	}

	public static void printLocalDevice(LocalDevice dev) {
		System.out.println("Print Local Device " + dev.getBluetoothAddress());
		System.out.println("Name: " + dev.getFriendlyName());
		DeviceClass devClass = dev.getDeviceClass();
		if (devClass != null) {
			System.out.println("MajorDevice:"
					+ majorToName(devClass.getMajorDeviceClass()));
			System.out.println("MinorDevice:"
					+ minorToName(devClass.getMajorDeviceClass(),
					devClass.getMinorDeviceClass()));
			System.out.println("ServiceClass:");
			String[] str = Util.majorServiceToName(devClass.getServiceClasses());
			for (int i = 0; i < str.length; i++) {
				System.out.println("  " + str[i]);
			}
		}

	}

	public static void printServiceRecord(ServiceRecord r) {
		int[] ids = r.getAttributeIDs();
		System.out.println("Print Service Record (# of element: " + ids.length + ")");
		System.out.println("Print Service Record URL " + r.getConnectionURL(ServiceRecord.NOAUTHENTICATE_NOENCRYPT, false));

		for (int i = 0; i < ids.length; i++) {
			DataElement el = r.getAttributeValue(ids[i]);
			printDataElement(el, ids[i], "");
		}
	}

	public static void printDataElement(DataElement e, int id, String indent) {
		int type = e.getDataType();
		if (type == DataElement.DATALT || type == DataElement.DATSEQ) {
			Enumeration en = (Enumeration) e.getValue();
			System.out.println(indent + "DataElement[" + idToName(id) + "] " + type + " (# of element: " + e.getSize() + ")");
			while (en.hasMoreElements()) {
				DataElement e2 = (DataElement) en.nextElement();
				printDataElement(e2, id, indent + "  ");
			}
		} else if (type == DataElement.U_INT_1 || type == DataElement.U_INT_2 || type == DataElement.U_INT_4
				|| type == DataElement.INT_1 || type == DataElement.INT_2 || type == DataElement.INT_4 || type == DataElement.INT_8) {
			long v = e.getLong();
			System.out.println(indent + "DataElement[" + idToName(id) + "] " + v);
		} else if (type == DataElement.UUID) {
			UUID uuid = (UUID) e.getValue();
			System.out.println(indent + "DataElement[" + idToName(id) + "] " + uuidToName(uuid));
		} else if (type == DataElement.U_INT_8 || type == DataElement.U_INT_16 || type == DataElement.INT_16) {
			byte[] v = (byte[]) e.getValue();
			String s = "";
			for (int i = 0; i < v.length; i++) {
				s += Integer.toHexString((int) v[i]);
			}
			System.out.println(indent + "DataElement[" + idToName(id) + "] " + s);

		} else if (type == DataElement.STRING || type == DataElement.URL) {
			String v = (String) e.getValue();
			System.out.println(indent + "DataElement[" + idToName(id) + "] " + v);

		} else if (type == DataElement.BOOL) {
			boolean v = e.getBoolean();
			System.out.println(indent + "DataElement[" + idToName(id) + "] " + String.valueOf(v));

		} else if (type == DataElement.NULL) {
			System.out.println(indent + "DataElement[" + idToName(id) + "] NULL");

		}

	}

	// convert Attribute  ID to human friendly name
	public static String idToName(int id) {
		if (id == 0x0000) {
			return "ServiceRecordHandle";
		} else if (id == 0x0001) {
			return "ServiceClassIDList";

		} else if (id == 0x0002) {
			return "ServiceRecordState";
		} else if (id == 0x0003) {
			return "ServiceID";
		} else if (id == 0x0004) {
			return "ProtocolDescriptorList";
		} else if (id == 0x0005) {
			return "BrowseGroupList";
		} else if (id == 0x0006) {
			return "LanguageBasedAttributeIDList";
		} else if (id == 0x0007) {
			return "ServiceInfoTimeToLive";
		} else if (id == 0x0008) {
			return "ServiceAvailability";
		} else if (id == 0x0009) {
			return "BluetoothProfileDescriptorList";
		} else if (id == 0x000A) {
			return "DocumentationURL";
		} else if (id == 0x000B) {
			return "ClientExecutableURL";
		} else if (id == 0x000C) {
			return "IconURL";
		} else if (id == 0x000D) {
			return "AdditionalProtocol";
		} else if (id == 0x0100) {
			return "ServiceName";
		} else if (id == 0x0101) {
			return "ServiceDescription";
		} else if (id == 0x0102) {
			return "ProviderName";
		} else if (id == 0x0200) {
			/** @todo why the spec say it is GroupID, IpSubnet and VersionNumberList as well? */
			return "GroupID";
		} else if (id == 0x0201) {
			return "ServiceDatabaseState";

		} else if (id == 0x0300) {
			return "ServiceVersion";
		} else if (id == 0x0301) {
			return "ExternalNetwork";
		} else if (id == 0x0302) {
			// @todo or FaxClass1Support in case of Fax Profile
			return "RemoteAudioVolumeControl";
		} else if (id == 0x0303) {
			// @todo or FaxClass2Support in case of Fax Profile
			return "SupportedFormatList";
		} else if (id == 0x0304) {
			return "FaxClass2Support";
		} else if (id == 0x0305) {
			return "AudioFeedbackSupport";
		} else if (id == 0x0306) {
			return "NetworkAddress";
		} else if (id == 0x0307) {
			return "WAPGateway";
		} else if (id == 0x0308) {
			return "HomePageURL";
		} else if (id == 0x0309) {
			return "WAPStackType";
		} else if (id == 0x030A) {
			return "SecurityDescription";
		} else if (id == 0x030B) {
			return "NetAccessType";
		} else if (id == 0x030C) {
			return "MaxNetAccessrate";
		} else if (id == 0x030D) {
			return "IPv4Subnet";
		} else if (id == 0x030E) {
			return "IPv6Subnet";
		} else if (id == 0x0310) {
			return "SupportedCapabalities";
		} else if (id == 0x0311) {
			return "SupportedFeatures";
		} else if (id == 0x0312) {
			return "SupportedFunctions";
		} else if (id == 0x0313) {
			return "TotalImagingDataCapacity";
		} else {
			return "UnknownAttribute(" + id + ")";
		}
	}

	public static String uuidToName(UUID u) {
		if (u.equals(new UUID(0x0001))) {
			return "SDP";
		} else if (u.equals(new UUID(0x0003))) {
			return "RFCOMM";
		} else if (u.equals(new UUID(0x0008))) {
			return "OBEX";
		} else if (u.equals(new UUID(0x000C))) {
			return "HTTP";
		} else if (u.equals(new UUID(0x0100))) {
			return "L2CAP";
		} else if (u.equals(new UUID(0x000F))) {
			return "BNEP";
		} else if (u.equals(new UUID(0x1000))) {
			return "ServiceDiscoveryServerServiceClassID";
		} else if (u.equals(new UUID(0x1001))) {
			return "BrowseGroupDescriptorCerviceClassID";
		} else if (u.equals(new UUID(0x1002))) {
			return "PublicBrowseGroup";
		} else if (u.equals(new UUID(0x1101))) {
			return "SerialPort";
		} else if (u.equals(new UUID(0x1102))) {
			return "LANAccessUsingPPP";
		} else if (u.equals(new UUID(0x1103))) {
			return "DialupNetworking";
		} else if (u.equals(new UUID(0x1104))) {
			return "IrMCSync";
		} else if (u.equals(new UUID(0x1105))) {
			return "OBEX ObjectPushProfile";
		} else if (u.equals(new UUID(0x1106))) {
			return "OBEX FileTrasnferProfile";
		} else if (u.equals(new UUID(0x1107))) {
			return "IrMCSyncCommand";
		} else if (u.equals(new UUID(0x1108))) {
			return "Headset";
		} else if (u.equals(new UUID(0x1109))) {
			return "CordlessTelephony";
		} else if (u.equals(new UUID(0x110A))) {
			return "AudioSource";
		} else if (u.equals(new UUID(0x1111))) {
			return "Fax";
		} else if (u.equals(new UUID(0x1112))) {
			return "HeadsetAudioGateway";
		} else if (u.equals(new UUID(0x1115))) {
			return "PersonalAreaNetworkingUser";
		} else if (u.equals(new UUID(0x1116))) {
			return "NetworkAccessPoint";
		} else if (u.equals(new UUID(0x1117))) {
			return "GroupNetwork";
		} else if (u.equals(new UUID(0x111E))) {
			return "Handsfree";
		} else if (u.equals(new UUID(0x111F))) {
			return "HandsfreeAudioGateway";
		} else if (u.equals(new UUID(0x1201))) {
			return "GenericNetworking";
		} else if (u.equals(new UUID(0x1202))) {
			return "GenericFileTransfer";
		} else if (u.equals(new UUID(0x1203))) {
			return "GenericAudio";
		} else if (u.equals(new UUID(0x1204))) {
			return "GenericTelephony";
		} else {
			return u.toString();
		}
	}

	public static String majorToName(int d) {
		if (d == 0x0000) {
			return "Miscellaneous";
		} else if (d == 0x0100) {
			return "Computer";
		} else if (d == 0x0200) {
			return "Phone";
		} else if (d == 0x0300) {
			return "LANAccessPoint";
		} else if (d == 0x0400) {
			return "AudioVideo";
		} else if (d == 0x0500) {
			return "Peripheral";
		} else if (d == 0x0600) {
			return "Imaging";
		} else if (d == 0x1F00) {
			return "Uncategorized";
		} else {
			return "UnknownMajorDevice(" + d + ")";
		}
	}

	/**
	 *
	 * @param d major device class
	 * @param m minor device class
	 * @return
	 */
	public static String minorToName(int d, int m) {
		if (d == 0x0000) {
			return "Miscellaneous";
		} else if (d == 0x0100 && m == 0x00) {
			return "Uncategorized";
		} else if (d == 0x0100 && m == 0x04) {
			return "Workstation";
		} else if (d == 0x0100 && m == 0x08) {
			return "Server";
		} else if (d == 0x0100 && m == 0x0C) {
			return "Laptop";
		} else if (d == 0x0100 && m == 0x10) {
			return "HandheldPcPda";
		} else if (d == 0x0100 && m == 0x14) {
			return "PalmPcPda";
		} else if (d == 0x0100 && m == 0x18) {
			return "Wearable";
		} else if (d == 0x0200 && m == 0x00) {
			return "Uncategorized";
		} else if (d == 0x0200 && m == 0x04) {
			return "Cellular";
		} else if (d == 0x0200 && m == 0x08) {
			return "Cordless";
		} else if (d == 0x0200 && m == 0x0C) {
			return "SmartPhone";
		} else if (d == 0x0200 && m == 0x10) {
			return "Modem";
		} else if (d == 0x0200 && m == 0x14) {
			return "ISDN";
		} else if (d == 0x0300 && m == 0x00) {
			return "FullyAvailable";
		} else if (d == 0x0300 && m == 0x20) {
			return "1to17%Utilized";
		} else if (d == 0x0300 && m == 0x40) {
			return "17to33%Utilized";
		} else if (d == 0x0300 && m == 0x60) {
			return "33to50%Utilized";
		} else if (d == 0x0300 && m == 0x80) {
			return "50to67%Utilized";
		} else if (d == 0x0300 && m == 0xA0) {
			return "67to83%Utilized";
		} else if (d == 0x0300 && m == 0xC0) {
			return "83to100%Utilized";
		} else if (d == 0x0300 && m == 0xE0) {
			return "NoServiceAvailable";
		} else if (d == 0x0400 && m == 0x00) {
			return "Uncategorized";
		} else if (d == 0x0400 && m == 0x04) {
			return "Headset";
		} else if (d == 0x0400 && m == 0x08) {
			return "HandsFree";
		} else if (d == 0x0400 && m == 0x0C) {
			return "(Reserved)";
		} else if (d == 0x0400 && m == 0x10) {
			return "Microphone";
		} else if (d == 0x0400 && m == 0x14) {
			return "Loudspeaker";
		} else if (d == 0x0400 && m == 0x18) {
			return "Headphones";
		} else if (d == 0x0400 && m == 0x1C) {
			return "PortableAudio";
		} else if (d == 0x0400 && m == 0x20) {
			return "CarAudio";
		} else if (d == 0x0400 && m == 0x24) {
			return "SetTopBox";
		} else if (d == 0x0400 && m == 0x28) {
			return "HiFiAudioDevice";
		} else if (d == 0x0400 && m == 0x2C) {
			return "VCR";
		} else if (d == 0x0400 && m == 0x30) {
			return "VideoCamera";
		} else if (d == 0x0400 && m == 0x34) {
			return "Camcorder";
		} else if (d == 0x0400 && m == 0x38) {
			return "VideoMonitor";
		} else if (d == 0x0400 && m == 0x3C) {
			return "VideoDisplayAndLoudspeaker";
		} else if (d == 0x0400 && m == 0x40) {
			return "VideoConferencing";
		} else if (d == 0x0400 && m == 0x44) {
			return "(Reserved)";
		} else if (d == 0x0400 && m == 0x48) {
			return "GamingToy";
		} else if (d == 0x0500 && m == 0x00) {
			return "Uncategoried";
		} else if (d == 0x0500 && m == 0x04) {
			return "Joystick";
		} else if (d == 0x0500 && m == 0x08) {
			return "Gamepad";
		} else if (d == 0x0500 && m == 0x0C) {
			return "RemoteControl";
		} else if (d == 0x0500 && m == 0x10) {
			return "SensingDevice";
		} else if (d == 0x0500 && m == 0x14) {
			return "DigitizerTablet";
		} else if (d == 0x0500 && m == 0x18) {
			return "CardReader";
		} else if (d == 0x0500 && m == 0x40) {
			return "Keyboard";
		} else if (d == 0x0500 && m == 0x80) {
			return "PointingDevice";
		} else if (d == 0x0500 && m == 0xC0) {
			return "KeyboardPointingDevice";
		} else if (d == 0x0600 && m == 0x10) {
			return "Display";
		} else if (d == 0x0600 && m == 0x20) {
			return "Camera";
		} else if (d == 0x0600 && m == 0x40) {
			return "Scanner";
		} else if (d == 0x0600 && m == 0x80) {
			return "Printer";
		} else if (d == 0x1F00) {
			return "Uncategorized(" + m + ")";
		} else {
			return "UnknownMinorDevice(" + m + ")";
		}

	}

	public static String[] majorServiceToName(int d) {
		Vector v = new Vector();

		if ((d & 0x2000) > 0) {
			v.addElement("LimitedDiscoverableMode");
		}
		if ((d & 0x10000) > 0) {
			v.addElement("Positioning");
		}
		if ((d & 0x20000) > 0) {
			v.addElement("Networking");
		}
		if ((d & 0x40000) > 0) {
			v.addElement("Rendering");
		}
		if ((d & 0x80000) > 0) {
			v.addElement("Capturing");
		}
		if ((d & 0x100000) > 0) {
			v.addElement("ObjectTransfer");
		}
		if ((d & 0x200000) > 0) {
			v.addElement("Audio");
		}
		if ((d & 0x400000) > 0) {
			v.addElement("Telephony");
		}
		if ((d & 0x800000) > 0) {
			v.addElement("Information");
		}

		String[] str = new String[v.size()];
		v.copyInto(str);
		return str;

	}
}
