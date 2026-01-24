package com.mediscan.medicine.repository;

import com.mediscan.medicine.document.Medicine;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MedicineRepository extends MongoRepository<Medicine, String> {
    Optional<Medicine> findByBarcode(String barcode);
    boolean existsByBarcode(String barcode);
}
