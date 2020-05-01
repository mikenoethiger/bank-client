package bank.rabbitmq;

import java.util.Arrays;

/**
 * Request according to https://github.com/mikenoethiger/bank-server-socket#request
 */
public class MqRequest {
    /* actions are documented here https://github.com/mikenoethiger/bank-server-socket#actions */
    private final int actionId;
    private final String[] args;

    public MqRequest(int actionId, String[] args) {
        this.actionId = actionId;
        this.args = args;
    }

    public int getActionId() {
        return actionId;
    }

    public String[] getArgs() {
        return args;
    }

    @Override
    public String toString() {
        return "Request{" +
                "actionId=" + actionId +
                ", args=" + Arrays.toString(args) +
                '}';
    }
}
