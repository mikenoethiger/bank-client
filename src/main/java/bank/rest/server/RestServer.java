package bank.rest.server;

import bank.Bank;
import bank.protocol.InMemoryBank;
import org.glassfish.jersey.jdkhttp.JdkHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import javax.ws.rs.core.Application;
import java.net.URI;

public class RestServer extends Application {

    public static final String SERVER = "http://localhost:8080";
    public static final Bank BANK = new InMemoryBank();

    public static void main(String[] args) throws Exception {
        URI baseUri = new URI(SERVER + "/");

        // @Singleton annotations will be respected
        ResourceConfig rc = new ResourceConfig(BankResource.class);

        // Create and start the JDK HttpServer with the Jersey application
        JdkHttpServerFactory.createHttpServer(baseUri, rc);
    }
}
