package server.socialnetwork;

public class Vote {
    
    private User author;
    private long timeStamp;

    private Integer vote; 

    public Vote(User author, Integer vote) {
        this.author = author;
        this.timeStamp = System.currentTimeMillis();
        this.vote = vote;
    }

    public User getAuthor() {
        return author;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public int getValue() {
        return (vote > 0) ? +1 : -1;
    }
}
