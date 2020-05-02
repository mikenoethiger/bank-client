package bank.websocket;

import bank.Bank;
import bank.BankDriver2;
import org.glassfish.tyrus.client.ClientManager;

import javax.websocket.DeploymentException;
import javax.websocket.Session;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class WsBankDriver implements BankDriver2 {

    public static final String DEFAULT_HOST = "localhost";
    public static final int DEFAULT_PORT = 5003;

    private Session session;
    private WsBank bank;

    @Override
    public void registerUpdateHandler(UpdateHandler updateHandler) throws IOException {
    }

    @Override
    public void connect(String[] argv) throws IOException {
        String host = DEFAULT_HOST;
        int port = DEFAULT_PORT;
        if (argv.length > 0) host = argv[0];
        if (argv.length > 1) port = Integer.valueOf(argv[1]);

        // URI url = new URI("ws://echo.websocket.org/");
        URI url;
        try {
            url = new URI(String.format("ws://%s:%s/bank", host, port));
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

        ClientManager client = ClientManager.createClient();
        WsConnection connection = new WsConnection();
        try {
            session = client.connectToServer(connection, url);
        } catch (DeploymentException e) {
            throw new IOException(e);
        }

        connection.setSession(session);
        bank = new WsBank(connection);
    }


    @Override
    public void disconnect() throws IOException {
        session.close();
        session = null;
        bank = null;
    }

    @Override
    public Bank getBank() {
        return this.bank;
    }

}
