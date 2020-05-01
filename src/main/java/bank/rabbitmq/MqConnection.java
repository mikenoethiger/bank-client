package bank.rabbitmq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

public class MqConnection {

    public final Connection connection;
    public final Channel channel;
    public final String corrId;

    public MqConnection(Connection connection, Channel channel, String corrId) {
        this.connection = connection;
        this.channel = channel;
        this.corrId = corrId;
    }
}
