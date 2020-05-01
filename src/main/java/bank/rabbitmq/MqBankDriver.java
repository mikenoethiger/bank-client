package bank.rabbitmq;

import bank.Bank;
import bank.BankDriver2;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeoutException;

public class MqBankDriver implements BankDriver2 {

    private static final String DEFAULT_HOST = "localhost";
    private static final int DEFAULT_PORT = 5672;
    private static final String DEFAULT_USER = "guest";
    private static final String DEFAULT_PASS = "guest";
    private static final String DEFAULT_VIRTUAL_HOST = "/";

    private static final String RPC_QUEUE_NAME = "bank.requests";
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private Bank bank;

    @Override
    public void registerUpdateHandler(UpdateHandler updateHandler) throws IOException {

    }

    @Override
    public void connect(String[] argv) throws IOException {
        String[] credentials = new String[]{DEFAULT_HOST, String.valueOf(DEFAULT_PORT), DEFAULT_USER, DEFAULT_PASS, DEFAULT_VIRTUAL_HOST};
        // override default credentials with argv parameters if available
        System.arraycopy(argv, 0, credentials, 0, argv.length);

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(credentials[0]);
        factory.setPort(Integer.valueOf(credentials[1]));
        factory.setUsername(credentials[2]);
        factory.setPassword(credentials[3]);
        factory.setVirtualHost(credentials[4]);

        bank = new MqBank(factory);
    }

    @Override
    public void disconnect() throws IOException {
        bank = null;
    }

    @Override
    public Bank getBank() {
        return bank;
    }

    public static MqResponse sendRequest(MqRequest request, ConnectionFactory factory) throws IOException {
        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {

            channel.queueDeclare(RPC_QUEUE_NAME,
                    /* durable:    */ false,
                    /* exclusive:  */ false,
                    /* autoDelete: */ false,
                    /* arguments:  */ null);

            final String corrId = UUID.randomUUID().toString();

            String replyQueueName = channel.queueDeclare().getQueue();
            AMQP.BasicProperties props = new AMQP.BasicProperties
                    .Builder()
                    .correlationId(corrId)
                    .replyTo(replyQueueName)
                    .build();

            String message = gson.toJson(request);

            channel.basicPublish(
                    /* exchange:    */ "",            // Exchange: empty string is called "default exchang" which is a direct exchange.
                    /* routing key: */ RPC_QUEUE_NAME,
                    /* props:       */ props,
                    /* body:        */ message.getBytes(StandardCharsets.UTF_8));


            final BlockingQueue<String> responseQueue = new ArrayBlockingQueue<>(1);

            String ctag = channel.basicConsume(replyQueueName, true, (consumerTag, delivery) -> {
                if (delivery.getProperties().getCorrelationId().equals(corrId)) {
                    responseQueue.offer(new String(delivery.getBody(), "UTF-8"));
                }
            }, consumerTag -> {
            });

            String result = responseQueue.take();
            channel.basicCancel(ctag);
            return gson.fromJson(result, MqResponse.class);
        } catch (TimeoutException | InterruptedException e) {
            throw new IOException(e);
        }
    }
}
