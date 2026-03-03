package com.mediscan.inventory.repository;

import com.mediscan.inventory.entity.Inventory;
import com.mediscan.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {
    Optional<Inventory> findByMedicineIdAndUserId(String medicineId, Long userId);

    Optional<Inventory> findByIdAndUserId(Long id, Long userId);

    List<Inventory> findByUserId(Long userId);

    // Admin/batch use only – do NOT expose via user-facing services
    List<Inventory> findByUser(User user);
}
