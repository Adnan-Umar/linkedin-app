package com.adnanumar.linkedin.posts_service.service;

import com.adnanumar.linkedin.posts_service.entity.PostLike;
import com.adnanumar.linkedin.posts_service.exception.BadRequestException;
import com.adnanumar.linkedin.posts_service.exception.ResourceNotFoundException;
import com.adnanumar.linkedin.posts_service.repository.PostLikeRepository;
import com.adnanumar.linkedin.posts_service.repository.PostsRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PostLikeService {

    final PostLikeRepository postLikeRepository;

    final PostsRepository postsRepository;

    public void likePost(Long postId, Long userId) {
        log.info("Attempting to like the post with ID : {}", postId);
        boolean exists = postsRepository.existsById(postId);
        if (!exists) {
            throw new ResourceNotFoundException("Post not found with id: " + postId);
        }

        boolean alreadyLiked = postLikeRepository.existsByUserIdAndPostId(userId, postId);
        if (alreadyLiked) {
            throw new BadRequestException("Cannot like the same post again.");
        }
        PostLike postLike = new PostLike();
        postLike.setPostId(postId);
        postLike.setUserId(userId);
        postLikeRepository.save(postLike);
        log.info("Posts with ID : {} liked successfully", postId);
    }

}
