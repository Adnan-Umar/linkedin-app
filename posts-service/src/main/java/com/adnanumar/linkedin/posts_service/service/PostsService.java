package com.adnanumar.linkedin.posts_service.service;

import com.adnanumar.linkedin.posts_service.auth.UserContextHolder;
import com.adnanumar.linkedin.posts_service.dto.PostCreateRequestDto;
import com.adnanumar.linkedin.posts_service.dto.PostDto;
import com.adnanumar.linkedin.posts_service.entity.Post;
import com.adnanumar.linkedin.posts_service.exception.ResourceNotFoundException;
import com.adnanumar.linkedin.posts_service.repository.PostsRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
@Slf4j
public class PostsService {

    final PostsRepository postsRepository;

    final ModelMapper modelMapper;

    public PostDto createPost(PostCreateRequestDto postDto, Long userId) {
        log.info("Post Create by user with ID : {}", userId);
        Post post = modelMapper.map(postDto, Post.class);
        post.setUserId(userId);
        Post savedPost = postsRepository.save(post);
        return modelMapper.map(savedPost, PostDto.class);
    }

    public PostDto getPostById(Long postId) {
        log.info("Get Post by ID : {}", postId);
        Long userId = UserContextHolder.getCurrentUserId();
        Post post = postsRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with id " + postId));
        return modelMapper.map(post, PostDto.class);
    }

    public List<PostDto> getAllPostsOfUser(Long userId) {
        List<Post> posts = postsRepository.findByUserId(userId);
        return posts.stream()
                .map((element) -> modelMapper.map(element, PostDto.class))
                .collect(Collectors.toList());
    }

}
