package server.socialnetwork;

public class Comment {
    
    private String author;
    private long timeStamp;

    private String contents; 
    
    public Comment(String author, String contents) {
        this.author = author;
        this.timeStamp = System.currentTimeMillis();
        this.contents = contents;
    }

    public String getAuthor() {
        return author;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getContents() {
        return contents;
    }
}
