package bank.socket;

public class SocketResponse {

    private static final int OK_STATUS_CODE = 0;

    /* error codes according to protocol (see github.com/mikenoethiger/bank-server) */
    public static final int ERROR_ACCOUNT_DOES_NOT_EXIST = 1;
    public static final int ERROR_ACCOUNT_COULD_NOT_BE_CREATED = 2;
    public static final int ERROR_ACCOUNT_COULD_NOT_BE_CLOSED = 3;
    public static final int ERROR_INACTIVE_ACCOUNT = 4;
    public static final int ERROR_ACCOUNT_OVERDRAW = 5;
    public static final int ERROR_ILLEGAL_ARGUMENT = 6;
    public static final int ERROR_BAD_REQUEST = 7;
    public static final int ERROR_INTERNAL_ERROR = 8;

    private final String[] response;

    public SocketResponse(String[] response) {
        this.response = response;
    }

    public boolean isOK() {
        return OK_STATUS_CODE == getStatusCode();
    }

    public int getStatusCode() {
        return Integer.parseInt(response[0]);
    }

    public String getErrorText() {
        if (isOK()) throw new UnsupportedOperationException();
        return response[1];
    }

    public String[] array() {
        return response;
    }
}
