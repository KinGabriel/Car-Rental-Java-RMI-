package Utilities;

/**
 * The IP class provides a centralized way to store and retrieve the IP address of the server.
 * This allows various parts of the application to access the server IP address consistently.
 */
public class IP {
    /**
     * Static field to hold the server IP address.
     * Being static, this field belongs to the class and is shared across all instances.
     */
    private static String serverIp;

    /**
     * Retrieves the currently set server IP address.
     * @return The server IP address as a {@link String}.
     */
    public static String getServerIp() {
        return serverIp;
    }

    /**
     * Sets the server IP address.
     * This method allows changing the server IP address dynamically at runtime.
     * @param ip The new IP address of the server as a {@link String}.
     */
    public static void setServerIp(String ip) {
        serverIp = ip;
    }
}
