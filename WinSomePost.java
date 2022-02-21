import java.util.HashMap;
import java.util.UUID;

public class WinSomePost {
    
    private UUID uniqueID;
    private UUID authorID;

    private WinSomeUser author;
    private String title;
    private String contents; // TODO: maybe create content class

    private int upvotes;
    private int downvotes;

    private HashMap<WinSomeUser, String> comments;

    public WinSomePost(WinSomeUser author, String title, String contents) {
        this.uniqueID = UUID.randomUUID();
        this.author = author;
        this.authorID = author.getUserID();
        this.title = title;
        this.contents = contents;

        this.comments = new HashMap<>();
    }

    public UUID getUniqueID() {
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

}
