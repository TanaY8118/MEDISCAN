package com.mediscan.history.repository;

import com.mediscan.history.document.MedicineHistory;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HistoryRepository extends MongoRepository<MedicineHistory, String> {
    List<MedicineHistory> findByMedicineId(String medicineId);
    List<MedicineHistory> findByPerformedBy(String username);
}
