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
import java.rmi.AlreadyBoundException;
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

import javax.swing.plaf.synth.Region;


public class ServerMain {
    
    private final static long KEEP_ALIVE = 12 * 60000;

    private final static int LISTEN_PORT = 6789;
    private final static int RMI_PORT = 6889;
    private final static int CALLBACK_PORT = 6989;

    private final static String CALLBACK_SERVICE_NAME = "WinSomeCallback";
    private final static String RMI_SERVICE_NAME = "WinSomeRegistration";

    private final static AtomicBoolean exitSignal = new AtomicBoolean(false);

    private final static ExecutorService threadpool = Executors.newCachedThreadPool();

    private final static WinSomeNetwork network = new WinSomeNetwork();

    private final static LinkedList<Socket> connectedSockets = new LinkedList<>();

    private final static RMIRegistration REGISTRATION = new RMIRegistration(network);
    private final static CallbackRegistration FOLLOWER_UPDATES = new CallbackRegistration();
    

    public static void main(String[] args) {

        Thread rewardManager = new Thread(new RewardsManager(network, exitSignal));
        rewardManager.start();

        RMIRegistrationInterface REGISTRATION_STUB = null;
        CallbackRegistrationInterface CALLBACK_STUB = null;
        Registry RMI_REGISTRY = null;
        Registry CALLBACK_REGISTRY = null;

        // RMI
        try {

            REGISTRATION_STUB = (RMIRegistrationInterface) UnicastRemoteObject.exportObject(REGISTRATION, 0);
            LocateRegistry.createRegistry(RMI_PORT);
            RMI_REGISTRY = LocateRegistry.getRegistry(RMI_PORT);
            RMI_REGISTRY.rebind(RMI_SERVICE_NAME, REGISTRATION_STUB);

        } catch (RemoteException e) {
            System.err.println("Error: " + e.getMessage());
        }

        // CALLBACK
        try {

            CALLBACK_STUB = (CallbackRegistrationInterface) UnicastRemoteObject.exportObject(FOLLOWER_UPDATES, 0);
            LocateRegistry.createRegistry(CALLBACK_PORT);
            CALLBACK_REGISTRY = LocateRegistry.getRegistry(CALLBACK_PORT);
            CALLBACK_REGISTRY.bind(CALLBACK_SERVICE_NAME, CALLBACK_STUB);

        } catch (RemoteException e) {
            System.err.println("Error: " + e.getMessage());
        } catch (AlreadyBoundException e) {
            System.err.println("Error: " + e.getMessage());
        }
        
        
        try (ServerSocket listenSocket = new ServerSocket(LISTEN_PORT)) {

            long startTime = System.currentTimeMillis();
            while ((KEEP_ALIVE - Math.abs(System.currentTimeMillis() - startTime)) > 0) {
                Socket newConnection = listenSocket.accept();
                connectedSockets.add(newConnection);
                threadpool.submit(new ConnectionManager(newConnection, network, exitSignal, FOLLOWER_UPDATES));
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
