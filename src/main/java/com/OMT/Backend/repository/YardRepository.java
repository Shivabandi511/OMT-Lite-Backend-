package com.OMT.Backend.repository;

import com.OMT.Backend.model.Yard;
import com.OMT.Backend.model.YardStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface YardRepository extends JpaRepository<Yard, Long> {

    Optional<Yard> findByYardCode(String yardCode);

    boolean existsByYardCode(String yardCode);

    @Query("SELECT y FROM Yard y WHERE " +
           "(:status IS NULL OR y.status = :status) AND " +
           "(:search IS NULL OR LOWER(y.name) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(y.yardCode) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(y.city) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "ORDER BY y.id DESC")
    List<Yard> searchYards(@Param("search") String search, @Param("status") YardStatus status);

    List<Yard> findByStatus(YardStatus status);

    long countByStatus(YardStatus status);

    @Query("SELECT MAX(y.id) FROM Yard y")
    Long findMaxId();
}

