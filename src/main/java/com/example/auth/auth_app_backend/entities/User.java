package com.example.auth.auth_app_backend.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "user_id")
    private UUID id;
    @Column(name = "user_email",unique = true,length = 255)
    private String email;
    @Column(name = "user_name")
    private String name;
    private String password;
    @Column(name = "user_img")
    private String image;
    private boolean enable;
    private Instant createdAt = Instant.now();
    private Instant updatedAt = Instant.now();

    private Provider provider = Provider.LOCAL;
    private Set<Role> roles = new HashSet<>();
}
