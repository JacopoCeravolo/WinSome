package server.socialnetwork;

public class Comment {
    
    private User author;
    private long timeStamp;

    private String contents; 
    
    public Comment(User author, String contents) {
        this.author = author;
        this.timeStamp = System.currentTimeMillis();
        this.contents = contents;
    }

    public User getAuthor() {
        return author;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public String getContents() {
        return contents;
    }
}
