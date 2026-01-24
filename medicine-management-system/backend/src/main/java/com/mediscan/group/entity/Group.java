package com.mediscan.group.entity;

import com.mediscan.user.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.annotation.CreatedBy;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user_groups") // 'groups' is a reserved keyword in MySQL
public class Group {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;
    
    // Simplistic Owner link
    // More complex RBAC would require a separate ManyToMany Members table with Roles.
    // For this prototype, let's keep it simple: Owner is creating user.
    // We can add a simple members mapping later or now.
    
    @ManyToOne
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @CreationTimestamp
    private LocalDateTime createdAt;
}
