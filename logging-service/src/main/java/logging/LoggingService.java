package logging;

import java.util.Map;
import java.util.UUID;

public interface LoggingService {

    void addToLog(Message msg);

    Map<UUID,String> log();
}
