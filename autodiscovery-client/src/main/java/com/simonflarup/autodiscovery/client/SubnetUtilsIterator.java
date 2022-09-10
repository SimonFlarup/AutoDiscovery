package com.simonflarup.autodiscovery.client;

import org.apache.commons.net.util.SubnetUtils;

import java.util.Iterator;

public class SubnetUtilsIterator implements Iterator<String> {
    private SubnetUtils currentAddress;
    private SubnetUtils currentTopAddress;
    private SubnetUtils currentBottomAddress;
    private boolean alternator;


    public SubnetUtilsIterator(SubnetUtils subnetUtils) {
        this.currentAddress = subnetUtils;
        alternator = true;
        currentBottomAddress = currentAddress;
        currentTopAddress = currentAddress;
    }

    @Override
    public boolean hasNext() {
        return currentAddress != null;
    }

    @Override
    public String next() {
        String returnString = currentAddress.getInfo().getAddress();

        SubnetUtils next;
        if (alternator) {
            if (checkLow(currentBottomAddress)) {
                next = overflowToHigh();
            } else {
                next = currentBottomAddress.getPrevious();
            }
            currentBottomAddress = next;
            alternator = false;
        } else {
            if (checkHigh(currentTopAddress)) {
                next = overflowToLow();
            } else {
                next = currentTopAddress.getNext();
            }
            currentTopAddress = next;
            alternator = true;
        }

        if (currentAddress.getInfo().getAddress().equals(next.getInfo().getAddress())) {
            next = null;
        }

        currentAddress = next;

        return returnString;
    }

    private boolean checkLow(SubnetUtils subnetUtils) {
        return (subnetUtils.getInfo().getAddress().equals(subnetUtils.getInfo().getLowAddress()));
    }

    private boolean checkHigh(SubnetUtils subnetUtils) {
        return (subnetUtils.getInfo().getAddress().equals(subnetUtils.getInfo().getHighAddress()));
    }

    private SubnetUtils overflowToLow() {
        return new SubnetUtils(currentAddress.getInfo().getLowAddress(), currentAddress.getInfo().getNetmask());
    }

    private SubnetUtils overflowToHigh() {
        return new SubnetUtils(currentAddress.getInfo().getHighAddress(), currentAddress.getInfo().getNetmask());
    }
}
