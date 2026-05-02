package com.example.demo.controller;

import com.example.demo.model.Post;
import com.example.demo.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/posts")
@CrossOrigin("*")
public class PostController {
    @Autowired
    private PostService postService;

    @PostMapping("/create")
    public Post savePost(@RequestBody Post post) {
        return postService.createPost(post);
    }

    @GetMapping("/all")
    public List<Post> getAll() {
        return postService.getAllPosts();
    }
}