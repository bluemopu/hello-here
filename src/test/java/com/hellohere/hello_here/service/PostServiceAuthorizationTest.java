package com.hellohere.hello_here.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.hellohere.hello_here.entity.Post;
import com.hellohere.hello_here.repository.PostImageRepository;
import com.hellohere.hello_here.repository.PostRepository;
import com.hellohere.hello_here.repository.UserRepository;

class PostServiceAuthorizationTest {

    private final PostRepository postRepository = mock(PostRepository.class);
    private final UserRepository userRepository = mock(UserRepository.class);
    private final PostImageRepository postImageRepository = mock(PostImageRepository.class);
    private final PostService postService = new PostService(
            postRepository, userRepository, postImageRepository);

    @Test
    void ownerCanSoftDeleteOwnPost() {
        Post post = new Post();
        when(postRepository.findByIdAndUser_IdAndDeletedAtIsNull(10L, 20L))
                .thenReturn(Optional.of(post));

        postService.deleteUserPost(10L, 20L);

        assertNotNull(post.getDeletedAt());
        assertNotNull(post.getUpdatedAt());
        verify(postRepository).saveAndFlush(post);
        verifyNoInteractions(userRepository, postImageRepository);
    }

    @Test
    void nonOwnerCannotDeletePost() {
        when(postRepository.findByIdAndUser_IdAndDeletedAtIsNull(10L, 99L))
                .thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> postService.deleteUserPost(10L, 99L));

        verify(postRepository).findByIdAndUser_IdAndDeletedAtIsNull(10L, 99L);
        verifyNoInteractions(userRepository, postImageRepository);
    }
}
