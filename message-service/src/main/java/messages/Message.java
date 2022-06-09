package messages;

import lombok.AllArgsConstructor;

import java.util.UUID;

@AllArgsConstructor
public class Message {
    public UUID id;
    public String txt;

}
