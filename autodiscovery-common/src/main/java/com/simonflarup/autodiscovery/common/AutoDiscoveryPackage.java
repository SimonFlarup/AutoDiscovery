package com.simonflarup.autodiscovery.common;

import java.net.InetAddress;

/**
 * <b>Auto Discovery Package</b>
 * <br>
 * Contains information about a discovered device.
 * Information can be used for establishing follow-up communication
 */
public interface AutoDiscoveryPackage {
    /**
     * Get the identifying display name for Auto Discovered device
     * @return displayable name of device
     */
    String getDisplayName();

    /**
     * Get the host address of the Auto Discovered device
     * @return Internet Protocol (IP) address of the device which can be used for further communication
     */
    InetAddress getAddress();
}
