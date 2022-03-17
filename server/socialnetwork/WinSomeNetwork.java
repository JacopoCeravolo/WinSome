package server.socialnetwork;

import server.socialnetwork.exceptions.InvalidPasswordException;
import server.socialnetwork.exceptions.PostNotFoundException;
import server.socialnetwork.exceptions.UnauthorizedOperationException;
import server.socialnetwork.exceptions.UserNotFoundException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class WinSomeNetwork {

    private AtomicInteger postID;

    private ConcurrentHashMap<String, User> usersMap;
    private ConcurrentHashMap<Integer, Post> postsMap;
    private ConcurrentHashMap<String, List<String>> tagsMap;

    public WinSomeNetwork() {
        this.postID = new AtomicInteger(0);
        this.usersMap = new ConcurrentHashMap<>();
        this.postsMap = new ConcurrentHashMap<>();
        this.tagsMap = new ConcurrentHashMap<>();
    }

    public WinSomeNetwork(ConcurrentHashMap<String, User> usersMap) {
        this.postID = new AtomicInteger(0);
        this.usersMap = usersMap;
        this.postsMap = new ConcurrentHashMap<>();
        this.tagsMap = new ConcurrentHashMap<>();
    }

    
    

    public void setPostID(AtomicInteger postID) {
        this.postID = postID;
    }

    public void setUsersMap(ConcurrentHashMap<String, User> usersMap) {
        this.usersMap = usersMap;
    }

    public void setPostsMap(ConcurrentHashMap<Integer, Post> postsMap) {
        this.postsMap = postsMap;
    }

    public void setTagsMap(ConcurrentHashMap<String, List<String>> tagsMap) {
        this.tagsMap = tagsMap;
    }

    public User login(String username, String password)
        throws UserNotFoundException, InvalidPasswordException {
    
        User user = usersMap.get(username);
                        
        if (user == null) throw new UserNotFoundException();
        
        if (!password.equals(user.getPassword())) throw new InvalidPasswordException();
        
        user.setUserStatus(UserStatus.ONLINE);

        return user;
    }

    public void logout(User user) {
        user.setUserStatus(UserStatus.OFFLINE);
    }
    
    public ArrayList<User> listUsers(User user) {
        
        StringBuilder format = new StringBuilder("NAME\t:\tFOLLOWERS\t:\tFOLLOWING\t:\tTAGS\n");
        ArrayList<User> usersList = new ArrayList<>();

        for (String tag : user.getTags()) {

            for (String username : tagsMap.get(tag)) {

                User u = usersMap.get(username);
                if (u.equals(user) || usersList.contains(u)) continue;

                usersList.add(u);
                format.append(u.toString() + "\n");
            }
        }

        return usersList;
    }

    public void followUser(User user, String username)
        throws UserNotFoundException {

        User toFollow = usersMap.get(username);

        if (toFollow == null) throw new UserNotFoundException();

        user.getFollowing().add(toFollow.getUserName());
        toFollow.getFollowers().add(user.getUserName());
        
    }

    public void unfollowUser(User user, String username)
        throws UserNotFoundException {

            User toUnfollow = usersMap.get(username);

            if (toUnfollow == null) throw new UserNotFoundException();

            user.getFollowing().remove(username);
            toUnfollow.getFollowers().remove(user.getUserName());

    }

    public Integer createPost(User user, Post post) {

        Integer postNum = postID.incrementAndGet();

        post.setPostID(postNum);
        postsMap.put(postNum, post);
        user.getBlog().add(postNum);

        return postNum;
    }

    public Post showPost(Integer postID)
        throws PostNotFoundException {

        Post post = postsMap.get(postID);
        
        if (post == null) throw new PostNotFoundException();
        
        return post;
    }

    // TODO: Should return an ordered set
    public ArrayList<Post> showFeed(User user) {

        ArrayList<Post> feed = new ArrayList<>();
        TreeSet<String> following = user.getFollowing();
        
        for (String u : following) {
            for (Integer post_id : usersMap.get(u).getBlog()) {
                feed.add(postsMap.get(post_id));
            }
        }

        return feed;
    }

    public Post deletePost(User user, Integer postID)
        throws PostNotFoundException, UnauthorizedOperationException {

            Post post = postsMap.get(postID);

            if (post == null) throw new PostNotFoundException();

            if (!post.getAuthor().equals(user)) throw new UnauthorizedOperationException();

            // should delete everything
            postsMap.remove(postID, post);
            user.getBlog().remove(postID);
            return post;
    }

    public void rewinPost(User user, Integer postID)
        throws PostNotFoundException {

            Post post = postsMap.get(postID);

            if (post == null) throw new PostNotFoundException();

            post.increRewin();

            // should check if key (postID) is already used
            user.getBlog().add(postID);
    }

    public void ratePost(User user, Integer postID, Integer vote)
        throws PostNotFoundException, UnauthorizedOperationException {

            Post post = postsMap.get(postID);

            if (post == null) throw new PostNotFoundException();

            if (!this.showFeed(user).contains(post)) throw new UnauthorizedOperationException();

            if (vote > 0) {
                post.upvotePost(user);
            } else {
                post.downvotePost(user);
            }
            
    }

    public void commentPost(User user, Integer postID, String comment)
        throws PostNotFoundException, UnauthorizedOperationException {

            Post post = postsMap.get(postID);

            if (post == null) throw new PostNotFoundException();
            

            if (!this.showFeed(user).contains(post)) throw new UnauthorizedOperationException();

            post.getComments().add(new Comment(user.getUserName(), comment));
            
    }

    public ConcurrentHashMap<String, User> getUsersMap() {
        return usersMap;
    }

    public ConcurrentHashMap<Integer, Post> getPostsMap() {
        return postsMap;
    }

    public ConcurrentHashMap<String, List<String>> getTagsMap() {
        return tagsMap;
    }

    public AtomicInteger getPostID() {
        return postID;
    }

}
