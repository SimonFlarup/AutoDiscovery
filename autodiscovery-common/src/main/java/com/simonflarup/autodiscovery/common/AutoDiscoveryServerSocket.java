package com.simonflarup.autodiscovery.common;

import java.io.IOException;

public interface AutoDiscoveryServerSocket extends AutoDiscoverySocket {
    /**
     * Starts the Auto Discovery Socket in a new thread
     * @see #stop()
     */
    void start();

    /**
     * Interrupts the Socket Thread and stops the Auto Discovery Socket
     * @see #start()
     * @throws IOException If the Socket Thread fails to be interrupted and stopped
     */
    void stop() throws IOException;
}
