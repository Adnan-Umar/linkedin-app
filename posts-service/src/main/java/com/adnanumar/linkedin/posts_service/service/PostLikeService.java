package com.adnanumar.linkedin.posts_service.service;

import com.adnanumar.linkedin.posts_service.auth.UserContextHolder;
import com.adnanumar.linkedin.posts_service.entity.Post;
import com.adnanumar.linkedin.posts_service.entity.PostLike;
import com.adnanumar.linkedin.posts_service.event.PostLikedEvent;
import com.adnanumar.linkedin.posts_service.exception.BadRequestException;
import com.adnanumar.linkedin.posts_service.exception.ResourceNotFoundException;
import com.adnanumar.linkedin.posts_service.repository.PostLikeRepository;
import com.adnanumar.linkedin.posts_service.repository.PostsRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PostLikeService {

    final PostLikeRepository postLikeRepository;

    final PostsRepository postsRepository;

    final KafkaTemplate<Long, PostLikedEvent> kafkaTemplate;

    public void likePost(Long postId) {
        Long userId = UserContextHolder.getCurrentUserId();
        log.info("Attempting to like the post with ID : {}", postId);

        Post post = postsRepository.findById(postId).orElseThrow(
                () -> new ResourceNotFoundException("Post not found with id: " + postId));

        boolean alreadyLiked = postLikeRepository.existsByUserIdAndPostId(userId, postId);
        if (alreadyLiked) {
            throw new BadRequestException("Cannot like the same post again.");
        }
        PostLike postLike = new PostLike();
        postLike.setPostId(postId);
        postLike.setUserId(userId);
        postLikeRepository.save(postLike);
        log.info("Posts with ID : {} liked successfully", postId);

        PostLikedEvent postLikedEvent = PostLikedEvent.builder()
                .PostId(postId)
                .likeByUserId(userId)
                .creatorId(post.getUserId())
                .build();

        kafkaTemplate.send("post-liked-topic", postId, postLikedEvent);
    }

    public void unlikePost(Long postId) {
        Long userId = UserContextHolder.getCurrentUserId();
        log.info("Attempting to un-like the post with ID : {}", postId);
        boolean exists = postsRepository.existsById(postId);
        if (!exists) {
            throw new ResourceNotFoundException("Post not found with id: " + postId);
        }

        boolean alreadyLiked = postLikeRepository.existsByUserIdAndPostId(userId, postId);
        if (!alreadyLiked) throw new BadRequestException("Cannot un-like which is not liked.");

        postLikeRepository.deleteByUserIdAndPostId(userId, postId);

        log.info("Posts with ID : {} un-liked successfully", postId);
    }

}
