package server.socialnetwork;

import java.util.ArrayList;
import java.util.List;

public class Wallet {

    private float totalAmount;
    private List<Transaction> history;

    public Wallet() {
        history = new ArrayList<>();
        totalAmount = 0;
    }

    public void addTransaction(Transaction transaction) {
        history.add(transaction);
        totalAmount += transaction.getVariation();
    }

    public String viewHistory() {

        StringBuilder sb = new StringBuilder("AMOUNT\tDATE\n");

        for (Transaction t : history) {
            sb.append( String.format("%.2f", t.getVariation()) + "\t" + t.getTimeStamp() + "\n");
        }

        sb.append("\nTOTAL:\t" + totalAmount + " [WinCoins]\n");

        return sb.toString();
    }
}