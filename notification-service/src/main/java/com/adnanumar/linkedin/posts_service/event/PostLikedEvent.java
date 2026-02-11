package com.adnanumar.linkedin.posts_service.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
public class PostLikedEvent {

    Long PostId;

    Long creatorId;

    Long likeByUserId;

}
