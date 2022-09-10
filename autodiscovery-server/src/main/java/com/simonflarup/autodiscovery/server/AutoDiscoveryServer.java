package com.simonflarup.autodiscovery.server;

import com.simonflarup.autodiscovery.common.AutoDiscoveryCallback;
import com.simonflarup.autodiscovery.common.AutoDiscoveryPackage;
import com.simonflarup.autodiscovery.common.AutoDiscoveryServerSocket;
import com.simonflarup.autodiscovery.common.impl.AutoDiscoveryPackageImpl;
import com.simonflarup.autodiscovery.common.utils.AutoDiscoveryPackageFactory;
import com.simonflarup.autodiscovery.common.utils.AutoDiscoverySocketMessageHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Auto Discovery Server
 *
 * The Auto Discovery Server can be used to listen for attempted connection from Auto Discovery Clients.
 * This class starts up a threaded server socket, which accepts input on a specific port (default port 29120)
 * Auto Discovery Clients can be used to automatically attempt to scan the network for Auto Discovery Servers.
 * The resulting exchange of information will identify the server for clients and vice versa.
 */
public class AutoDiscoveryServer implements AutoDiscoveryServerSocket {
    private final Logger logger = Logger.getLogger(AutoDiscoveryServer.class.getName());

    private final int portNumber;
    private final Inet4Address inet4Address;
    private final String displayName;
    private Thread socketThread;
    private ServerSocket socket;

    private List<AutoDiscoveryCallback> callbacks = new ArrayList<>();

    /**
     * @param displayName A display name for identifying the device which is Auto Discoverable
     * @param portNumber The NAT Port Number exposed by this server socket to listen on
     * @param inet4Address A IPv4 address specifying which address to expose this server socket on for listening (For multi-homed devices / devices with multiple IPv4 addresses)
     */
    public AutoDiscoveryServer(String displayName, int portNumber, Inet4Address inet4Address) {
        this.displayName = displayName;
        this.inet4Address = inet4Address;
        this.portNumber = portNumber;
    }

    /**
     * @param displayName A display name for identifying the device which is Auto Discoverable
     * @param portNumber The NAT Port Number exposed by this server socket to listen on
     */
    public AutoDiscoveryServer(String displayName, int portNumber) {
        this(displayName, portNumber, null);
    }

    /**
     * @param displayName A display name for identifying the device which is Auto Discoverable
     */
    public AutoDiscoveryServer(String displayName) {
        this(displayName,29120);
    }

    @Override
    public void registerCallback(AutoDiscoveryCallback callback) {
        this.callbacks.add(callback);
    }

    @Override
    public void unregisterCallback(AutoDiscoveryCallback callback) {
        this.callbacks.remove(callback);
    }

    @Override
    public void start() {
        this.socketThread = new Thread(createServer());
        socketThread.start();
    }

    @Override
    public void stop() throws IOException {
        if (socketThread == null) {
            logger.log(Level.INFO, "Nothing to stop, No socket started");
            return;
        }

        if (!socketThread.isAlive()) {
            logger.log(Level.INFO,"Socket is not alive");
            return;
        }

        if (socket != null) {
            socket.close();
        }

        socketThread.interrupt();

        final CountDownLatch latch = new CountDownLatch(1);
        synchronized (latch) {
            while (!socketThread.isInterrupted()) {
                try {
                    wait(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            latch.countDown();
        }

        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void setupSocket() throws IOException {
        if (inet4Address != null) {
            socket = new ServerSocket(portNumber, 10, inet4Address);
        } else {
            socket = new ServerSocket(portNumber);
        }
    }

    private void listenForMessages() throws IOException {
        try (
                Socket clientSocket = socket.accept();
                PrintWriter out =
                        new PrintWriter(clientSocket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(clientSocket.getInputStream()))
        ) {
            AutoDiscoverySocketMessageHandler.publishAutoDiscoveryMessage(
                    new AutoDiscoveryPackageImpl(displayName, socket.getInetAddress()),
                    out
            );

            handleCallbacks(AutoDiscoveryPackageFactory.generateAutoDiscoveryPackage(in, clientSocket));
        } catch (SocketException ex) {
            if (!"Interrupted function call: accept failed".equals(ex.getMessage())) {
                throw ex;
            }
        }
    }

    private void serverLoop() throws IOException {
        while (true) {
            if (Thread.interrupted() || socket.isClosed()) {
                break;
            }
            listenForMessages();
        }
    }

    private Runnable createServer() {
        return () -> {
            try {
                setupSocket();
                serverLoop();
            } catch (IOException exception) {
                logger.log(Level.WARNING, "Unable to start SocketServer");
                exception.printStackTrace();
            } finally {
                if (socket != null) {
                    try {
                        socket.close();
                    } catch (IOException e) {
                        logger.log(Level.WARNING, "Unable to stop SocketServer");
                        e.printStackTrace();
                    }
                }
            }
        };
    }

    private void handleCallbacks(AutoDiscoveryPackage autoDiscoveryPackage) {
        for (AutoDiscoveryCallback callback : this.callbacks) {
            callback.onPackageReceived(autoDiscoveryPackage);
        }
    }
}
