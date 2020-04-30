package bank.graphql;

import java.util.Arrays;

public class GraphQLResponse {

    private int statusCode;
    private String[] data;

    public GraphQLResponse(int statusCode) {
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String[] getData() {
        return data;
    }

    public void setData(String[] data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "GraphQLResponse{" +
                "statusCode=" + statusCode +
                ", data=" + Arrays.toString(data) +
                '}';
    }
}
