package com.simonflarup.autodiscovery.client;

import org.apache.commons.net.util.SubnetUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

class SubnetUtilsIteratorTest {

    @Test
    public void alternatingNext() {
        SubnetUtilsIterator iterator = new SubnetUtilsIterator(new SubnetUtils("192.168.0.10/24"));

        Assertions.assertTrue(iterator.hasNext());
        Assertions.assertEquals("192.168.0.10", iterator.next());
        Assertions.assertTrue(iterator.hasNext());
        Assertions.assertEquals("192.168.0.9", iterator.next());
        Assertions.assertTrue(iterator.hasNext());
        Assertions.assertEquals("192.168.0.11", iterator.next());
    }

    @Test
    public void overflow() {
        SubnetUtilsIterator iterator = new SubnetUtilsIterator(new SubnetUtils("192.168.0.1/24"));

        Assertions.assertTrue(iterator.hasNext());
        Assertions.assertEquals("192.168.0.1", iterator.next());
        Assertions.assertTrue(iterator.hasNext());
        Assertions.assertEquals("192.168.0.254", iterator.next());
    }

    @Test
    public void nonColliding() {
        SubnetUtilsIterator iterator = new SubnetUtilsIterator(new SubnetUtils("192.168.0.4/29"));

        Assertions.assertTrue(iterator.hasNext());
        Assertions.assertEquals("192.168.0.4", iterator.next());
        Assertions.assertTrue(iterator.hasNext());
        Assertions.assertEquals("192.168.0.3", iterator.next());
        Assertions.assertTrue(iterator.hasNext());
        Assertions.assertEquals("192.168.0.5", iterator.next());
        Assertions.assertTrue(iterator.hasNext());
        Assertions.assertEquals("192.168.0.2", iterator.next());
        Assertions.assertTrue(iterator.hasNext());
        Assertions.assertEquals("192.168.0.6", iterator.next());
        Assertions.assertTrue(iterator.hasNext());
        Assertions.assertEquals("192.168.0.1", iterator.next());
        Assertions.assertFalse(iterator.hasNext());
    }

    @Test
    public void nonCollidingOverflow() {
        SubnetUtilsIterator iterator = new SubnetUtilsIterator(new SubnetUtils("192.168.0.3/29"));

        Assertions.assertTrue(iterator.hasNext());
        Assertions.assertEquals("192.168.0.3", iterator.next());
        Assertions.assertTrue(iterator.hasNext());
        Assertions.assertEquals("192.168.0.2", iterator.next());
        Assertions.assertTrue(iterator.hasNext());
        Assertions.assertEquals("192.168.0.4", iterator.next());
        Assertions.assertTrue(iterator.hasNext());
        Assertions.assertEquals("192.168.0.1", iterator.next());
        Assertions.assertTrue(iterator.hasNext());
        Assertions.assertEquals("192.168.0.5", iterator.next());
        Assertions.assertTrue(iterator.hasNext());
        Assertions.assertEquals("192.168.0.6", iterator.next());
        Assertions.assertFalse(iterator.hasNext());
    }

    @Test
    @Disabled
    public void nonCollidingOverflowLarge() {
        SubnetUtils subnetUtils = new SubnetUtils("192.168.0.3/16");
        SubnetUtilsIterator iterator = new SubnetUtilsIterator(subnetUtils);

        long addressesCount = subnetUtils.getInfo().getAddressCountLong();

        List<String> addresses = new ArrayList<>();

        for (int i = 0; i < addressesCount; i++) {
            Assertions.assertTrue(iterator.hasNext());
            addresses.add(iterator.next());
        }
        Assertions.assertFalse(iterator.hasNext());

        for (String address: addresses) {
            int collisions = 0;
            for (String comparedAddress: addresses) {
                if (address.equals(comparedAddress)) {
                    collisions++;
                }
            }
            Assertions.assertEquals(1, collisions, "Address found more than once by iterator");
        }
    }
}