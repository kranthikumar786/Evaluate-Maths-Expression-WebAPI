package org.example;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
public class MathExpressionAPIEvaluator {
    private static final int MAX_REQUESTS_PER_SECOND = 500; // Adjust the rate limit as needed for an application
    private static final RateLimiter rateLimiter = new RateLimiter(MAX_REQUESTS_PER_SECOND);

    public static void main(String[] args) {
        //long startTime = System.currentTimeMillis();
        List<Client> clients = new ArrayList<>();
        for(int i=0;i<MAX_REQUESTS_PER_SECOND/50;i++){
            Client client = new Client(50, i+1);
            clients.add(client); // Each client will be serverd only 50 requests   per sec .
        }
        ExecutorService executorService = Executors.newCachedThreadPool();
        try {
            File file = new File("give the input path file extension as well");
            Scanner scanner = new Scanner(file);

           // System.out.println("Reading mathematical expressions from file:");

            while (scanner.hasNextLine()) {
                String expression = scanner.nextLine();

                if (expression.equalsIgnoreCase("end")) {
                    break;
                }

                // Check if the rate limit allows the request
                if (rateLimiter.allowRequest()) {
                    executorService.submit(() -> {
                        for(Client c : clients){
                            if (c.rateLimiter.allowRequest()){
                                 // Here we are conforming that only 50 request were serving to one user/ second
                                // System.out.println("Serving request from clientId: "+c.clientId);
                                 // feel free uncommnet above print to check how many requests are serving per user/second
                                String result = null;
                                try {
                                    result = evaluateExpression(expression);  // callling evaluateExpression() method which ideally calling web public api for evaluation.
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                                System.out.println(expression + "=" + result);
                                break;
                            }
                        }
                    });
                } else {
                    System.out.println("Rate limit exceeded. Request blocked.");
                    // if we are making sure that if more than 50 request /user/sec not allowed
                }
            }
            scanner.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            executorService.shutdown();
        }
        // long endTime = System.currentTimeMillis();
        //System.out.println("Total execution time: " + (endTime - startTime) + " milliseconds");
        // just to check time feel free to uncomment above  2 lines of code  and line number 16 starting time
    }

    private static String evaluateExpression(String expression) throws IOException {
        String apiUrl = "https://api.mathjs.org/v4/"; // using web api for mathematical expressions calcualtion where ^ is working like expoentional not like pow()
        String encodedExpression = URLEncoder.encode(expression, "UTF-8"); // i have encode expression inorder to use with web public api
        String fullUrl = apiUrl + "?expr=" + encodedExpression;

        URL url = new URL(fullUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                response.append(line);
            }

            return response.toString();
        } finally {
            connection.disconnect();
        }
    }

    static class RateLimiter {
        private final int maxRequestsPerSecond;
        private long lastRequestTime;
        private int tokens;

        public RateLimiter(int maxRequestsPerSecond) {
            this.maxRequestsPerSecond = maxRequestsPerSecond;
            this.lastRequestTime = System.currentTimeMillis();
            this.tokens = maxRequestsPerSecond;
        }

        public synchronized boolean allowRequest() {
            refillTokens();

            if (tokens > 0) {
                tokens--;     // this will make sure of count that should be only 50 in our case / user / sec.
                return true;
            } else {
                return false;
            }
        }

        private void refillTokens() {
            long currentTime = System.currentTimeMillis();
            long elapsedTime = currentTime - lastRequestTime;
            int tokensToAdd = (int) ((elapsedTime * maxRequestsPerSecond) / 1000);

            if (tokensToAdd > 0) {
                tokens = Math.min(tokens + tokensToAdd, maxRequestsPerSecond);
                lastRequestTime = currentTime;
            }
        }
    }
}

class Client {
    public int maxRequests;
    public int clientId;
    public MathExpressionAPIEvaluator.RateLimiter rateLimiter;

    public Client(int maxRequests, int clientId) {
        this.clientId = clientId;
        this.maxRequests = maxRequests;
        this.rateLimiter = new MathExpressionAPIEvaluator.RateLimiter(this.maxRequests);
    }
}
