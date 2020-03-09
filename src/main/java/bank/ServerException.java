package bank;

/**
 *
 */
public class ServerException extends RuntimeException {

    public ServerException() {
        super();
    }

    public ServerException(String reason) {
        super(reason);
    }

    public ServerException(String reason, Throwable cause) {
        super(reason, cause);
    }

    public ServerException(Throwable cause) {
        super(cause);
    }

}
