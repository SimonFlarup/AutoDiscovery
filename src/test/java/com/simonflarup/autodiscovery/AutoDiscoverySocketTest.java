package com.simonflarup.autodiscovery;

import com.simonflarup.autodiscovery.client.AutoDiscoveryClient;
import com.simonflarup.autodiscovery.common.AutoDiscoveryCallback;
import com.simonflarup.autodiscovery.common.AutoDiscoveryPackage;
import com.simonflarup.autodiscovery.server.AutoDiscoveryServer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Duration;
import java.util.concurrent.CountDownLatch;

public class AutoDiscoverySocketTest {
    @Test
    public void testAutoDiscoveryCallbackWithServer() throws UnknownHostException {
        String clientName = "LocalClient";
        String serverName = "LocalTest";
        InetAddress serverAddress = InetAddress.getByName("127.0.0.1");
        AutoDiscoveryClient client = new AutoDiscoveryClient(clientName, 29120);
        client.enableLocalAddresses();
        AutoDiscoveryServer server = new AutoDiscoveryServer(serverName, 29120, (Inet4Address) serverAddress);

        CountDownLatch latch = new CountDownLatch(1);

        AutoDiscoveryCallback callback = new AutoDiscoveryCallback() {
            @Override
            public void onPackageReceived(AutoDiscoveryPackage autoDiscoveryPackage) {
                Assertions.assertEquals(clientName, autoDiscoveryPackage.getDisplayName());
                Assertions.assertEquals(serverAddress, autoDiscoveryPackage.getAddress());
                latch.countDown();
            }
        };
        server.registerCallback(callback);

        server.start();

        try {
            client.search();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        Assertions.assertTimeout(Duration.ofMinutes(1), () -> {
            synchronized (latch) {
                latch.getCount();
            }
            latch.await();
        });
    }

    @Test
    public void testAutoDiscoveryCallbackWithClient() throws UnknownHostException {
        String serverName = "LocalTest";
        InetAddress serverAddress = InetAddress.getByName("127.0.0.2");
        AutoDiscoveryClient client = new AutoDiscoveryClient(29120);
        client.enableLocalAddresses();
        AutoDiscoveryServer server = new AutoDiscoveryServer(serverName, 29120, (Inet4Address) serverAddress);

        CountDownLatch latch = new CountDownLatch(1);

        AutoDiscoveryCallback callback = new AutoDiscoveryCallback() {
            @Override
            public void onPackageReceived(AutoDiscoveryPackage autoDiscoveryPackage) {
                Assertions.assertEquals(serverName, autoDiscoveryPackage.getDisplayName());
                Assertions.assertEquals(serverAddress, autoDiscoveryPackage.getAddress());
                latch.countDown();
            }
        };
        client.registerCallback(callback);

        server.start();

        try {
            client.search();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        Assertions.assertTimeout(Duration.ofMinutes(1), () -> {
            synchronized (latch) {
                latch.getCount();
            }
            latch.await();
        });
    }

    @Test
    @Disabled
    public void testAutoDiscoveryWithRealDevice() throws IOException, InterruptedException {
        AutoDiscoveryClient client = new AutoDiscoveryClient(29120);

        client.search();

        Assertions.assertTimeout(Duration.ofMinutes(1), () -> {
            CountDownLatch latch = new CountDownLatch(1);
            synchronized (latch) {
                latch.getCount();
            }
            latch.await();
        });
    }

    @Test
    @Disabled
    public void testAutoDiscoveryServerWithRealDevice() {
        AutoDiscoveryServer server = new AutoDiscoveryServer("LocalTest");

        server.start();

        Assertions.assertTimeout(Duration.ofMinutes(1), () -> {
            CountDownLatch latch = new CountDownLatch(1);
            synchronized (latch) {
                latch.getCount();
            }
            latch.await();
        });
    }
}
