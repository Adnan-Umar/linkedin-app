package com.adnanumar.linkedin.connections_service.event;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class SendConnectionRequestEvent {

    Long senderId;

    Long receiverId;

}
