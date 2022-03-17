package server.socialnetwork;

public class Vote {
    
    private String author;
    private long timeStamp;

    private Integer vote; 

    public Vote(String author, Integer vote) {
        this.author = author;
        this.timeStamp = System.currentTimeMillis();
        this.vote = vote;
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

    public int getValue() {
        return (vote > 0) ? +1 : -1;
    }
}
