package server.socialnetwork;

import java.util.ArrayList;
import java.util.List;

public class WinSomeWallet {


    private float totalAmount;
    private List<Transaction> history;

    public WinSomeWallet() {
        history = new ArrayList<>();
        totalAmount = 0;
    }

    public void addTransaction(Transaction transaction) {
        history.add(transaction);
        totalAmount += transaction.getVariation();
    }

    public String viewHistory() {

        StringBuilder sb = new StringBuilder("AMOUNT\t\t\tDATE\n");

        for (Transaction t : history) {
            sb.append(t.getVariation() + "\t" + t.getTimeStamp() + "\n");
        }

        sb.append("\nTOTAL:\t" + totalAmount + " [WinCoins]\n");

        return sb.toString();
    }
}