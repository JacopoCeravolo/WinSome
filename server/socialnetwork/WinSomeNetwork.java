package server.socialnetwork;

import java.util.ArrayList;
import java.util.Set;
import java.util.HashMap;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class WinSomeNetwork {
    
    private AtomicInteger userID;
    private AtomicInteger postID;

    private ConcurrentHashMap<String, WinSomeUser> usersMap;
    private ConcurrentHashMap<Integer, WinSomePost> postsMap;
    private ConcurrentHashMap<String, ArrayList<WinSomeUser>> tagsMap;

    public WinSomeNetwork() {
        userID = new AtomicInteger(0);
        postID = new AtomicInteger(0);
        usersMap = new ConcurrentHashMap<>();
        postsMap = new ConcurrentHashMap<>();
        tagsMap = new ConcurrentHashMap<>();
    }

    // TODO: Temporary
    public void register(WinSomeUser user) {

        user.setUserID(userID.incrementAndGet());

        ArrayList<String> tags = (ArrayList<String>)user.getTags();
        System.out.println(tags);
        for (String tag : tags) {
            tagsMap.putIfAbsent(tag, new ArrayList<>());
            tagsMap.get(tag).add(user);
        }
        
        usersMap.put(user.getUserName(), user);
    }

    public WinSomeUser login(String username, String password) 
        throws UserNotFoundException, InvalidPasswordException {
    
        WinSomeUser user = usersMap.get(username);
                        
        if (user == null) throw new UserNotFoundException();
        
        if (!password.equals(user.getPassword())) throw new InvalidPasswordException();
        
        user.setUserStatus(WinSomeUserStatus.ONLINE);

        return user;
    }

    public void logout(WinSomeUser user) {
            
        user.setUserStatus(WinSomeUserStatus.OFFLINE);
    }
    
    public ArrayList<WinSomeUser> listUsers(WinSomeUser user) {
        
        StringBuilder format = new StringBuilder("NAME\t:\tFOLLOWERS\t:\tFOLLOWING\t:\tTAGS\n");
        ArrayList<WinSomeUser> usersList = new ArrayList<>();

        for (String tag : user.getTags()) {
            for (WinSomeUser u : tagsMap.get(tag)) {
                
                if (u.equals(user) || usersList.contains(u)) continue;

                usersList.add(u);
                format.append(u.toString());
            }
        }

        return usersList;
    }

    public void followUser(WinSomeUser user, String username) 
        throws UserNotFoundException {

        WinSomeUser toFollow = usersMap.get(username);

        if (toFollow == null) throw new UserNotFoundException();

        user.getFollowing().putIfAbsent(username, toFollow);
        toFollow.getFollowers().putIfAbsent(user.getUserName(), user);
        
    }

    public void unfollowUser(WinSomeUser user, String username) 
        throws UserNotFoundException {

            WinSomeUser toUnfollow = usersMap.get(username);

            if (toUnfollow == null) throw new UserNotFoundException();

            user.getFollowing().remove(username, toUnfollow);
            toUnfollow.getFollowers().remove(user.getUserName(), user);

    }

    public Integer createPost(WinSomeUser user, WinSomePost post) {

        Integer postNum = postID.incrementAndGet();

        post.setPostID(postNum);
        postsMap.put(postNum, post);
        user.getBlog().put(postNum, post);

        return postNum;
    }

    public WinSomePost getPost(Integer postID) 
        throws PostNotFoundException {

        WinSomePost post = postsMap.get(postID);
        
        if (post == null) throw new PostNotFoundException();
        
        return post;
    }

    // Should return an ordered set
    public ArrayList<WinSomePost> getFeed(WinSomeUser user) {

        ArrayList<WinSomePost> feed = new ArrayList<>();
        HashMap<String, WinSomeUser> following = user.getFollowing();
        
        for (WinSomeUser u : following.values()) {
            for (WinSomePost post : u.getBlog().values()) {
                feed.add(post);
            }
        }

        return feed;
    }

    public WinSomePost deletePost(WinSomeUser user, Integer postID) 
        throws PostNotFoundException, UnauthorizedOperationException {

            WinSomePost post = postsMap.get(postID);

            if (post == null) throw new PostNotFoundException();

            if (!post.getAuthor().equals(user)) throw new UnauthorizedOperationException();

            // should delete everything
            postsMap.remove(postID, post);
            user.getBlog().remove(postID, post);
            return post;
    }

    public void rewinPost(WinSomeUser user, Integer postID)
        throws PostNotFoundException {

            WinSomePost post = postsMap.get(postID);

            if (post == null) throw new PostNotFoundException();

            post.increRewin();

            // should check if key (postID) is already used
            user.getBlog().put(postID, post);
    }

    public void ratePost(WinSomeUser user, Integer postID, Integer vote)
        throws PostNotFoundException, UnauthorizedOperationException {

            WinSomePost post = postsMap.get(postID);

            if (post == null) throw new PostNotFoundException();

            if (!this.getFeed(user).contains(post)) throw new UnauthorizedOperationException();

            if (vote > 0) {
                post.upvotePost();
            } else {
                post.downvotePost();
            }
            
    }

    public void commentPost(WinSomeUser user, Integer postID, String comment)
        throws PostNotFoundException, UnauthorizedOperationException {

            WinSomePost post = postsMap.get(postID);

            if (post == null) throw new PostNotFoundException();

            if (!this.getFeed(user).contains(post)) throw new UnauthorizedOperationException();

            post.getComments().put(user, comment);
            
    }

    public void addUserToTagList(WinSomeUser user, String tag) {
        tagsMap.putIfAbsent(tag, new ArrayList<>());

        tagsMap.get(tag).add(user);
    }

    public ConcurrentHashMap<String, WinSomeUser> getUsersMap() {
        return usersMap;
    }

    public void setUsersMap(ConcurrentHashMap<String, WinSomeUser> usersMap) {
        this.usersMap = usersMap;
    }

    public ConcurrentHashMap<Integer, WinSomePost> getPostsMap() {
        return postsMap;
    }

    public void setPostsMap(ConcurrentHashMap<Integer, WinSomePost> postsMap) {
        this.postsMap = postsMap;
    }

    public ConcurrentHashMap<String, ArrayList<WinSomeUser>> getTagsMap() {
        return tagsMap;
    }

    public void setTagsMap(ConcurrentHashMap<String, ArrayList<WinSomeUser>> tagsMap) {
        this.tagsMap = tagsMap;
    }

    


}
