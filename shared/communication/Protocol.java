package shared.communication;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.StringTokenizer;

public class Protocol {

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

    public static void sendRequest(PrintWriter out, String action, String... parameters) {

        StringBuilder sb = new StringBuilder();
        
        long length = 0;
        for (String param : parameters) {
            length += param.length();
        }

        sb.append(action + DELIMITER);
        sb.append((length + parameters.length)  + "\n");

        for (String param : parameters) {
            sb.append(param + DELIMITER);
        }

        out.print(sb);
        out.flush();
    }

    public static String receiveRequest(BufferedReader in) throws IOException {

        StringBuilder sb = new StringBuilder();

        StringTokenizer parser = new StringTokenizer(in.readLine(), DELIMITER);

        String action = parser.nextToken();

        Integer length = Integer.parseInt(parser.nextToken());

        char[] message = new char[length];
        int offset = 0;

        while (offset < length) {
            int count = in.read(message, offset, (length - offset));
            if (count == -1) break;
            offset += count;
        }

        sb.append(action + DELIMITER);
        sb.append(String.valueOf(message));

        return sb.toString();
    }

    public static String receiveResponse(BufferedReader in) throws IOException {

        StringBuilder sb = new StringBuilder();

        String firstLine = in.readLine();
        StringTokenizer header = new StringTokenizer(firstLine, DELIMITER);
        

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


