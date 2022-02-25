package shared.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.Socket;
import java.util.StringTokenizer;

public class Utils {
    
    public final static String RMI_SERVICE_NAME = "WinSomeRegistration";

    public String receive(BufferedReader in) throws IOException {

        StringBuilder full = new StringBuilder();
        StringTokenizer header = new StringTokenizer(in.readLine());

        switch (header.nextToken()) {
            case "request":
                
                break;
        
            case "response":

                break;

            default:

                break;
        }
        
        return full.toString();
    }

}
