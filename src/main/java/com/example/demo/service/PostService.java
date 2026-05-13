package com.example.demo.service;

import com.example.demo.model.Comment;
import com.example.demo.model.Post;
import com.example.demo.model.User;
import com.example.demo.repository.CommentRepository;
import com.example.demo.repository.PostRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Service
public class PostService {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private com.example.demo.repository.FollowRepository followRepository;

    public Post createPost(Post post) {
        return postRepository.save(post);
    }

    public List<Post> getAllPosts() {
        return postRepository.findAllByOrderByCreatedAtDesc();
    }

    public Optional<Post> getPostById(Long id) {
        return postRepository.findById(id);
    }

    public List<Post> getPostsByUser(User user) {
        return postRepository.findByUserOrderByCreatedAtDesc(user);
    }

    public List<Post> searchPosts(String keyword) {
        return postRepository.searchByContent(keyword);
    }

    public void deletePost(Long id) {
        postRepository.deleteById(id);
    }

    @Transactional
    public Post likePost(Long postId, User currentUser) {
        Post post = postRepository.findById(postId)
            .orElseThrow(() -> new RuntimeException("Post non trouvé"));

        User user = userRepository.findById(currentUser.getId())
            .orElseThrow(() -> new RuntimeException("User non trouvé"));

        boolean alreadyLiked = user.getLikedPosts().stream()
            .anyMatch(p -> p.getId().equals(postId));

        if (alreadyLiked) {
            user.getLikedPosts().removeIf(p -> p.getId().equals(postId));
        } else {
            user.getLikedPosts().add(post);
        }

        userRepository.save(user);

        return postRepository.findById(postId)
            .orElseThrow(() -> new RuntimeException("Post non trouvé"));
    }

    public Comment addComment(Long postId, User user, String text) {
        Post post = postRepository.findById(postId)
            .orElseThrow(() -> new RuntimeException("Post non trouvé"));
        Comment comment = new Comment();
        comment.setText(text);
        comment.setUser(user);
        comment.setPost(post);
        return commentRepository.save(comment);
    }

    public List<Comment> getCommentsByPost(Long postId) {
        Post post = postRepository.findById(postId)
            .orElseThrow(() -> new RuntimeException("Post non trouvé"));
        return commentRepository.findByPostOrderByCreatedAtDesc(post);
    }

    public void deleteComment(Long commentId) {
        commentRepository.deleteById(commentId);
    }

    public List<Post> getForYouPosts(User currentUser) {
        try {
            List<com.example.demo.model.Follow> follows =
                followRepository.findByFollower(currentUser);

            List<Long> followedIds = follows.stream()
                .map(f -> f.getFollowing().getId())
                .toList();

            List<Post> allPosts = postRepository.findAllByOrderByCreatedAtDesc();
            List<Post> result = new java.util.ArrayList<>();

            // 1. ✅ Posts des gens suivis en premier
            for (Post p : allPosts) {
                if (followedIds.contains(p.getUser().getId())) {
                    result.add(p);
                }
            }

            // 2. ✅ Top 5 posts les plus likés (pas les siens, pas déjà dans result)
            allPosts.stream()
                .filter(p -> !p.getUser().getId().equals(currentUser.getId()))
                .filter(p -> result.stream().noneMatch(r -> r.getId().equals(p.getId())))
                .sorted((a, b) -> b.getLikedBy().size() - a.getLikedBy().size())
                .limit(5)
                .forEach(result::add);

            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return new java.util.ArrayList<>();
        }
    }
}