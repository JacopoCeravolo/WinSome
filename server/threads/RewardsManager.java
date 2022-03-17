package server.threads;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import server.socialnetwork.*;
import server.socialnetwork.Comment;


public class RewardsManager implements Runnable {

    private final long AWAIT_TIME = 1 * 60000;
    private WinSomeNetwork network;
    private AtomicBoolean exitSignal;

    private DatagramSocket socket;
    private InetAddress group;
    private byte[] buf;


    public RewardsManager(WinSomeNetwork network, AtomicBoolean exitSignal) {
        this.network = network;
        this.exitSignal = exitSignal;
    }

    public void multicast(
      String multicastMessage) throws IOException {
        socket = new DatagramSocket();
        // if (socket == null) System.err.println("socket is null");
        group = InetAddress.getByName("230.0.0.0");
        buf = multicastMessage.getBytes();

        DatagramPacket packet 
          = new DatagramPacket(buf, buf.length, group, 8888);
        //System.out.println("[REWARDS MANAGER] sending to socket " + 8888 + socket.getInetAddress() + " at group " + group);
        socket.send(packet);
        //System.out.println("[REWARDS MANAGER] sent update " + multicastMessage);
        socket.close();
    }

    @Override
    public void run() {
        
        long last_update = System.currentTimeMillis();
        
        while (!exitSignal.get()) {
            
            if (Math.abs(last_update - System.currentTimeMillis()) < AWAIT_TIME) continue;

            System.out.println("[REWARDS MANAGER] calculating rewards");
            ConcurrentHashMap<String, User> usersMap = network.getUsersMap();

            synchronized (usersMap) {

                for (User user : usersMap.values()) {
        
                    double total_reward = 0;

                    for (Integer postID : user.getBlog()) {

                        Post post = network.getPostsMap().get(postID);
                        double likes_reward = calculateLikesReward(post, last_update);
                        double comments_reward = calculateCommentsReward(post, last_update);
            
                        double post_reward = 
                            (Math.log(likes_reward) + Math.log(comments_reward)) / (post.times_evalued++);

                        total_reward += post_reward;
                    }

                    if (Double.isInfinite(total_reward)) {
                        System.err.println("[REWARD MANAGER] error when calculating, got Infinity");
                        continue;
                    }

                    if (total_reward > 0) {
                        user.getWallet().addTransaction(
                            new Transaction(new Date(System.currentTimeMillis()), total_reward));
                    }
                }
            }

            try {
                multicast("update");
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            System.out.println("[REWARDS MANAGER] sent wallet updates");

            last_update = System.currentTimeMillis();
        }
        
    }


    private synchronized int calculateLikesReward(Post post, long last_update) {

        int likes_reward = 0;

        for (Vote vote : post.getVotes()) {
            
            if (vote.getTimeStamp() < last_update) continue;

            likes_reward += vote.getValue();
        }
        
        return ((likes_reward > 0) ? likes_reward : 0) + 1;
    }

    private synchronized double calculateCommentsReward(Post post, long last_update) {

        double comments_reward = 0;

        HashMap<User, Integer> commentsByUser = new HashMap<>();

        for (Comment comment : post.getComments()) {
            
            if (comment.getTimeStamp() < last_update) continue;

            User author = network.getUsersMap().get(comment.getAuthor());

            if (!commentsByUser.containsKey(author)) {
                commentsByUser.put(author, 1);
            } else {
                commentsByUser.put(author, commentsByUser.get(author) + 1);
            }
        }

        for (User user : commentsByUser.keySet()) {
            comments_reward += 2/(1 + Math.exp( -(commentsByUser.get(user) - 1) ));
        }

        return comments_reward + 1;
    }
}
