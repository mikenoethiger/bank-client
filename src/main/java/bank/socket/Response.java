package bank.socket;

public class Response {

    private static final String OK = "ok";

    /* error codes according to protocol (see github.com/mikenoethiger/bank-server) */
    public static final int ERROR_INTERNAL_ERROR = 0;
    public static final int ERROR_ACCOUNT_DOES_NOT_EXIST = 1;
    public static final int ERROR_ACCOUNT_COULD_NOT_BE_CREATED = 2;
    public static final int ERROR_ACCOUNT_COULD_NOT_BE_CLOSED = 3;
    public static final int ERROR_INACTIVE_ACCOUNT = 4;
    public static final int ERROR_ACCOUNT_OVERDRAW = 5;
    public static final int ERROR_ILLEGAL_ARGUMENT = 6;
    public static final int ERROR_BAD_REQUEST = 7;

    private final String[] response;

    public Response(String[] response) {
        this.response = response;
    }

    public boolean isOK() {
        return OK.equalsIgnoreCase(response[0]);
    }

    public int getErrorCode() {
        if (isOK()) throw new UnsupportedOperationException();
        return Integer.parseInt(response[1]);
    }

    public String getErrorText() {
        if (isOK()) throw new UnsupportedOperationException();
        return response[2];
    }

    public String[] array() {
        return response;
    }
}
