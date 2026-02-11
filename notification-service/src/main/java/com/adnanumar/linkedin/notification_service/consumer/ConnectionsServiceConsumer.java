package com.adnanumar.linkedin.notification_service.consumer;

import com.adnanumar.linkedin.connections_service.event.AcceptConnectionRequestEvent;
import com.adnanumar.linkedin.connections_service.event.SendConnectionRequestEvent;
import com.adnanumar.linkedin.notification_service.service.SendNotification;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class ConnectionsServiceConsumer {

    final SendNotification sendNotification;

    @KafkaListener(topics = "send-connection-request-topic")
    public void handleSendConnectionRequest(SendConnectionRequestEvent sendConnectionRequestEvent) {
        log.info("Handled connections: handleSendConnectionRequest : {}", sendConnectionRequestEvent);
        String message = String.format("You have received a connection request from user with id: %d",
                sendConnectionRequestEvent.getSenderId());
        sendNotification.send(sendConnectionRequestEvent.getReceiverId(), message);
    }

    @KafkaListener(topics = "accept-connection-request-topic")
    public void handleAcceptConnectionRequest(AcceptConnectionRequestEvent acceptConnectionRequestEvent) {
        log.info("Handled connections: handleAcceptConnectionRequest : {}", acceptConnectionRequestEvent);
        String message = String.format("Your connection request has been accepted by the user with id: %d",
                acceptConnectionRequestEvent.getReceiverId());
        sendNotification.send(acceptConnectionRequestEvent.getSenderId(), message);
    }

}
