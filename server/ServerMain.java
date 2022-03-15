package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;


import server.rmi.RMIRegistration;
import server.socialnetwork.WinSomeNetwork;
import server.threads.ConnectionManager;
import server.threads.RewardsManager;
import server.threads.Serializer;

import shared.rmi.RMIRegistrationInterface;


public class ServerMain {
    
    private final static long SERIALIZE_TIME = 2 * 60000;

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
    // private final static CallbackRegistration FOLLOWER_UPDATES = new CallbackRegistration();
    

    public static void main(String[] args) {



        Thread rewardManager = new Thread(new RewardsManager(network, exitSignal));
        rewardManager.start();

        Thread serializer = new Thread(new Serializer(network, exitSignal));
        serializer.start();

        RMIRegistrationInterface STUB = null;
        //CallbackRegistrationInterface CALLBACK_STUB = null;
        Registry RMI_REGISTRY = null;
        //Registry CALLBACK_REGISTRY = null;

        // RMI
        try {

            STUB = (RMIRegistrationInterface) UnicastRemoteObject.exportObject(REGISTRATION, 0);
           
            LocateRegistry.createRegistry(RMI_PORT);
            RMI_REGISTRY = LocateRegistry.getRegistry(RMI_PORT);
            RMI_REGISTRY.rebind(RMI_SERVICE_NAME, STUB);

            

        } catch (RemoteException e) {
            System.err.println("Error: " + e.getMessage());
        }

        // CALLBACK
        /* try {

            CALLBACK_STUB = (CallbackRegistrationInterface) UnicastRemoteObject.exportObject(FOLLOWER_UPDATES, 0);
            LocateRegistry.createRegistry(CALLBACK_PORT);
            CALLBACK_REGISTRY = LocateRegistry.getRegistry(CALLBACK_PORT);
            CALLBACK_REGISTRY.bind(CALLBACK_SERVICE_NAME, CALLBACK_STUB);

        } catch (RemoteException e) {
            System.err.println("Error: " + e.getMessage());
        } catch (AlreadyBoundException e) {
            System.err.println("Error: " + e.getMessage());
        } */
        
        
        try (ServerSocket listenSocket = new ServerSocket(LISTEN_PORT)) {

            long startTime = System.currentTimeMillis();
            long lastSerialization = startTime;
            while (true) {

                Socket newConnection = listenSocket.accept();
                connectedSockets.add(newConnection);
                threadpool.submit(new ConnectionManager(newConnection, network, exitSignal, REGISTRATION));

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
                serializer.join();
                rewardManager.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            System.out.println("Exiting...");
        } 
    }
}
