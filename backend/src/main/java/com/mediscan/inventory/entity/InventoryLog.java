package com.mediscan.inventory.entity;

import com.mediscan.user.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * SYSTEM INVARIANT: inventory_logs is APPEND-ONLY
 * This entity cannot be updated or deleted after creation
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "inventory_logs")
public class InventoryLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "inventory_id", nullable = false)
    private Long inventoryId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inventory_id", insertable = false, updatable = false)
    private Inventory inventory;

    /**
     * OWNER: Relational link to the owning user.
     * Enforces data ownership at the database and JPA layers.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LogReason reason;

    @Column(name = "change_amount", nullable = false)
    private Integer changeAmount;

    @Column(name = "quantity_before", nullable = false)
    private Integer quantityBefore;

    @Column(name = "quantity_after", nullable = false)
    private Integer quantityAfter;

    private String note;

    @Column(name = "performed_by", nullable = false)
    private String performedBy;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    /**
     * INVARIANT ENFORCEMENT: Block updates
     */
    @PreUpdate
    protected void preventUpdate() {
        throw new UnsupportedOperationException(
                "INVARIANT VIOLATION: inventory_logs is append-only. Updates are forbidden. " +
                        "Log ID: " + this.id);
    }

    /**
     * INVARIANT ENFORCEMENT: Block deletes
     */
    @PreRemove
    protected void preventDelete() {
        throw new UnsupportedOperationException(
                "INVARIANT VIOLATION: inventory_logs is append-only. Deletion is forbidden. " +
                        "Log ID: " + this.id);
    }
}
