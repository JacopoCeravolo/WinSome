package server;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import server.rmi.RMIRegistration;
import server.socialnetwork.Post;
import server.socialnetwork.User;
import server.socialnetwork.WinSomeNetwork;
import server.threads.ConnectionManager;
import server.threads.RewardsManager;
import server.threads.BackupManager;

import shared.rmi.RMIRegistrationInterface;


public class ServerMain {
    
    private final static long SERIALIZE_TIME = 2 * 60000;

    private final static int LISTEN_PORT = 6789;
    private final static int RMI_PORT = 6889;


    private final static String RMI_SERVICE_NAME = "WinSomeRegistration";

    private final static AtomicBoolean exitSignal = new AtomicBoolean(false);

    private final static ExecutorService threadpool = Executors.newCachedThreadPool();

    private static WinSomeNetwork network = new WinSomeNetwork();

    private final static LinkedList<Socket> connectedSockets = new LinkedList<>();

    private static RMIRegistration REGISTRATION;

    public final static String USERBACKUP_PATH = "/Users/jacopoceravolo/Development/WinSome/server/backup/users_backup.json";
    public final static String POSTBACKUP_PATH = "/Users/jacopoceravolo/Development/WinSome/server/backup/posts_backup.json";
    public final static String TAGBACKUP_PATH = "/Users/jacopoceravolo/Development/WinSome/server/backup/tags_backup.json";
    

    public static void main(String[] args) {

        boolean recover = false;

        File usersBackupFile = new File(USERBACKUP_PATH);
        File postsBackupFile = new File(POSTBACKUP_PATH);
        File tagsBackupFile = new File(TAGBACKUP_PATH);
        
        try {
            if (usersBackupFile.exists() && postsBackupFile.exists() && tagsBackupFile.exists()) {
        
                System.out.println("[SERVER] retreiving backup");
                
                ConcurrentHashMap<String, User> usersMap = 
                    BackupManager.deserializeUsers(new FileInputStream(usersBackupFile));

                AtomicInteger postID =
                    BackupManager.deserializePostID(new FileInputStream(postsBackupFile));
                
                ConcurrentHashMap<Integer, Post> postsMap = 
                    BackupManager.deserializePosts(new FileInputStream(postsBackupFile));

                ConcurrentHashMap<String, List<String>> tagsMap = 
                    BackupManager.deserializeTags(new FileInputStream(tagsBackupFile));

                System.out.println("[SERVER] initializing structures with saved backup");

                network.setUsersMap(usersMap);
                network.setPostID(postID);
                network.setPostsMap(postsMap);
                network.setTagsMap(tagsMap);

                recover = true;
                
                
            } else {
                // network = new WinSomeNetwork();
                usersBackupFile.createNewFile();
                postsBackupFile.createNewFile();
                tagsBackupFile.createNewFile();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        // RMI
        try {

            REGISTRATION = new RMIRegistration(network);

            if (recover) {

                for (User user : network.getUsersMap().values()) {
                    for (String follower : user.getFollowers()) {
                        REGISTRATION.followerUpdate(user.getUsername(), "add"+":"+user.toString());
                    }
                }
            }

            RMIRegistrationInterface STUB = (RMIRegistrationInterface) UnicastRemoteObject.exportObject(REGISTRATION, 0);
           
            LocateRegistry.createRegistry(RMI_PORT);
            Registry RMI_REGISTRY = LocateRegistry.getRegistry(RMI_PORT);
            RMI_REGISTRY.rebind(RMI_SERVICE_NAME, STUB);
            System.out.println("[SERVER] exported registration stub");

        } catch (RemoteException e) {
            System.err.println("Error: " + e.getMessage());
        }


        System.out.println("[SERVER] starting rewards manager");
        Thread rewardManager = new Thread(new RewardsManager(network, exitSignal));
        rewardManager.start();

        System.out.println("[SERVER] starting backup manager");
        Thread backupManager = new Thread(new BackupManager(network, exitSignal));
        backupManager.start();


        try (ServerSocket listenSocket = new ServerSocket(LISTEN_PORT)) {

            System.out.println("[SERVER] ready to accept connections");

            long startTime = System.currentTimeMillis();
            long lastSerialization = startTime;
            while (true) {

                Socket newConnection = listenSocket.accept();
                System.out.println("[SERVER] new connection on " + newConnection.getPort() + newConnection.getInetAddress());
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
                backupManager.join();
                rewardManager.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            System.out.println("Exiting...");
        } 
    }
}
