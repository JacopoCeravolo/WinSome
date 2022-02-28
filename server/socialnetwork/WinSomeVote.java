package server.socialnetwork;

import java.util.Date;

public class WinSomeVote {
    
    private WinSomeUser author;
    private long timeStamp;

    private Integer vote; 

    public WinSomeVote(WinSomeUser author, Integer vote) {
        this.author = author;
        this.timeStamp = System.currentTimeMillis();
        this.vote = vote;
    }

    public WinSomeUser getAuthor() {
        return author;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public int getValue() {
        return (vote > 0) ? +1 : -1;
    }
}
