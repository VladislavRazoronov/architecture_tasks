package facade;

import com.hazelcast.collection.IQueue;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Optional;
import java.util.UUID;

@Service
public class FacadeService {

    @Autowired
    private DiscoveryClient discoveryClient;

    public Optional<Integer> servicePort(String service_name) {
        return discoveryClient.getInstances(service_name)
                .stream()
                .findFirst()
                .map(ServiceInstance::getPort);
    }

    Logger logger = LoggerFactory.getLogger(FacadeService.class);
    private HazelcastInstance hz = Hazelcast.newHazelcastInstance();
    private IQueue<String> mQueue = hz.getQueue("message_queue");

    public FacadeService() {

    }

    public Mono<Void> addMessage(String text) throws InterruptedException {
        Message msg = new Message(UUID.randomUUID(), text);
        WebClient loggingWebClient = WebClient.create("http://localhost"+servicePort("Logging"));
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

        WebClient loggingWebClient = WebClient.create("http://localhost"+servicePort("Logging"));

        WebClient messagesWebClient = WebClient.create("http://localhost"+servicePort("Messages"));

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
