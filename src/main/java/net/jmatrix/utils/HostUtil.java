package net.jmatrix.utils;

import java.net.InetAddress;

public class HostUtil {
   
   
   public static String getHostname() {
      String name="unk";
      String ip="u.n.k";
      try {
         
         InetAddress localhost=InetAddress.getLocalHost();
         
         name=localhost.getHostName();
         
         InetAddress addrs[] = InetAddress.getAllByName(name);
         
         for (InetAddress addr : addrs) {
            System.out.println("addr.getHostAddress() = " + addr.getHostAddress());
            System.out.println("addr.getHostName() = " + addr.getHostName());
            System.out.println ("addr.getCanonicalName() = "+addr.getCanonicalHostName());
            System.out.println("addr.isAnyLocalAddress() = "
                  + addr.isAnyLocalAddress());
            System.out.println("addr.isLinkLocalAddress() = "
                  + addr.isLinkLocalAddress());
            System.out.println("addr.isLoopbackAddress() = "
                  + addr.isLoopbackAddress());
            System.out.println("addr.isMulticastAddress() = "
                  + addr.isMulticastAddress());
            System.out.println("addr.isSiteLocalAddress() = "
                  + addr.isSiteLocalAddress());
            System.out.println("");

            if (!addr.isLoopbackAddress() && addr.isSiteLocalAddress()) {
               ip=addr.getHostAddress();
               
            }
         }
         
      } catch (Exception ex) {
         System.out.println("HostUtil: error getting hostnme");
         ex.printStackTrace();
      }
      return name+"/"+ip;
   }
}
