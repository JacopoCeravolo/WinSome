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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import javax.lang.model.element.Element;
import javax.sql.rowset.spi.SyncResolver;

import server.ServerMain;
import server.socialnetwork.*;

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

            System.out.println("[BACKUP MANAGER] finished");

            last_update = System.currentTimeMillis();
        }
    }


    private static synchronized void backupUserMap() {
        
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
        String jsonMap = gson.toJson(usersMap);

        try {
            ow.write(jsonMap);
            ow.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static synchronized void backupPostMap() {

        FileOutputStream fos;
        OutputStreamWriter ow;
        
        try {
            fos = new FileOutputStream(ServerMain.POSTBACKUP_PATH); 
            ow = new OutputStreamWriter(fos);
        } catch (FileNotFoundException e) {
            System.err.println("file not found");
            return;
        } 
        
        System.out.println("[BACKUP MANAGER] serializing post ID");
        
        AtomicInteger postID = network.getPostID();
        String postIDJson = gson.toJson(postID);

        System.out.println("[BACKUP MANAGER] serializing posts map");

        ConcurrentHashMap<Integer, Post> postMap = network.getPostsMap();
        String jsonMap = gson.toJson(postMap);

        try {
            ow.write(postIDJson);
            ow.write(jsonMap);
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
        String jsonMap = gson.toJson(tagsMap);

        try {
            ow.write(jsonMap);
            ow.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static synchronized ConcurrentHashMap<String, User> deserializeUsers(InputStream userBackup) {
        
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonReader reader = new JsonReader(new InputStreamReader(userBackup));
        //String full = "";

        ConcurrentHashMap<String, User> usersMap = new ConcurrentHashMap<>();

        try {
            reader.beginObject();
            while (reader.hasNext()) {

                String username = reader.nextName();
                
                User newUser = null;

                reader.beginObject();
                while (reader.hasNext()) {
                    newUser = parseUser(reader);
                }
                reader.endObject();

                usersMap.put(username, newUser);
            }
            reader.endObject();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return usersMap;
    }

    public static synchronized AtomicInteger deserializePostID(InputStream postBackup) {

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonReader reader = new JsonReader(new InputStreamReader(postBackup));
        
        AtomicInteger postID = null;

        try {
            postID = new AtomicInteger(reader.nextInt());
        } catch (IOException e) {
            e.printStackTrace();
        }

        return postID;
    }
    
    public static synchronized ConcurrentHashMap<Integer, Post> deserializePosts(InputStream postBackup) {

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonReader reader = new JsonReader(new InputStreamReader(postBackup));
        reader.setLenient(true);

        ConcurrentHashMap<Integer, Post> postMap = new ConcurrentHashMap<>();

        

        try {
            Integer discardedPostId = reader.nextInt();

            reader.beginObject();
            while (reader.hasNext()) {

                String postID = reader.nextName();
                
                Post newPost = null;

                reader.beginObject();
                while (reader.hasNext()) {
                    newPost = parsePost(reader);
                }
                reader.endObject();

                postMap.put(Integer.parseInt(postID), newPost);
            }
            reader.endObject();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return postMap;
    }

    public static synchronized ConcurrentHashMap<String, List<String>> deserializeTags(InputStream tagBackup) {

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonReader reader = new JsonReader(new InputStreamReader(tagBackup));

        ConcurrentHashMap<String, List<String>> tagMap = new ConcurrentHashMap<>();

        try {
            reader.beginObject();
            while (reader.hasNext()) {

                String tag = reader.nextName();
                
                List<String> tagList = parseStringList(reader);

                
                /* while (reader.hasNext()) {
                    tagList = parseStringList(reader);
                }
                reader.endObject(); */

                tagMap.put(tag, tagList);
            }
            reader.endObject();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return tagMap;
    }
    
    private static synchronized User parseUser(JsonReader reader) {
        Integer uniqueID = null;
        String username = null;
        String password = null;
        UserStatus status = UserStatus.OFFLINE;
        ArrayList<String> tagsList = new ArrayList<>();
        TreeSet<String> following = new TreeSet<>();
        TreeSet<String> followers = new TreeSet<>();
        TreeSet<Integer> blog = new TreeSet<>();
        Wallet wallet = null;

        try {
            
            while (reader.hasNext()) {
                String name = reader.nextName();
    
                if (name.equals("username")) {
                    username = reader.nextString();
                } else if (name.equals("password")) {
                    password = reader.nextString();
                } else if (name.equals("status")) {
                    status = UserStatus.valueOf(reader.nextString());
                } else if (name.equals("tagsList")) {
                    reader.beginArray();
                    while (reader.hasNext()) {
                        tagsList.add(reader.nextString());
                    }
                    reader.endArray();
                } else if (name.equals("following")) {
                    reader.beginArray();
                    while (reader.hasNext()) {
                        following.add(reader.nextString());
                    }
                    reader.endArray();
                } else if (name.equals("followers")) {
                    reader.beginArray();
                    while (reader.hasNext()) {
                        followers.add(reader.nextString());
                    }
                    reader.endArray();
                } else if (name.equals("blog")) {
                    reader.beginArray();
                    while (reader.hasNext()) {
                        blog.add(reader.nextInt());
                    }
                    reader.endArray();
                } else if (name.equals("wallet")) {
                    reader.beginObject();
                    wallet = parseWallet(reader);
                    reader.endObject();
                } else {
                    reader.skipValue();
                }
            }
            
        } catch (IOException e) {
            e.printStackTrace();
        }

        User newUser = new User(username, password, tagsList);

        newUser.setStatus(status);
        newUser.setFollowing(following);
        newUser.setFollowers(followers);
        newUser.setBlog(blog);
        newUser.setWallet(wallet);

        return newUser;
    }

    private static synchronized Wallet parseWallet(JsonReader reader) {
        Wallet wallet = new Wallet();
        List<Transaction> history = new ArrayList<>();
        double amount = 0;

        try {
         
            while (reader.hasNext()) {
                String name = reader.nextName();
                if (name.equals("totalAmount")) {
                    amount = reader.nextDouble();
                } else if (name.equals("history")) {
                    reader.beginArray();
                    while (reader.hasNext()) {
                        reader.beginObject();
                        wallet.addTransaction(parseTransaction(reader));
                        reader.endObject();
                    }
                    reader.endArray();
                } else {
                    reader.skipValue();
                }
            }
        
        } catch (IOException e) {
            e.printStackTrace();
        }

        // wallet.setHistory(history);
        wallet.setTotalAmount(amount);

        return wallet;
    }

    private static synchronized Transaction parseTransaction(JsonReader reader) {
        long timeStamp = 0;
        double variation = 0;

        try {

            while (reader.hasNext()) {
                String name = reader.nextName();

                if (name.equals("timeStamp")) {
                    timeStamp = reader.nextLong();
                } else if (name.equals("variation")) {
                    variation = reader.nextDouble();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new Transaction(timeStamp, variation);
    }

    private static synchronized Post parsePost(JsonReader reader) {

        Integer uniqueID = 0;
        long timeStamp = 0;
        User author = null;
        String title = null;
        String contents = null;
        int upvotes = 0;
        int downvotes = 0;
        int rewins = 0;
        int times_evalued = 0;
        List<Vote> votes = new ArrayList<>();
        List<Comment> comments = new ArrayList<>();

        try {
            
            while (reader.hasNext()) {
                String name = reader.nextName();

                if (name.equals("uniqueID")) {
                    uniqueID = reader.nextInt();
                } else if (name.equals("author")) {
                    reader.beginObject();
                    author = parseUser(reader);
                    reader.endObject();
                } else if (name.equals("title")) {
                    title = reader.nextString();
                } else if (name.equals("contents")) {
                    contents = reader.nextString();
                } else if (name.equals("upvotes")) {
                    upvotes = reader.nextInt();
                } else if (name.equals("downvotes")) {
                    downvotes = reader.nextInt();
                } else if (name.equals("rewins")) {
                    rewins = reader.nextInt();
                } else if (name.equals("times_evalued")) {
                    times_evalued = reader.nextInt();
                } else if (name.equals("votes")) {
                    reader.beginArray();
                    while (reader.hasNext()) {
                        votes.add(parseVote(reader));
                    }
                    reader.endArray();
                } else if (name.equals("comments")) {
                    reader.beginArray();
                    while (reader.hasNext()) {
                        comments.add(parseComment(reader));
                    }
                    reader.endArray();
                } else {
                    reader.skipValue();
                }
            }
            
            
        } catch (IOException e) {
            e.printStackTrace();
        }


        
        Post newPost = new Post(author, title, contents);

        newPost.setUniqueID(uniqueID);
        newPost.setTimeStamp(timeStamp);
        newPost.setUpvotes(upvotes);
        newPost.setDownvotes(downvotes);
        newPost.setRewins(rewins);
        newPost.setTimes_evalued(times_evalued);
        newPost.setVotes(votes);
        newPost.setComments(comments);

        return newPost;
    }

    private static synchronized Comment parseComment(JsonReader reader) {
        String author = null;
        long timeStamp = 0;
        String contents = null;
        
        try {
            reader.beginObject();
            while (reader.hasNext()) {
                String name = reader.nextName();

                if (name.equals("author")) {
                    author = reader.nextString();
                } else if (name.equals("timeStamp")) {
                    timeStamp = reader.nextLong();
                } else if (name.equals("contents")) {
                    contents = reader.nextString();
                } else {
                    reader.skipValue();
                }
            }
            reader.endObject();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Comment newComment = new Comment(author, contents);
        newComment.setTimeStamp(timeStamp);

        return newComment;
    }

    private static synchronized Vote parseVote(JsonReader reader) {
        String author = null;
        long timeStamp = 0;
        Integer vote = 0;
        
        try {
            reader.beginObject();
            while (reader.hasNext()) {
                String name = reader.nextName();

                if (name.equals("author")) {
                    author = reader.nextString();
                } else if (name.equals("timeStamp")) {
                    timeStamp = reader.nextLong();
                } else if (name.equals("vote")) {
                    vote = reader.nextInt();
                } else {
                    reader.skipValue();
                }
            }
            reader.endObject();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Vote newVote = new Vote(author, vote);
        newVote.setTimeStamp(timeStamp);

        return newVote;
    }
    
    private static synchronized List<String> parseStringList(JsonReader reader) {

        List<String> stringList = new ArrayList<>();

        try {
            reader.beginArray();
            while (reader.hasNext()) {
                stringList.add(reader.nextString());
            }
            reader.endArray();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return stringList;
    }

}
