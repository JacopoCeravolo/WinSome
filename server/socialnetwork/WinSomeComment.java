package server.socialnetwork;

import java.util.Date;

public class WinSomeComment {
    
    private WinSomeUser author;
    private long timeStamp;

    private String contents; 
    
    public WinSomeComment(WinSomeUser author, String contents) {
        this.author = author;
        this.timeStamp = System.currentTimeMillis();
        this.contents = contents;
    }

    public WinSomeUser getAuthor() {
        return author;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public String getContents() {
        return contents;
    }
}
