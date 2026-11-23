package com.OMT.Backend.repository;

import com.OMT.Backend.model.RoutingDecision;
import com.OMT.Backend.model.Vehicle;
import com.OMT.Backend.model.VehicleStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, Long> {

    Optional<Vehicle> findByVin(String vin);

    boolean existsByVin(String vin);

    Optional<Vehicle> findByAssetCode(String assetCode);

    boolean existsByAssetCode(String assetCode);

    @Query("SELECT v FROM Vehicle v JOIN FETCH v.yard y WHERE " +
           "(:yardId IS NULL OR y.id = :yardId) AND " +
           "(:routingDecision IS NULL OR v.routingDecision = :routingDecision) AND " +
           "(:status IS NULL OR v.status = :status) AND " +
           "(:search IS NULL OR LOWER(v.vin) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(v.assetCode) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(v.make) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(v.model) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "ORDER BY v.id DESC")
    List<Vehicle> searchVehicles(@Param("search") String search,
                                 @Param("yardId") Long yardId,
                                 @Param("routingDecision") RoutingDecision routingDecision,
                                 @Param("status") VehicleStatus status);

    long countByYardId(Long yardId);

    // Count vehicles in yard that are active (i.e. status != COMPLETED, or any vehicles)
    // Requirement says: "A yard cannot be deactivated while active vehicle records are assigned to it."
    @Query("SELECT COUNT(v) FROM Vehicle v WHERE v.yard.id = :yardId AND v.status <> com.OMT.Backend.model.VehicleStatus.COMPLETED")
    long countActiveVehiclesByYardId(@Param("yardId") Long yardId);

    long countByStatus(VehicleStatus status);

    @Query("SELECT MAX(v.id) FROM Vehicle v")
    Long findMaxId();
}

