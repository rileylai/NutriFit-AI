package au.edu.sydney.elec5619.prac03.group8.nutrifit_app.repository;

import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.model.UserMetrics;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserMetricsRepository extends JpaRepository<UserMetrics, Long> {

    Optional<UserMetrics> findTopByUserUserIdOrderByRecordAt(Long userUserId);

    Optional<UserMetrics> findTopByUserUserIdOrderByCreatedAtDesc(Long userUserId);
    
    /**
     * Find the second latest metrics entry for a user
     */
    @Query("SELECT um FROM UserMetrics um WHERE um.user.userId = :userId " +
           "ORDER BY um.createdAt DESC LIMIT 1 OFFSET 1")
    Optional<UserMetrics> findSecondLatestByUserId(@Param("userId") Long userId);
    
    /**
     * Alternative method for second latest using native query if JPQL LIMIT/OFFSET doesn't work
     */
    @Query(value = "SELECT * FROM user_metrics WHERE user_id = :userId " +
                   "ORDER BY created_at DESC LIMIT 1 OFFSET 1", 
           nativeQuery = true)
    Optional<UserMetrics> findSecondLatestByUserIdNative(@Param("userId") Long userId);
    
    /**
     * Find all metrics for a user within a date range
     */
    List<UserMetrics> findByUserUserIdAndCreatedAtBetweenOrderByCreatedAtDesc(
        Long userId, LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Find all metrics for a user ordered by creation date
     */
    List<UserMetrics> findByUserUserIdOrderByCreatedAtDesc(Long userId);

    /**
     * Paginated metrics for a user
     */
    Page<UserMetrics> findByUserUserId(Long userId, Pageable pageable);

    /**
     * Paginated metrics within a date range
     */
    Page<UserMetrics> findByUserUserIdAndCreatedAtBetween(Long userId, LocalDateTime startDate,
                                                         LocalDateTime endDate, Pageable pageable);
    
    /**
     * Get the latest N entries for a user
     */
    @Query("SELECT um FROM UserMetrics um WHERE um.user.userId = :userId " +
           "ORDER BY um.createdAt DESC LIMIT :limit")
    List<UserMetrics> findLatestNEntriesByUserId(@Param("userId") Long userId, @Param("limit") int limit);
    
    /**
     * Check if user has any metrics recorded
     */
    boolean existsByUserUserId(Long userId);
    
    /**
     * Count total metrics entries for a user
     */
    long countByUserUserId(Long userId);
}
