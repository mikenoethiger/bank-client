package bank.socket;

import bank.Bank;

import java.io.IOException;
import java.net.Socket;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public class ConcurrencyTest {

    // private static final String IP = "192.168.1.136"; // raspberry
    private static final String IP = "178.128.198.205"; // do
    private static final int PORT = 5001;

    private static final String ACCOUNT_NUMBER = "CH5610000000000000000";

    private static final int POOL_SIZE = 10;
    private static final int REQUESTS_NUM = 10_000;

    private final Bank bank;
    private final Socket socket;
    private final ExecutorService pool;

    public static void main(String[] args) throws IOException, InterruptedException {
        new ConcurrencyTest();
    }

    ConcurrencyTest() throws IOException, InterruptedException {
        Driver d = new Driver();
        d.connect(new String[]{IP, String.valueOf(PORT)});
        bank = d.getBank();
        socket = d.socket;
        pool = Executors.newFixedThreadPool(POOL_SIZE);

        long start = System.currentTimeMillis();
        testConcurrentDepositWithdraw();
        long stop = System.currentTimeMillis();
        System.out.println("duration: " + (stop-start)/1000);

        d.disconnect();
    }

    private void runRequestsConcurrently(Request[] requests) throws IOException, InterruptedException {
        int step = requests.length/POOL_SIZE;

        for (int i = 0; i < requests.length; i += step) {
            Socket s = new Socket(IP, PORT);
            if (i + step >= requests.length) {
                pool.execute(new RequestSender(s, requests, i, requests.length));
            } else {
                pool.execute(new RequestSender(s, requests, i, i + step));
            }
            System.out.println("added task to pool");
        }
        pool.shutdown();
        pool.awaitTermination(180, TimeUnit.SECONDS);
    }

    public static class RequestSender implements Runnable {

        private final Socket socket;
        private final Request[] request;
        private final int begin;
        private final int end;

        public RequestSender(Socket socket, Request[] request, int begin, int end) {
            this.socket = socket;
            this.request = request;
            this.begin = begin;
            this.end = end;
        }

        @Override
        public void run() {
            for (int i = begin; i < end; i++) {
                try {
                    Request.sendRequest(request[i], socket);
                } catch (IOException e) { e.printStackTrace(); }
            }
            try {
                socket.close();
            } catch (IOException e) { e.printStackTrace(); }
        }
    }

    private void testConcurrentDepositWithdraw() throws IOException, InterruptedException {
        // String account = bank.createAccount("mike");
        // make sure there's enough money to prevent an overdraw
        int initial_balance = 10_000_000;
        // Request.sendRequest(new Request.Deposit(account, initial_balance), socket);

        int amount = 1;
        Request[] requests = new Request[REQUESTS_NUM];
        int i = 0;
        for (; i < REQUESTS_NUM/2; i++) {
            requests[i] = new Request.Deposit(ACCOUNT_NUMBER, amount);
        }
        for (; i < REQUESTS_NUM; i++) {
            requests[i] = new Request.Withdraw(ACCOUNT_NUMBER, amount);
        }
        shuffleArray(requests);
        shuffleArray(requests);

        runRequestsConcurrently(requests);

        int expectedBalance = initial_balance + (REQUESTS_NUM/2)*amount - (REQUESTS_NUM-(REQUESTS_NUM/2))*amount;

        Response res = Request.sendRequest(new Request.GetAccount(ACCOUNT_NUMBER), socket);
        double balance = Double.parseDouble(res.array()[3]);
        if (balance == expectedBalance) {
            System.out.println("passed testConcurrentDepositWithdraw");
        } else {
            System.out.println("failed testConcurrentDepositWithdraw: expected balance=" + expectedBalance + " but actual balance=" + balance);
        }
    }

    private void shuffleArray(Request[] requests) {
        Random rnd = ThreadLocalRandom.current();
        Request r;
        for (int i = requests.length - 1; i > 0; i--)
        {
            int index = rnd.nextInt(i + 1);
            r = requests[index];
            requests[index] = requests[i];
            requests[i] = r;
        }
    }

}
