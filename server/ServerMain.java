package server;

import server.rmi.*;
import server.socialnetwork.*;
import server.threads.*;


import shared.rmi.*;
import shared.utils.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;


public class ServerMain {
    
    private final static long KEEP_ALIVE = 180000;
    private final static int LISTEN_PORT = 6789;
    private final static int RMI_PORT = 6889;
    private final static AtomicBoolean exitSignal = new AtomicBoolean(false);
    private final static ExecutorService threadpool = Executors.newCachedThreadPool();
    private final static WinSomeNetwork network = new WinSomeNetwork();
    private final static LinkedList<Socket> connectedSockets = new LinkedList<>();
    private final static RMIRegistration REGISTRATION = new RMIRegistration(network);
    

    public static void main(String[] args) {

        Thread rewardManager = new Thread(new RewardsManager(network, exitSignal));
        rewardManager.start();

        RMIRegistrationInterface STUB = null;
        Registry RMI_REGISTRY = null;

        try {
            
            STUB = (RMIRegistrationInterface) UnicastRemoteObject.exportObject(REGISTRATION, 0);
            LocateRegistry.createRegistry(RMI_PORT);
            RMI_REGISTRY = LocateRegistry.getRegistry(RMI_PORT);
            RMI_REGISTRY.rebind(Utils.RMI_SERVICE_NAME, STUB);

        } catch (RemoteException e) {
            System.err.println("Error: " + e.getMessage());
        }
        
        
        try (ServerSocket listenSocket = new ServerSocket(LISTEN_PORT)) {

            long startTime = System.currentTimeMillis();
            while ((KEEP_ALIVE - Math.abs(System.currentTimeMillis() - startTime)) > 0) {
                Socket newConnection = listenSocket.accept();
                connectedSockets.add(newConnection);
                threadpool.submit(new RequestHandler(newConnection, network, exitSignal));
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {

            for (Socket socket : connectedSockets) {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            exitSignal.set(true);
            threadpool.shutdown();

            try {
                rewardManager.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            System.out.println("Exiting...");
        } 
    }
}
