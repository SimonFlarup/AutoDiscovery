package com.simonflarup.autodiscovery.common;

public interface AutoDiscoverySocket {
    /**
     * Registers a callback to be invoked when a device have been discovered. Use this to initiate follow up connections
     * @param callback the callback to register for invoking when a device have been discovered
     * @see #unregisterCallback(AutoDiscoveryCallback)
     * @see AutoDiscoveryPackage
     */
    void registerCallback(AutoDiscoveryCallback callback);

    /**
     * Removes a previous registered callback
     * @param callback the callback to unregister
     * @see #registerCallback(AutoDiscoveryCallback)
     */
    void unregisterCallback(AutoDiscoveryCallback callback);
}
