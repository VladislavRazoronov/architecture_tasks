package facade;

import com.hazelcast.collection.IQueue;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@Service
public class FacadeService {

    Logger logger = LoggerFactory.getLogger(FacadeService.class);
    private HazelcastInstance hz = Hazelcast.newHazelcastInstance();
    private IQueue<String> mQueue = hz.getQueue("message_queue");

    private List<WebClient> loggingWebClients;
    private List<WebClient> messagesWebClients;

    public FacadeService() {
        loggingWebClients = List.of(
                WebClient.create("http://localhost:8083"),
                WebClient.create("http://localhost:8084"),
                WebClient.create("http://localhost:8085")
                );
        messagesWebClients = List.of(
                WebClient.create("http://localhost:8081"),
                WebClient.create("http://localhost:8082")
        );
    }

    public WebClient getRandomLoggingClient(){
        double ind = Math.random()*loggingWebClients.size();
        return loggingWebClients.get((int)ind);
    }

    public WebClient getRandomMessageClient(){
        double ind = Math.random()*messagesWebClients.size();
        return messagesWebClients.get((int)ind);
    }

    public Mono<Void> addMessage(String text) throws InterruptedException {
        Message msg = new Message(UUID.randomUUID(), text);
        WebClient loggingWebClient = getRandomLoggingClient();
        logger.info( loggingWebClient.toString());

        mQueue.put(text);

        return loggingWebClient.post()
                .uri("/log")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(msg), Message.class)
                .retrieve()
                .bodyToMono(Void.class);
    }

    public Mono<String> messages(){

        WebClient loggingWebClient = getRandomLoggingClient();

        WebClient messagesWebClient = getRandomMessageClient();

        Mono<String> logValuesMono = loggingWebClient.get()
                .uri("/log")
                .retrieve()
                .bodyToMono(String.class);

        Mono<String> messageMono = messagesWebClient.get()
                .uri("/message")
                .retrieve()
                .bodyToMono(String.class);
        return logValuesMono.zipWith(messageMono,
                (logValues,message) -> logValues + ": " + message)
                .onErrorReturn("Error");
    }
}
