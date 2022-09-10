package com.simonflarup.autodiscovery.common.utils;

import com.simonflarup.autodiscovery.common.AutoDiscoveryPackage;
import com.simonflarup.autodiscovery.common.impl.AutoDiscoveryPackageImpl;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.ConnectException;
import java.net.Socket;

public class AutoDiscoveryPackageFactory {
    public static AutoDiscoveryPackage generateAutoDiscoveryPackage(BufferedReader in, Socket socket) throws IOException {
        String inputLine;
        while ((inputLine = in.readLine()) != null) {
            if (inputLine.startsWith("AUTODISCOVERY;")) {
                String[] split = inputLine.split(";");
                String displayName = "UNKNOWN";
                if (split.length >= 1) {
                    displayName = split[1];
                }
                return new AutoDiscoveryPackageImpl(displayName, socket.getInetAddress());
            }
        }
        throw new ConnectException("Did not respond correctly");
    }
}
