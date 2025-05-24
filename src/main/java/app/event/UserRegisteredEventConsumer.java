package app.event;

import app.event.payload.UserRegisteredEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class UserRegisteredEventConsumer {

    @KafkaListener (topics = "user-registered-event.v1", groupId = "notification-svc")
    public void consumeEvent(UserRegisteredEvent event) {
        System.out.println("Successfully consumed user registered event: " + event);
    }
}
