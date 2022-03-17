package server.socialnetwork;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Post {
    
    private Integer uniqueID;

    private long timeStamp;

    private User author;
    private String title;
    private String contents; // TODO: maybe create content class

    private int upvotes;
    private int downvotes;
    private int rewins;
    public int times_evalued;

    private List<Vote> votes;
    private List<Comment> comments;

    // private HashMap<WinSomeUser, String> comments;

    public Post(User author, String title, String contents) {
        
        this.author = author;
        this.title = title;
        this.contents = contents;

        this.timeStamp = System.currentTimeMillis();

        this.upvotes = 0;
        this.downvotes = 0;
        this.rewins = 0;
        this.times_evalued = 0;

        this.votes = new ArrayList<>();
        this.comments = new ArrayList<>();
    }

    public void setPostID(Integer postID) {
        this.uniqueID = postID;
    }

    public Integer getUniqueID() {
        return uniqueID;
    }

    public User getAuthor() {
        return author;
    }

    public String getTitle() {
        return title;
    }

    public String getContents() {
        return contents;
    }

    public int getUpvotes() {
        return upvotes;
    }

    public int getDownvotes() {
        return downvotes;
    }

    public List<Comment> getComments() {
        return comments;
    }

    public List<Vote> getVotes() {
        return votes;
    }

    public void upvotePost(User user) {
        votes.add(new Vote(user.getUserName(), +1));
        upvotes++;
    }

    public void downvotePost(User user) {
        votes.add(new Vote(user.getUserName(), -1));
        downvotes++;
    }

    public void increRewin() {
        rewins++;
    }

    

    public void setUniqueID(Integer uniqueID) {
        this.uniqueID = uniqueID;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public void setAuthor(User author) {
        this.author = author;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setContents(String contents) {
        this.contents = contents;
    }

    public void setUpvotes(int upvotes) {
        this.upvotes = upvotes;
    }

    public void setDownvotes(int downvotes) {
        this.downvotes = downvotes;
    }

    public int getRewins() {
        return rewins;
    }

    public void setRewins(int rewins) {
        this.rewins = rewins;
    }

    public int getTimes_evalued() {
        return times_evalued;
    }

    public void setTimes_evalued(int times_evalued) {
        this.times_evalued = times_evalued;
    }

    public void setVotes(List<Vote> votes) {
        this.votes = votes;
    }

    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }

    @Override
    public String toString() {

        String postFormatted = new String(
            "\n(id="+uniqueID+") " +  author.getUserName() + " says:\n\n" +
            "\t" + title + "\n" +
            "\t" + contents + "\n\n" +
            "\t[upvotes]: " + upvotes + " [downvotes]: " + downvotes + " [rewins]: " + rewins + "\n"
        );

        if (!comments.isEmpty()) {

            String commentsFormatted = "\n";

            for (Comment comment : comments) {
                commentsFormatted = commentsFormatted + 
                    "\t" + comment.getAuthor() + " commented:\n" +
                    "\t\t- " + comment.getContents() + "\n";
            }

            postFormatted = postFormatted + commentsFormatted;
        }
    
        return postFormatted;
    }

}
