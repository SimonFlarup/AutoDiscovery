package com.simonflarup.autodiscovery.common;

public interface AutoDiscoveryCallback {
    /**
     * Invoked when a device has been discovered. Will provide a {@code AutoDiscoveryPackage}
     * @param autoDiscoveryPackage object containing information about discovered device
     * @see AutoDiscoverySocket
     * @see AutoDiscoveryPackage
     */
    void onPackageReceived(AutoDiscoveryPackage autoDiscoveryPackage);
}
