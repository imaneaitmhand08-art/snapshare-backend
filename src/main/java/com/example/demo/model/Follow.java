package com.example.demo.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "follows")
@Data
public class Follow {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "follower_id")
    private User follower; // L'utilisateur qui s'abonne

    @ManyToOne
    @JoinColumn(name = "following_id")
    private User following; // L'utilisateur qui est suivi
}