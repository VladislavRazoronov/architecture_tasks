package facade;

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

    private List<WebClient> loggingWebClients;
    private WebClient messagesWebClient;

    public FacadeService() {
        loggingWebClients = List.of(
                WebClient.create("http://localhost:8082"),
                WebClient.create("http://localhost:8083"),
                WebClient.create("http://localhost:8084")
                );
        messagesWebClient = WebClient.create("http://localhost:8081");
    }

    public WebClient getRandomLoggingClient(){
        double ind = Math.random()*loggingWebClients.size();
        return loggingWebClients.get((int)ind);
    }

    public Mono<Void> addMessage(String text) {
        Message msg = new Message(UUID.randomUUID(), text);
        WebClient loggingWebClient = getRandomLoggingClient();
        logger.info( loggingWebClient.toString());

        return loggingWebClient.post()
                .uri("/log")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(msg), Message.class)
                .retrieve()
                .bodyToMono(Void.class);
    }

    public Mono<String> messages(){

        WebClient loggingWebClient = getRandomLoggingClient();

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
