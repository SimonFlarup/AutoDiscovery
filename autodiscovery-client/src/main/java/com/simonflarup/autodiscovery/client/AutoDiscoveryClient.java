package com.simonflarup.autodiscovery.client;

import com.simonflarup.autodiscovery.common.AutoDiscoveryCallback;
import com.simonflarup.autodiscovery.common.AutoDiscoveryClientSocket;
import com.simonflarup.autodiscovery.common.AutoDiscoveryPackage;
import com.simonflarup.autodiscovery.common.impl.AutoDiscoveryPackageImpl;
import com.simonflarup.autodiscovery.common.utils.AutoDiscoveryPackageFactory;
import com.simonflarup.autodiscovery.common.utils.AutoDiscoverySocketMessageHandler;
import org.apache.commons.net.util.SubnetUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;

// Consider implementing a possible speed improvement by half-connections in tcps: https://nmap.org/nmap_doc.html
// You need to use raw sockets not available in plain java.
// TCP SYN scanning
// Also consider a multicast solution with this as fallback for devices that do not support multicast

public class AutoDiscoveryClient implements AutoDiscoveryClientSocket {
    private final Logger logger = Logger.getLogger(AutoDiscoveryClient.class.getName());

    private final String displayName;
    private final int portNumber;

    private boolean allowLocal = false;
    private boolean allowIPV6 = false;

    private List<AutoDiscoveryCallback> callbacks = new ArrayList<>();

    public AutoDiscoveryClient(String displayName, int portNumber) {
        this.displayName = displayName;
        this.portNumber = portNumber;
    }

    public AutoDiscoveryClient(int portNumber) {
        this("ANONYMOUS", portNumber);
    }

    public AutoDiscoveryClient() {
        this(29120);
    }

    public void enableLocalAddresses() {
        allowLocal = true;
    }

    public void disableLocalAddresses() {
        allowLocal = false;
    }

    @Override
    public void registerCallback(AutoDiscoveryCallback callback) {
        this.callbacks.add(callback);
    }

    @Override
    public void unregisterCallback(AutoDiscoveryCallback callback) {
        this.callbacks.remove(callback);
    }

    public void search() throws IOException, InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        ArrayBlockingQueue<Runnable> boundedQueue = new ArrayBlockingQueue<>(1024);
        ExecutorService executor = new ThreadPoolExecutor(256, 256, 10, TimeUnit.SECONDS, boundedQueue, new ThreadPoolExecutor.AbortPolicy());
        for (InterfaceAddress interfaceAddress : getNetworks()) {
            if (latch.getCount() == 0) {
                break;
            }
            try {
                for (Iterator<String> it = getAddress(interfaceAddress); it.hasNext(); ) {
                    if (latch.getCount() == 0) {
                        break;
                    }
                    String address = it.next();
                    try {
                        InetAddress inetAddress = InetAddress.getByName(address);
                        scheduleThread(inetAddress, executor, latch);
                    } catch (UnknownHostException ex) {
                        //Intentionally ignored
                    }
                }
            } catch (Exception ex) {
                // Intentional ignored
            }
        }
        latch.await(1, TimeUnit.MINUTES);
        executor.shutdownNow();
    }

    private void scheduleThread(InetAddress address, ExecutorService executor, CountDownLatch latch) throws InterruptedException {
        try {
            executor.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        poke(address);
                        latch.countDown();
                    } catch (ConnectException ex) {
                        //Intentional skipped
                    } catch (IOException e) {
                        if ("Host unreachable".equals(e.getMessage())) {
                            return;
                        }
                        logger.log(Level.SEVERE,"IOException: " + e.getMessage() + " | " + address.getHostAddress());
                    }
                }
            });
        } catch (RejectedExecutionException ex) {
            CountDownLatch waitLatch = new CountDownLatch(1);
            waitLatch.await(1, TimeUnit.SECONDS);
            scheduleThread(address, executor, latch);
        }
    }

    //This may experience IOException: Connection Reset, before it manages to read anything. The connection has been established, so try to handle this case, where it still discovers it correctly.
    private void poke(InetAddress address) throws IOException {
        try (
                Socket socket = new Socket(address, portNumber);
                PrintWriter out =
                        new PrintWriter(socket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(socket.getInputStream()))
        ) {
            AutoDiscoverySocketMessageHandler.publishAutoDiscoveryMessage(
                    new AutoDiscoveryPackageImpl(displayName, socket.getInetAddress()),
                    out
            );

            handleCallback(AutoDiscoveryPackageFactory.generateAutoDiscoveryPackage(in, socket));
        }
    }

    private List<InterfaceAddress> getNetworks() throws SocketException {
        List<InterfaceAddress> addressList = new ArrayList<>();

        Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
        for (NetworkInterface netint : Collections.list(nets)) {
            addressList.addAll(netint.getInterfaceAddresses());
        }
        return addressList;
    }

    private Iterator<String> getAddress(InterfaceAddress interfaceAddress) {
        if (interfaceAddress.getAddress().isAnyLocalAddress() || interfaceAddress.getAddress().isLoopbackAddress()) {
            logger.log(Level.WARNING,"LOCAL ADDRESS | " + interfaceAddress.getAddress().getHostAddress());
            checkLocalAllowed();
        }

        if (interfaceAddress.getAddress() instanceof Inet6Address) {
            logger.log(Level.WARNING, "IPV6 | " + interfaceAddress.getAddress().getHostAddress());
            checkIPV6Allowed();
        }

        short netmask = interfaceAddress.getNetworkPrefixLength();
        if (netmask < 24) {
            logger.log(Level.INFO, "Netmask below /24 are not supported due to time constraints, most consumer networks are /24 networks. Attempting scan assuming a /24 netmask instead");
            netmask = 24;
        }
        return new SubnetUtilsIterator(new SubnetUtils(interfaceAddress.getAddress().getHostAddress() + "/" + netmask));
    }

    private void checkLocalAllowed() {
        if (!this.allowLocal) {
            throw new RuntimeException("Local addresses not allowed");
        }
    }

    private void checkIPV6Allowed() {
        throw new RuntimeException("IPV6 not allowed");
    }

    private void handleCallback(AutoDiscoveryPackage autoDiscoveryPackage) {
        for (AutoDiscoveryCallback callback : this.callbacks) {
            callback.onPackageReceived(autoDiscoveryPackage);
        }
    }
}
