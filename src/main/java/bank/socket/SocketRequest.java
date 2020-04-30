package bank.socket;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class SocketRequest {

    private static final int ACTION_GET_ACCOUNT_NUMBERS = 1;
    private static final int ACTION_GET_ACCOUNT = 2;
    private static final int ACTION_CREATE_ACCOUNT = 3;
    private static final int ACTION_CLOSE_ACCOUNT = 4;
    private static final int ACTION_TRANSFER = 5;
    private static final int ACTION_DEPOSIT = 6;
    private static final int ACTION_WITHDRAW = 7;

    private final String[] request;

    public SocketRequest(String[] request) {
        if (request.length < 1) throw new IllegalArgumentException();
        this.request = request;
        for (String s : request) {
            if (s == null || s.length() == 0) throw new IllegalArgumentException("request arguments must not be empty");
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (String s : request) {
            sb.append(s);
            sb.append("\n");
        }
        sb.append("\n");
        return sb.toString();
    }

    /**
     * Read a line from input stream.
     * Appends all bytes to string builder (except the line break.)
     *
     * @param sb string builder to append bytes to
     * @param in input stream to read bytes from
     * @return number of bytes read (including the line break)
     * @throws IOException
     */
    private static int readLine(StringBuilder sb, InputStream in) throws IOException {
        int bytes = 0;
        int buf;
        while ((buf = in.read()) != -1) {
            bytes++;
            if ((char) buf == '\n') break;
            sb.append((char) buf);
        }
        return bytes;
    }

    /**
     * Write a string to out.
     *
     * Writes each character as 1 byte as opposed to
     * {@link DataOutputStream#writeChars(String)}
     * which writes each character as 2 bytes.
     *
     * @param out stream to write to
     * @param s string to write
     * @throws IOException
     */
    private static void writeString(OutputStream out, String s) throws IOException {
        for (int i = 0; i < s.length(); i++) {
            out.write(s.charAt(i));
        }
    }

    /**
     * Encode socketRequest and write to socket.
     * Encode means translating the socketRequest into protocol text.
     *
     * @param socketRequest
     * @return the socket response
     */
    public static SocketResponse sendRequest(SocketRequest socketRequest, Socket socket) throws IOException {
        // write socketRequest
        writeString(socket.getOutputStream(), socketRequest.toString());

        // read response
        InputStream in = socket.getInputStream();
        StringBuilder sb = new StringBuilder();
        List<String> response = new ArrayList<>();
        while (readLine(sb, in) > 1) {
            response.add(sb.toString());
            sb = new StringBuilder();
        }

        String[] response_arr = new String[response.size()];
        response_arr = response.toArray(response_arr);
        return new SocketResponse(response_arr);
    }

    public static class GetAccountNumbers extends SocketRequest {
        public GetAccountNumbers() {
            super(new String[]{String.valueOf(ACTION_GET_ACCOUNT_NUMBERS)});
        }
    }

    public static class CreateAccount extends SocketRequest {
        public CreateAccount(String owner) {
            super(new String[]{ String.valueOf(ACTION_CREATE_ACCOUNT), owner });
        }
    }

    public static class CloseAccount extends SocketRequest {
        public CloseAccount(String account) {
            super(new String[]{ String.valueOf(ACTION_CLOSE_ACCOUNT), account });
        }
    }

    public static class GetAccount extends SocketRequest {
        public GetAccount(String account) {
            super(new String[]{ String.valueOf(ACTION_GET_ACCOUNT), account });
        }
    }

    public static class Transfer extends SocketRequest {
        public Transfer(String from_account, String to_account, double amount) {
            super(new String[]{ String.valueOf(ACTION_TRANSFER), from_account, to_account, String.valueOf(amount) });
        }
    }

    public static class Deposit extends SocketRequest {
        public Deposit(String account, double amount) {
            super(new String[]{ String.valueOf(ACTION_DEPOSIT), account, String.valueOf(amount) });
        }
    }

    public static class Withdraw extends SocketRequest {
        public Withdraw(String account, double amount) {
            super(new String[]{ String.valueOf(ACTION_WITHDRAW), account, String.valueOf(amount) });
        }
    }
}
