package server.threads;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.*;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.reflect.Type;
import com.google.gson.reflect.TypeToken;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import server.ServerMain;
import server.socialnetwork.Post;
import server.socialnetwork.User;
import server.socialnetwork.WinSomeNetwork;

public class BackupManager implements Runnable {

    private final long AWAIT_TIME = 90000;
    private static WinSomeNetwork network;
    private final AtomicBoolean exitSignal;
    private static Gson gson;

    private static OutputStreamWriter usersBackup;
    private static OutputStreamWriter postsBackup;
    private static OutputStreamWriter tagsBackup;


    public BackupManager(WinSomeNetwork network, AtomicBoolean exitSignal) {
        this.network = network;
        this.exitSignal = exitSignal;
        this.gson = new GsonBuilder().setPrettyPrinting().create();
    }

    @Override
    public void run() {
        

        long last_update = System.currentTimeMillis();
        
        while (!exitSignal.get()) {

            if (Math.abs(last_update - System.currentTimeMillis()) < AWAIT_TIME) continue;

            System.out.println("[BACKUP MANAGER] starting serialization");

            backupUserMap();
            backupPostMap();
            backupTagsMap();

            last_update = System.currentTimeMillis();
        }
    }

    public static ConcurrentHashMap<String, User> deserializeUserMap(InputStream userBackup) {
        
        Type usersMapType = new TypeToken<ConcurrentHashMap<String, User>>() {}.getType();
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonReader reader = new JsonReader(new InputStreamReader(userBackup));
        
        try {
            while (reader.hasNext()) {
                User u = gson.fromJson(reader, User.class);
                System.out.println(u);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        

        return null;
    }


    private static synchronized void backupUserMap() {
        System.out.println("[BACKUP MANAGER] serializing users map");

        FileOutputStream fos;
        OutputStreamWriter ow;
        
        try {
            fos = new FileOutputStream(ServerMain.USERBACKUP_PATH); 
            ow = new OutputStreamWriter(fos);
        } catch (FileNotFoundException e) {
            System.err.println("file not found");
            return;
        } 
       
                
        ConcurrentHashMap<String, User> usersMap = network.getUsersMap();
        Iterator<User> iterator = usersMap.values().iterator();

        try {
            ow.write("[");
            while (iterator.hasNext()) {
                
                String userToJson = gson.toJson(iterator.next());
       
                ow.write(userToJson);
                if (iterator.hasNext()) { ow.write(",\n"); }

            }
            ow.write("]");
            ow.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
    }

    private static synchronized void backupPostMap() {
        System.out.println("[BACKUP MANAGER] serializing post ID");
        
        AtomicInteger postID = network.getPostID();
        String postIDJson = gson.toJson(postID);

        System.out.println("[BACKUP MANAGER] serializing posts map");
                
        FileOutputStream fos;
        OutputStreamWriter ow;
        
        try {
            fos = new FileOutputStream(ServerMain.POSTBACKUP_PATH); 
            ow = new OutputStreamWriter(fos);
        } catch (FileNotFoundException e) {
            System.err.println("file not found");
            return;
        } 
       
                
        ConcurrentHashMap<Integer, Post> postMap = network.getPostsMap();
        Iterator<Post> iterator = postMap.values().iterator();

        try {       
            ow.write("[");
            while (iterator.hasNext()) {
                String postToJson = gson.toJson(iterator.next());
            
                ow.write(postToJson);
                if (iterator.hasNext()) { ow.write(",\n"); }
            } 
            ow.write("]");
            ow.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static synchronized void backupTagsMap() {
        System.out.println("[BACKUP MANAGER] serializing tags map");

        FileOutputStream fos;
        OutputStreamWriter ow;
        
        try {
            fos = new FileOutputStream(ServerMain.TAGBACKUP_PATH); 
            ow = new OutputStreamWriter(fos);
        } catch (FileNotFoundException e) {
            System.err.println("file not found");
            return;
        } 
       
        ConcurrentHashMap<String, List<String>> tagsMap = network.getTagsMap();
        Iterator<List<String>> iterator = tagsMap.values().iterator();
        try {  
            ow.write("[");
            while (iterator.hasNext()) {
                String usersListToJson = gson.toJson(iterator.next());

                ow.write(usersListToJson);
                if (iterator.hasNext()) { ow.write(",\n"); }
            } 
            ow.write("]");
            ow.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
    }

}
