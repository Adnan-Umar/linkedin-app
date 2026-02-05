package com.adnanumar.linkedin.posts_service.controller;

import com.adnanumar.linkedin.posts_service.dto.PostCreateRequestDto;
import com.adnanumar.linkedin.posts_service.dto.PostDto;
import com.adnanumar.linkedin.posts_service.service.PostsService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/posts")
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class PostsController {

    final PostsService postsService;

    @PostMapping
    public ResponseEntity<PostDto> createPost(@RequestBody PostCreateRequestDto postDto,
                                              HttpServletRequest httpServletRequest) {
        PostDto createdPost = postsService.createPost(postDto, 1L);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdPost);
    }

    @GetMapping("/{postId}")
    public ResponseEntity<PostDto> getPostById(@PathVariable Long postId) {
        PostDto postDto = postsService.getPostById(postId);
        return ResponseEntity.ok(postDto);
    }

    @GetMapping("/users/{userId}/allPosts")
    public ResponseEntity<List<PostDto>> getAllPostsOfUser(@PathVariable Long userId) {
        List<PostDto> posts = postsService.getAllPostsOfUser(userId);
        return ResponseEntity.ok(posts);
    }

}
