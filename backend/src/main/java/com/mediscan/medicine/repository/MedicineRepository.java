package com.mediscan.medicine.repository;

import com.mediscan.medicine.document.Medicine;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MedicineRepository extends MongoRepository<Medicine, String> {

    /**
     * USER-SCOPED: Only medicines created by the given user.
     */
    List<Medicine> findByCreatedBy(String createdBy);
}
