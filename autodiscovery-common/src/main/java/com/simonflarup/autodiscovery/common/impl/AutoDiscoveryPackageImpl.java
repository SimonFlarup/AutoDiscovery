package com.simonflarup.autodiscovery.common.impl;

import com.simonflarup.autodiscovery.common.AutoDiscoveryPackage;

import java.net.InetAddress;

public class AutoDiscoveryPackageImpl implements AutoDiscoveryPackage {
    private final String displayName;
    private final InetAddress hostAddress;

    public AutoDiscoveryPackageImpl(String displayName, InetAddress hostAddress) {
        this.displayName = displayName;
        this.hostAddress = hostAddress;
    }

    public AutoDiscoveryPackageImpl(InetAddress hostAddress) {
        this("ANONYMOUS", hostAddress);
    }

    @Override
    public String getDisplayName() {
        return this.displayName;
    }

    @Override
    public InetAddress getAddress() {
        return this.hostAddress;
    }
}
