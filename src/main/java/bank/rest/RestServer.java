package bank.rest;

import bank.Bank;
import bank.DefaultBank;
import org.glassfish.jersey.jdkhttp.JdkHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import javax.ws.rs.core.Application;
import java.net.URI;

public class RestServer extends Application {

    public static final Bank BANK = new DefaultBank();

    public static void main(String[] args) throws Exception {
        URI baseUri = new URI("http://localhost:8080/");

        // @Singleton annotations will be respected
        ResourceConfig rc = new ResourceConfig(AccountResource.class, BankResource.class);

        // Create and start the JDK HttpServer with the Jersey application
        JdkHttpServerFactory.createHttpServer(baseUri, rc);
    }
}
