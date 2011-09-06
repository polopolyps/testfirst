package com.polopoly.ps.test;

import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

public class ClientControlUtil {
    private static final Logger LOGGER = Logger.getLogger(ClientControlUtil.class.getName());

    public static boolean disconnectClient() {
        try {
            ObjectName mBean = getMBean();
            ManagementFactory.getPlatformMBeanServer().invoke(mBean, "disconnect", null, null);
            return true;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Could not put client in autonomous mode: " + e.getMessage());
            return false;
        }
    }

    public static boolean connectClient() {
        try {
            ObjectName mBean = getMBean();
            ManagementFactory.getPlatformMBeanServer().invoke(mBean, "connect", null, null);
            ManagementFactory.getPlatformMBeanServer().invoke(mBean, "startServing", null, null);
            return true;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Could not put client in autonomous mode: " + e.getMessage(), e);
            return false;
        }
    }

    private static ObjectName getMBean() throws UnknownHostException, MalformedObjectNameException {
        String myHost = InetAddress.getLocalHost().getHostName();
        String objectName = "com.polopoly:host=" + myHost
                + ",application=polopolyclient,detailLevel=SUMMARY,name=Control";
        return new ObjectName(objectName);
    }
}
