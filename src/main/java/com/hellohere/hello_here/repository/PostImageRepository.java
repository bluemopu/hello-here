package com.hellohere.hello_here.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hellohere.hello_here.entity.PostImage;

public interface PostImageRepository extends JpaRepository<PostImage, Long> {

    List<PostImage> findByPost_IdOrderByIdAsc(Long postId);
}
