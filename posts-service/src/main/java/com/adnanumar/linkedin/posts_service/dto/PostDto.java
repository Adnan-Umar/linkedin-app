package com.adnanumar.linkedin.posts_service.dto;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PostDto {

    Long id;

    String content;

    Long userId;

    LocalDateTime createdAt;

}
