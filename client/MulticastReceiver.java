package client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;

public class MulticastReceiver implements Runnable {
    protected MulticastSocket socket = null;
    protected final byte[] buf = new byte[256];

    @Deprecated
    public void run() {
        //System.out.println("opening socket");
        try {
            socket = new MulticastSocket(8888);
            //if (socket == null) System.err.println("socket is null");
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SecurityException e) {
            System.err.println("security exe");
        }

        //System.out.println("socket opened: " + socket.getPort() + socket.getInetAddress());

        InetAddress group = null;
        try {
            //System.out.println("get group to join");
            group = InetAddress.getByName("230.0.0.0");
        } catch (UnknownHostException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        //System.out.println("group: " + group.getHostAddress());
      
        try {
            //System.out.println("joining group");
            socket.joinGroup(group);
            //System.out.println("group joined");
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        //System.out.println("socket is "+  socket.getPort() + socket.getInetAddress());
        while (true) {
            DatagramPacket packet = new DatagramPacket(buf, buf.length);
            try {
                //System.out.println("receiving packet from "+  socket.getPort() + socket.getInetAddress());
                socket.receive(packet);
                //System.out.println("packet received");
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            String received = new String(
              packet.getData(), 0, packet.getLength());
            if ("end".equals(received)) {
                break;
            }
            // System.out.println(received);
        }
        try {
            socket.leaveGroup(group);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        socket.close();
    }
}