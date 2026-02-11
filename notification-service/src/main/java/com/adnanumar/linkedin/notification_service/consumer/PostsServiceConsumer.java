package com.adnanumar.linkedin.notification_service.consumer;

import com.adnanumar.linkedin.notification_service.clients.ConnectionsClient;
import com.adnanumar.linkedin.notification_service.dto.PersonDto;
import com.adnanumar.linkedin.notification_service.service.SendNotification;
import com.adnanumar.linkedin.posts_service.event.PostCreatedEvent;
import com.adnanumar.linkedin.posts_service.event.PostLikedEvent;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PostsServiceConsumer {

    final ConnectionsClient connectionsClient;

    final SendNotification sendNotification;

    @KafkaListener(topics = "post-created-topic")
    public void handlePostCreated(PostCreatedEvent postCreatedEvent) {
        log.info("Sending notification : handlePostCreated : {}", postCreatedEvent);
        List<PersonDto> connections = connectionsClient.getFirstConnections(postCreatedEvent.getCreatorId());

        for(PersonDto connection : connections){
            sendNotification.send(connection.getUserId(), "Your connection " + postCreatedEvent.getCreatorId()
                    + " has created a post, Check it out");
        }
    }

    @KafkaListener(topics = "post-liked-topic")
    public void handlePostLiked(PostLikedEvent postLikedEvent) {
        log.info("Sending notification : handlePostLiked : {}", postLikedEvent);
        String message = String.format("Your post, %d has been liked by %d", postLikedEvent.getPostId(),
                postLikedEvent.getLikeByUserId());

        sendNotification.send(postLikedEvent.getCreatorId(), message);
    }

}
