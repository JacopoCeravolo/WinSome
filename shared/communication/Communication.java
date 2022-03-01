package shared.communication;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.StringTokenizer;

public class Communication {

    public final static String DELIMITER = ":";

    // ADD METHODS FOR REQUEST HANDLING
    public static void packRequest(PrintWriter out, String requestType, String parameters) {

        StringBuilder request = new StringBuilder();

        request.append(requestType + DELIMITER);
        request.append(parameters.length() + DELIMITER);
        
    }
    
    public static void sendResponse(PrintWriter out, String responseLine) {
        
        StringBuilder sb = new StringBuilder();

        sb.append("OK" + DELIMITER);
   
        sb.append(responseLine.length() + "\n");
       
        sb.append(responseLine);
       
        out.print(sb);
        
        out.flush();
        
    }

    public static String receiveResponse(BufferedReader in) throws IOException {

        StringBuilder sb = new StringBuilder();
       
        StringTokenizer header = new StringTokenizer(in.readLine(), DELIMITER);
        

        String code = header.nextToken();

        Integer length = Integer.parseInt(header.nextToken());
    

        char[] message = new char[length];
        int offset = 0;

        while (offset < length) {
            int count = in.read(message, offset, (length - offset));
            if (count == -1) break;
            offset += count;
        }
        
        sb.append(String.valueOf(message));

        return sb.toString();
    }


}


