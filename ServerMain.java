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

public class ServerMain {
    
    private final static int LISTEN_PORT = 6789;
    private final static int RMI_PORT = 6889;
    private final static String DELIMITER = " ";
    private final static ExecutorService threadpool = Executors.newCachedThreadPool();
    private final static ConcurrentHashMap<String, WinSomeUser> usersMap = new ConcurrentHashMap<>();
    private final static WinSomeNetwork network = new WinSomeNetwork();
    private final static LinkedList<Socket> connectedSockets = new LinkedList<>();
    private final static RMIRegistration REGISTRATION = new RMIRegistration(network);
    

    public static void main(String[] args) {

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

            while (true) {
                Socket newConnection = listenSocket.accept();
                connectedSockets.add(newConnection);
                threadpool.submit(new RequestHandler(newConnection, network));
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
            threadpool.shutdown();
        } 
    }
}
