package com.cae;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.List;

import java.net.*;
import java.util.*;

public class getIP {
    public static void main(String[] args) {
        List<String> ipAddresses = getLocalIPAddresses();
        String sunnetPrefix = "192.168.1.";
        //匹配当前IP地址前缀
        for (String ip : ipAddresses) {
            if(ip.startsWith(sunnetPrefix)){
                System.out.println(ip);
            }
        }
    }

    public static List<String> getLocalIPAddresses() {
        List<String> ipAddresses = new ArrayList<String>();
        try {
            // 获取所有网络接口
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();

            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = networkInterfaces.nextElement();

                // 获取当前网络接口的所有InetAddress（IP地址）
                Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();

                while (inetAddresses.hasMoreElements()) {
                    InetAddress inetAddress = inetAddresses.nextElement();

                    // 过滤掉loopback地址（127.0.0.1）和非IPv4地址
                    if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                        ipAddresses.add(inetAddress.getHostAddress());
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return ipAddresses;
    }
}
