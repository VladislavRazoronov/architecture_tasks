package logging;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Primary
public class HazelcastLoggingService implements LoggingService {

    private HazelcastInstance hc = Hazelcast.newHazelcastInstance();
    private Map<UUID,String> messages = hc.getMap("messages");

    @Override
    public void addToLog(Message msg){
        System.out.println(msg.id);
        messages.put(msg.id,msg.txt);}

    @Override
    public Map<UUID,String> log(){return messages;}
}