package bank.graphql;

import bank.Bank;
import bank.BankDriver;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class GraphQLBankDriver implements BankDriver {

    private static final String DEFAULT_HOST = "localhost";
    private static final int    DEFAULT_PORT = 5002;

    private static HttpClient client;
    private static String host;
    private static int port;
    private static Bank bank;

    @Override
    public void connect(String[] args) throws IOException {
        if (args.length > 1) {
            host = args[0];
            port = Integer.valueOf(args[1]);
        } else if (args.length > 0) {
            host = args[0];
        } else {
            host = DEFAULT_HOST;
            port = DEFAULT_PORT;
        }
        this.client = HttpClient.newHttpClient();
        this.bank = new GraphQLBank();
    }

    @Override
    public void disconnect() throws IOException {
        client = null;
        host = null;
        port = 0;
    }

    @Override
    public Bank getBank() {
        return bank;
    }

    static JsonObject sendRequest(String graphqlQuery) {
        if (client == null) throw new IllegalStateException("call connect() before sending requests");

        ObjectMapper objectMapper = new ObjectMapper();
        String requestBody = null;
        try {
            requestBody = objectMapper
                    .writerWithDefaultPrettyPrinter()
                    .writeValueAsString(new Query(graphqlQuery));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        HttpRequest.BodyPublisher body = HttpRequest.BodyPublishers.ofString(requestBody);
        HttpRequest request;
        try {
            request = HttpRequest.newBuilder()
                    .uri(new URI(String.format("http://%s:%s/graphql", host, port)))
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .POST(body)
                    .build();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

        HttpResponse<String> response;
        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonObject jsonObject = gson.fromJson(response.body(), JsonObject.class);
        return jsonObject.getAsJsonObject("data");
    }

    static class Query {
        private final String query;
        private final String variables;

        public Query(String query, String variables) {
            this.query = query;
            this.variables = variables;
        }

        public Query(String query) {
            this(query, null);
        }

        public String getQuery() {
            return query;
        }

        public String getVariables() {
            return variables;
        }
    }
}
