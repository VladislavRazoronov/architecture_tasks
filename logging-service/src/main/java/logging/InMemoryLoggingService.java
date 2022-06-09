package logging;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Primary
public class InMemoryLoggingService implements LoggingService {

    private Map<UUID, String> messages = new ConcurrentHashMap<>();

    @Override
    public void addToLog(Message msg){
        System.out.println(msg.id);
        messages.put(msg.id,msg.txt);}

    @Override
    public Map<UUID,String> log(){return messages;}
}
