package messages;

import com.hazelcast.collection.IQueue;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MessageService {
    private List<String> messageList;
    private HazelcastInstance hz = Hazelcast.newHazelcastInstance();
    private IQueue<String> mQueue = hz.getQueue("message_queue");

    public void initiate_watching(Logger logger){
        messageList = new ArrayList<>();
        Runnable watcher = new Runnable() {
            @Override
            public void run() {
                while(true){
                    try {
                        String msg = mQueue.take();
                        messageList.add(msg);
                        logger.info(msg);
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };

        Thread thread = new Thread(watcher);

        thread.start();
    }

    public String messages(){
        return messageList.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(",", "{", "}"));
    }
}
