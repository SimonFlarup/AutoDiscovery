package com.simonflarup.autodiscovery.common.utils;

import com.simonflarup.autodiscovery.common.AutoDiscoveryPackage;

import java.io.PrintWriter;

public class AutoDiscoverySocketMessageHandler {

    private final static String HEADER = "AUTODISCOVERY";
    private final static String SEPARATOR = ";";

    public static void publishAutoDiscoveryMessage(AutoDiscoveryPackage autoDiscoveryPackage, PrintWriter out) {
        out.println(HEADER + SEPARATOR + autoDiscoveryPackage.getDisplayName());
    }
}
