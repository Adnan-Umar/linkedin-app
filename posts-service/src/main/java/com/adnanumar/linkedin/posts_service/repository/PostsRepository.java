package com.adnanumar.linkedin.posts_service.repository;

import com.adnanumar.linkedin.posts_service.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PostsRepository extends JpaRepository<Post, Long> {
}
