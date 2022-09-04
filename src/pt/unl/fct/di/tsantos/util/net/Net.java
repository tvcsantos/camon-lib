package pt.unl.fct.di.tsantos.util.net;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * This class provides utilities for internet, such
 * us obtaining the firt usable ip address and the public ip address
 * if the network is private.
 *
 * <dl><dt><b>Changes:</b></dt>
 * <dt><b>version 1.0 to version 1.2</b></dt>
 * <dd>Added broadcast methods.
 * Code from <a href="http://kickjava.com/src/org/alfresco/filesys/netbios/NetworkSettings.java.htm">NetworkSettings</a>
 * </dd></dl>
 * 
 * @author Tiago Santos
 *
 * @since JDK1.6
 * @version 1.2
 */
public class Net {

    /** Can't instantiate this class */
    private Net() {}

    /**
     * Get the first 'usable' ip address
     * @return a String representation of the first
     * non-loopback non-ipv6 address
     */
    public static String getFirstIP() {
        java.util.Enumeration<java.net.NetworkInterface> ifaces = null;
       
        try {
            ifaces = java.net.NetworkInterface.getNetworkInterfaces();
        } catch (java.net.SocketException e) { }
       
        for (; ifaces.hasMoreElements();) {
            java.util.Enumeration<java.net.InetAddress> addrs =
                    ifaces.nextElement().getInetAddresses();
           
            for (; addrs.hasMoreElements(); ) {
                java.net.InetAddress addr = addrs.nextElement();
                if (!addr.isLoopbackAddress() &&
                        !(addr instanceof java.net.Inet6Address))
                    return addr.getHostAddress();
            }
           
        }
        return "127.0.0.1";
    }

    /**
     * Get the public ip address if the network is private. If some
     * error occur returns the localhost (127.0.0.1)
     * @return the public ip address
     */
    public static String getPublicIP() {
        try {
            java.net.URL url = new java.net.URL("http://www.whatismyip.org/");
            java.net.HttpURLConnection conn =
                    (java.net.HttpURLConnection) url.openConnection();
            java.io.InputStream is = conn.getInputStream();
            java.io.InputStreamReader isr = new java.io.InputStreamReader(is);
            java.io.BufferedReader br = new java.io.BufferedReader(isr);
            return br.readLine();
        } catch (Exception ex) {
            return "127.0.0.1";
        }
    }
    
    /** Broadcast mask for broadcast messages **/
    private static String m_broadcastMask;
    /** Domain name/workgroup that this node is part of **/
    private static String m_domain;
    /** Subnet mask address **/
    private static InetAddress m_subnetAddr;

    /**
     * Determine the boradcast mask from the local hosts TCP/IP address
     * @param addr TCP/IP address to set the broadcast mask for, in
     * 'nnn.nnn.nnn.nnn' format.
     * @return the broadcast mask from the local hosts TCP/IP address
     * @throws java.net.UnknownHostException
     */
    public static String generateBroadcastMask(String addr)
            throws java.net.UnknownHostException {

        // Check if the broadcast mask has already been set

        if (m_broadcastMask != null) {
            return m_broadcastMask;
        }

        // Set the TCP/IP address string
        String localIP = addr;
        if (localIP == null) {
            localIP = Net.getFirstIP();
        }

        // Find the location of the first dot in the TCP/IP address

        int dotPos = localIP.indexOf('.');
        if (dotPos != -1) {

            // Extract the leading IP address value

            String ipStr = localIP.substring(0, dotPos);
            int ipVal = Integer.valueOf(ipStr).intValue();

            // Determine the broadcast mask to use

            if (ipVal <= 127) {

                // Class A address

                m_broadcastMask = "" + ipVal + ".255.255.255";
            } else if (ipVal <= 191) {

                // Class B adddress

                dotPos++;
                while (localIP.charAt(dotPos) != '.' && dotPos < localIP.length()) {
                    dotPos++;
                }

                if (dotPos < localIP.length()) {
                    m_broadcastMask = localIP.substring(0, dotPos) + ".255.255";
                }
            } else if (ipVal <= 223) {

                // Class C address

                dotPos++;
                int dotCnt = 1;
                while (dotCnt < 3 && dotPos < localIP.length()) {

                    // Check if the current character is a dot

                    if (localIP.charAt(dotPos++) == '.') {
                        dotCnt++;
                    }
                }

                if (dotPos < localIP.length()) {
                    m_broadcastMask = localIP.substring(0, dotPos - 1) + ".255";
                }
            }
        }

        // Check if the broadcast mask has been set, if not then use a general
        // broadcast mask

        if (m_broadcastMask == null) {

            // Invalid TCP/IP address string format, use a general broadcast
            // mask
            // for now.

            m_broadcastMask = "255.255.255.255";
        }

        // Return the broadcast mask string

        return m_broadcastMask;
    }

    /**
     * Return the broadcast mask as an address.
     * @return the broadcast mask as an address
     * @throws java.net.UnknownHostException
     */
    public final static InetAddress getBroadcastAddress()
            throws java.net.UnknownHostException {

        // Check if the subnet address is valid

        if (m_subnetAddr == null) {

            // Generate the subnet mask

            String subnet = generateBroadcastMask(null);
            m_subnetAddr = InetAddress.getByName(subnet);
        }

        // Return the subnet mask address

        return m_subnetAddr;
    }

    /**
     * Get the broadcast mask.
     * @return returns the broadcast mask.
     */
    public static String getBroadcastMask() {
        return m_broadcastMask;
    }

    /**
     * Get the local domain/workgroup name.
     * @return the domain/workgroup name
     */
    public static String getDomain() {
        return m_domain;
    }

    /**
     * Determine if the broadcast mask has been setup.
     * @return true if the broadcast mask has been setup otherwise false
     */
    public static boolean hasBroadcastMask() {
        if (m_broadcastMask == null) {
            return false;
        }
        return true;
    }

    /**
     * Set the broadcast mask to be used for broadcast packets.
     * @param mask the broadcast mask to be used for broadcast packets
     */
    public static void setBroadcastMask(String mask) {
        m_broadcastMask = mask;
    }

    /**
     * Set the local domain/workgroup name.
     * @param domain the local domain/workgroup name
     */
    public static void setDomain(String domain) {
        m_domain = domain;
    }
}
