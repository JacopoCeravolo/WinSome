package shared.communication;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.StringTokenizer;

public class Protocol {

    public final static String DELIMITER = ":";

    // ADD METHODS FOR REQUEST HANDLING
    
    public static void sendResponse(PrintWriter out, String response) {
        
        StringBuilder sb = new StringBuilder();

        // always send ok for now
        sb.append("OK");
        sb.append(":");
        sb.append(response.length() + "\n");

        sb.append(response);
        
        out.print(sb);
        out.flush();
    }

    public static String receiveResponse(BufferedReader in) throws IOException {

        StringBuilder sb = new StringBuilder();
        StringTokenizer header = new StringTokenizer(in.readLine(), DELIMITER);

        String code = header.nextToken();
        sb.append("\nCode: " + code + "\n");

        Integer length = Integer.parseInt(header.nextToken());
        sb.append("Length: " + length + "\n");


        char[] message = new char[length];
        int offset = 0;

        while (offset < length) {
            int count = in.read(message, offset, (length - offset));
            if (count == -1) break;
            offset += count;
        }

        sb.append("\nMessage:\n" + String.valueOf(message));

        return sb.toString();
    }


}


