package com.OMT.Backend.repository;

import com.OMT.Backend.model.Part;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PartRepository extends JpaRepository<Part, Long> {
    List<Part> findByVehicleIdOrderByIdAsc(Long vehicleId);
    long countByVehicleId(Long vehicleId);
    long countByStatus(String status);
    long countByCanBeExtracted(Boolean canBeExtracted);
}
