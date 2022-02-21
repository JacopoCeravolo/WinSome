import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerMain {
    
    private final static int PORT = 6789;
    private final static String DELIMITER = " ";
    private final static ExecutorService threadpool = Executors.newCachedThreadPool();
    private final static ConcurrentHashMap<String, WinSomeUser> usersMap = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        
        
        try (ServerSocket listenSocket = new ServerSocket(PORT)) {

            while (true) {
                threadpool.submit(new RequestHandler(listenSocket.accept(), usersMap));
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            threadpool.shutdown();
        } 
    }
}
