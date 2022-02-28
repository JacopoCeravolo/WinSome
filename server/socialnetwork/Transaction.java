package server.socialnetwork;

import java.util.Date;

public class Transaction {
    
    private Date timeStamp;
    private double variation;

    public Transaction(Date timeStamp, double variation) {
        this.timeStamp = timeStamp;
        this.variation = variation;
    }

    public Date getTimeStamp() {
        return timeStamp;
    }

    public double getVariation() {
        return variation;
    }

}
