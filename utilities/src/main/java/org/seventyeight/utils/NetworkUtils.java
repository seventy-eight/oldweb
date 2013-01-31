package org.seventyeight.utils;

import java.net.NetworkInterface;
import java.net.SocketException;

/**
 * @author cwolfgang
 *         Date: 31-01-13
 *         Time: 22:36
 */
public class NetworkUtils {
    private NetworkUtils() {

    }

    public static String getNetworkIdentity() {
        try {
            NetworkInterface ni = null;
            while( NetworkInterface.getNetworkInterfaces().hasMoreElements() ) {
                ni = NetworkInterface.getNetworkInterfaces().nextElement();
                break;
            }
            return new String( ni.getHardwareAddress() );
        } catch ( Exception e ) {
            return null;
        }
    }
}
