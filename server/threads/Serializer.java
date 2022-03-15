package server.threads;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import server.socialnetwork.Post;
import server.socialnetwork.User;
import server.socialnetwork.WinSomeNetwork;

public class Serializer implements Runnable {

    private final long AWAIT_TIME = 90000;
    private final WinSomeNetwork network;
    private final AtomicBoolean exitSignal;


    public Serializer(WinSomeNetwork network, AtomicBoolean exitSignal) {
        this.network = network;
        this.exitSignal = exitSignal;
    }

    @Override
    public void run() {
        
        long last_update = System.currentTimeMillis();
        
        while (!exitSignal.get()) {

            if (Math.abs(last_update - System.currentTimeMillis()) < AWAIT_TIME) continue;

            System.out.println("[SERIALIZER] starting serialization");

            String serializedNetwork = network.serialize();

            System.out.println(serializedNetwork);

            /* // TODO: should be synchronized
            System.out.println("[SERIALIZER] serializing post ID");
            AtomicInteger postID = network.getPostID();
            String postIDToJSON = gson.toJson(postID);
            System.out.println(postIDToJSON);

            // TODO: should be synchronized
            System.out.println("[SERIALIZER] serializing users map");
            ConcurrentHashMap<String, User> usersMap = network.getUsersMap();
            String usersMapToJSON = gson.toJson(usersMap);
            System.out.println(usersMapToJSON);

            // TODO: should be synchronized
            System.out.println("[SERIALIZER] serializing posts map");
            ConcurrentHashMap<Integer, Post> postsMap = network.getPostsMap();
            String postsMapToJSON = gson.toJson(postsMap);
            System.out.println(postsMapToJSON);
            
            // TODO: should be synchronized
            System.out.println("[SERIALIZER] serializing tags map");
            ConcurrentHashMap<String, List<User>> tagsMap = network.getTagsMap();
            String tagsMapToJSON = gson.toJson(tagsMap);
            System.out.println(tagsMapToJSON); */

            last_update = System.currentTimeMillis();
        }
    }
}
