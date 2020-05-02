package bank.websocket;

import bank.protocol.Request;
import bank.protocol.Response;

import javax.websocket.*;
import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

@ClientEndpoint
public class WsConnection {

    /* used to enforce synchronous (request/response) communication */
    private final BlockingQueue<String> messageQueue = new ArrayBlockingQueue<>(1);
    private Session session;

    synchronized public Response sendRequestSynchronous(Request request) throws IOException {
        if (session == null) throw new IllegalStateException("set session first");

        session.getBasicRemote().sendText(request.toString());
        String message;
        while (true) {
            try {
                message = this.messageQueue.take();
                break;
            } catch (InterruptedException e) { }
        }
        return Response.fromString(message);
    }

    @OnMessage
    public void onMessage(Session session, String message) throws IOException {
        while (true) {
            try {
                this.messageQueue.put(message);
                break;
            } catch (InterruptedException e) { }
        }
    }

    @OnClose
    public void onClose(Session session, CloseReason closeReason) {
        System.out.printf("[%s] Session %s closed because of %s\n", Thread.currentThread(), session.getId(), closeReason);
    }

    @OnError
    public void onError(Throwable exception, Session session) {
        System.out.println("an error occured on connection " + session.getId() + ":" + exception);
    }

    public void setSession(Session session) {
        this.session = session;
    }
}
