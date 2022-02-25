package server.socialnetwork;

import java.util.HashMap;
import java.util.UUID;

public class WinSomePost {
    
    private Integer uniqueID;
    private Integer authorID;

    private WinSomeUser author;
    private String title;
    private String contents; // TODO: maybe create content class

    private int upvotes;
    private int downvotes;
    private int rewins;

    private HashMap<WinSomeUser, String> comments;

    public WinSomePost(WinSomeUser author, String title, String contents) {
        this.author = author;
        this.authorID = author.getUserID();
        this.title = title;
        this.contents = contents;

        this.upvotes = 0;
        this.downvotes = 0;
        this.rewins = 0;

        this.comments = new HashMap<>();
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

    public HashMap<WinSomeUser, String> getComments() {
        return comments;
    }

    public void upvotePost() {
        upvotes++;
    }

    public void downvotePost() {
        downvotes++;
    }

    public void increRewin() {
        rewins++;
    }

    @Override
    public String toString() {
    
        return new String(uniqueID + "\t|\t" + author.getUserName() + "\t|\t" + 
                            title + "\t|\t" + contents + "\t|\t" + 
                            upvotes + "\t|\t" + downvotes + "\t|\t" + rewins + "\t|\t" + comments.values().size()  + "\n");
    }

}
