package server.socialnetwork;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class WinSomePost {
    
    private Integer uniqueID;

    private Date timeStamp;

    private WinSomeUser author;
    private String title;
    private String contents; // TODO: maybe create content class

    private int upvotes;
    private int downvotes;
    private int rewins;

    private List<WinSomeVote> votes;
    private List<WinSomeComment> comments;

    // private HashMap<WinSomeUser, String> comments;

    public WinSomePost(WinSomeUser author, String title, String contents) {
        
        this.author = author;
        this.title = title;
        this.contents = contents;

        this.timeStamp = new Date(System.currentTimeMillis());

        this.upvotes = 0;
        this.downvotes = 0;
        this.rewins = 0;

        this.votes = new ArrayList<>();
        this.comments = new ArrayList<>();
    }

    public void setPostID(Integer postID) {
        this.uniqueID = postID;
    }

    public Integer getUniqueID() {
        return uniqueID;
    }

    public WinSomeUser getAuthor() {
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

    public List<WinSomeComment> getComments() {
        return comments;
    }

    public List<WinSomeVote> getVotes() {
        return votes;
    }

    public void upvotePost(WinSomeUser user) {
        votes.add(new WinSomeVote(user, +1));
        upvotes++;
    }

    public void downvotePost(WinSomeUser user) {
        votes.add(new WinSomeVote(user, -1));
        downvotes++;
    }

    public void increRewin() {
        rewins++;
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

            for (WinSomeComment comment : comments) {
                commentsFormatted = commentsFormatted + 
                    "\t" + comment.getAuthor().getUserName() + " commented:\n" +
                    "\t\t- " + comment.getContents() + "\n";
            }

            postFormatted = postFormatted + commentsFormatted;
        }
    
        return postFormatted;
    }

}
