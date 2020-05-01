package bank.rabbitmq;

import bank.socket.SocketResponse;

import java.util.Arrays;

/**
 * Response according to https://github.com/mikenoethiger/bank-server-socket#response
 */
public class MqResponse {

    /* response documentation can be found here https://github.com/mikenoethiger/bank-server-socket#actions */
    private final int statusCode; /* https://github.com/mikenoethiger/bank-server-socket#status-codes */
    private final String[] data;

    public MqResponse(int statusCode, String[] data) {
        this.statusCode = statusCode;
        this.data = data;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String[] getData() {
        return data;
    }

    public boolean ok() {
        return statusCode == SocketResponse.OK_STATUS_CODE;
    }

    @Override
    public String toString() {
        return "Response{" +
                "statusCode=" + statusCode +
                ", data=" + Arrays.toString(data) +
                '}';
    }

}
