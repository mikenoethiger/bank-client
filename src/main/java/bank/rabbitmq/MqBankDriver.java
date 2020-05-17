package bank.rabbitmq;

import bank.Bank;
import bank.BankDriver2;
import bank.protocol.Request;
import bank.protocol.Response;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.rabbitmq.client.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeoutException;

public class MqBankDriver implements BankDriver2 {

    private static final String DEFAULT_HOST = "localhost";
    private static final int DEFAULT_PORT = 5672;
    private static final String DEFAULT_USER = "guest";
    private static final String DEFAULT_PASS = "guest";
    private static final String DEFAULT_VIRTUAL_HOST = "/";

    private static final String RPC_QUEUE_NAME = "bank.requests";
    static final String UPDATES_EXCHANGE_NAME = "bank.updates";	// XXX würde ich auch noch als private deklarieren.
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private Bank bank;
    private MqConnection mqConnection;
    private final List<UpdateHandler> updateHandlers = new CopyOnWriteArrayList<>();

    @Override
    public void registerUpdateHandler(UpdateHandler updateHandler) throws IOException {
        updateHandlers.add(updateHandler);
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

        /* connection and channel creation */
        Connection connection;
        try {
            connection = factory.newConnection();
        } catch (TimeoutException e) {
            throw new IOException(e);
        }
        Channel channel = connection.createChannel();
        final String corrId = UUID.randomUUID().toString();
        this.mqConnection = new MqConnection(connection, channel, corrId);

        /* subscribe to account updates */
        channel.exchangeDeclare(UPDATES_EXCHANGE_NAME, "fanout");
        String queueName = channel.queueDeclare().getQueue();
        channel.queueBind(queueName, UPDATES_EXCHANGE_NAME, "");

        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
            System.out.println(" [x] Received '" + message + "'");
            
            // XXX der folgende fix sollte eigentlich ihr Problem lösen, d.h. die Idee ist, dass bei jeder Änderung mit getAccount die Daten aktualisiert werden.
            //     Aber der fix funktioniert nicht. Ich vermute, dass man innerhalb einer Notifikation nicht bereits Anfragen an die Queue schicken darf.
            // bank.getAccount(message);
            // Wenn ich es jedoch in einen eigenen Thread auslagere, dann geht es, d.h. die Lösung die ich in dieser Version habe funktioneirt.
            // Bei jeder Notifikation wird der Cache (mit HIlfe von getAccount) aktualisiert. Und auch erst danach wird der updateHandler aufgerufen.
            
//            for (UpdateHandler updateHandler : updateHandlers) {
//                updateHandler.accountChanged(message);
//            }
            
            new Thread(() ->  {
            	try {
					bank.getAccount(message);
	                for (UpdateHandler updateHandler : updateHandlers) {
	                    updateHandler.accountChanged(message);
	                }
				} catch (IOException e) {
					e.printStackTrace();
				}
            }).start();
        };
        channel.basicConsume(queueName, true, deliverCallback, consumerTag -> {	});

        bank = new MqBank(mqConnection);
    }

    @Override
    public void disconnect() throws IOException {
        bank = null;
        try {
            mqConnection.channel.close();
        } catch (TimeoutException e) {
            throw new IOException(e);
        }
        mqConnection.connection.close();
    }

    @Override
    public Bank getBank() {
        return bank;
    }

    public static Response sendRequest(Request request, MqConnection mqConnection) throws IOException {
        String replyQueueName = mqConnection.channel.queueDeclare().getQueue();

        AMQP.BasicProperties props = new AMQP.BasicProperties
                .Builder()
                .correlationId(mqConnection.corrId)
                .replyTo(replyQueueName)
                .build();

        String message = gson.toJson(request);

        mqConnection.channel.basicPublish(
                /* exchange:    */ "",            // Exchange: empty string is called "default exchang" which is a direct exchange.
                /* routing key: */ RPC_QUEUE_NAME,
                /* props:       */ props,
                /* body:        */ message.getBytes(StandardCharsets.UTF_8));


        final BlockingQueue<String> responseQueue = new ArrayBlockingQueue<>(1);

        String ctag = mqConnection.channel.basicConsume(replyQueueName, true, (consumerTag, delivery) -> {
            if (delivery.getProperties().getCorrelationId().equals(props.getCorrelationId())) {
                responseQueue.offer(new String(delivery.getBody(), "UTF-8"));
            }
        }, consumerTag -> {
        });

        String result;
        while (true) {
            try {
                result = responseQueue.take();
                break;
            } catch (InterruptedException e) {
            }
        }

        mqConnection.channel.basicCancel(ctag);
        return gson.fromJson(result, Response.class);
    }
}
