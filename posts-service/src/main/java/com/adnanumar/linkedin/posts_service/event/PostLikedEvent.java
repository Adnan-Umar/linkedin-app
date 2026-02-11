package com.adnanumar.linkedin.posts_service.event;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PostLikedEvent {

    Long PostId;

    Long creatorId;

    Long likeByUserId;

}
