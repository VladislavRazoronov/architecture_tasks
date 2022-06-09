package messages;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.stream.Collectors;

@RestController
public class MessageController {

    Logger logger = LoggerFactory.getLogger(MessageController.class);

    private final MessageService messageService;

    public MessageController(MessageService messageService){
        this.messageService = messageService;
        this.messageService.initiate_watching(logger);
    }

    @GetMapping("/message")
    public String user() {
        return messageService.messages();
    }
}
