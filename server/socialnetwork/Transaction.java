package server.socialnetwork;

import java.util.Date;

public class Transaction {
    
    private long timeStamp;
    private double variation;

    public Transaction(long timeStamp, double variation) {
        this.timeStamp = timeStamp;
        this.variation = variation;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public double getVariation() {
        return variation;
    }

}
