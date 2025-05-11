package com.example.mindhaven;
import java.util.List;

public interface MessageConsumer {
    void accept(List<Message> messages);
}