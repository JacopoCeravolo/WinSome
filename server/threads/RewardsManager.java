package server.threads;

import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import server.socialnetwork.Transaction;
import server.socialnetwork.WinSomeComment;
import server.socialnetwork.WinSomeNetwork;
import server.socialnetwork.WinSomePost;
import server.socialnetwork.WinSomeUser;
import server.socialnetwork.WinSomeVote;

public class RewardsManager implements Runnable {

    private final long AWAIT_TIME = 1 * 60000;
    private WinSomeNetwork network;
    private AtomicBoolean exitSignal;


    public RewardsManager(WinSomeNetwork network, AtomicBoolean exitSignal) {
        this.network = network;
        this.exitSignal = exitSignal;
    }

    @Override
    public void run() {
        
        long last_update = System.currentTimeMillis();
        while (!exitSignal.get()) {
            
            if (Math.abs(last_update - System.currentTimeMillis()) < AWAIT_TIME) continue;

            System.out.println("[REWARD MAN] calculating rewards");
            ConcurrentHashMap<String, WinSomeUser> usersMap = network.getUsersMap();

            synchronized (usersMap) {

                for (WinSomeUser user : usersMap.values()) {
        
                    double total_reward = 0;

                    for (WinSomePost post : user.getBlog().values()) {


                        double likes_reward = calculateLikesReward(post, last_update);
                        double comments_reward = calculateCommentsReward(post, last_update);
            
                        double total_post_reward = Math.log(likes_reward) + Math.log(comments_reward);

                        if (total_post_reward > 0) {

                            total_reward = total_post_reward / (post.times_evalued++);
                        }
                    }

                    user.getWallet().addTransaction(
                        new Transaction(new Date(System.currentTimeMillis()), total_reward));
                }
            }

            last_update = System.currentTimeMillis();
        }
        
    }


    private synchronized int calculateLikesReward(WinSomePost post, long last_update) {

        int likes_reward = 0;

        for (WinSomeVote vote : post.getVotes()) {
            
            if (vote.getTimeStamp() < last_update) continue;

            likes_reward += vote.getValue();
        }
        
        return ((likes_reward > 0) ? likes_reward : 0) + 1;
    }

    private synchronized double calculateCommentsReward(WinSomePost post, long last_update) {

        double comments_reward = 0;

        HashMap<WinSomeUser, Integer> commentsByUser = new HashMap<>();

        for (WinSomeComment comment : post.getComments()) {
            
            if (comment.getTimeStamp() < last_update) continue;

            WinSomeUser author = comment.getAuthor();

            if (!commentsByUser.containsKey(author)) {
                commentsByUser.put(author, 1);
            } else {
                commentsByUser.put(author, commentsByUser.get(author) + 1);
            }
        }

        for (WinSomeUser user : commentsByUser.keySet()) {
            comments_reward += 2/(1 + Math.exp( -(commentsByUser.get(user) - 1) ));
        }

        return comments_reward + 1;
    }
}
