import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Collections;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;


public class CrptApi {
    private final TimeUnit timeUnit;
    private final int requestLimit;
    private final BlockingQueue<Long> requestTimestamps;

    public CrptApi(TimeUnit timeUnit, int requestLimit) {
        this.timeUnit = timeUnit;
        this.requestLimit = requestLimit;
        this.requestTimestamps = new LinkedBlockingQueue<>(requestLimit);
    }

    public void createDocument(Document document, String signature) {
        try {
            long currentTime = System.currentTimeMillis();

            if (!requestTimestamps.offer(currentTime)) {
                requestTimestamps.poll();
                requestTimestamps.offer(currentTime);
            }

            long millisInUnit = timeUnit.convert(1, TimeUnit.MILLISECONDS);
            long timeBwMaxAndMinStm = Collections.max(requestTimestamps) - Collections.min(requestTimestamps);
            long difTime = timeBwMaxAndMinStm - millisInUnit;

            if (requestTimestamps.size() == requestLimit && difTime > 0)
            {
                Thread.sleep(difTime);
                System.out.println(Thread.currentThread().getName() + " is sleeping");
            }

            makeApiRequest(document, signature);
            System.out.println("Request executed");

        } catch (IOException | InterruptedException | URISyntaxException e) {
            e.printStackTrace(); // Handle exception appropriately
        }
    }

    //Логика задания подсказывает, что этот метод может возвращать какой-нибудь DTO
    //но учитывая, что в задании нет требований к возвращаемому значению оставил метод void
    //Примерный код, возвращающий DTO приведен в комментарии в конце метода
    public void makeApiRequest(Document document, String signature) throws IOException, URISyntaxException, InterruptedException {

        String apiUrl = "https://ismp.crpt.ru/api/v3/lk/documents/create";

        ObjectMapper objectMapper = new ObjectMapper();
        String requestBody = objectMapper.writeValueAsString(document);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(apiUrl))
                .headers("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse response = HttpClient.newBuilder()
                .build()
                .send(request, HttpResponse.BodyHandlers.ofString());

        //SomeDTO dto = mapToDto(response.body());
        //return SomeDTO
    }

    //Условный статический класс для передачи в методы выполнения запроса
    static class Document {

    }
}
