package au.edu.sydney.elec5619.prac03.group8.nutrifit_app.repository;

import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.model.AIInsight;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AIInsightRepository extends JpaRepository<AIInsight, Long> {
    
    // Find latest active insights for a user
    @Query("SELECT ai FROM AIInsight ai WHERE ai.user.userId = :userId AND ai.isActive = true " +
           "ORDER BY ai.createdAt DESC")
    List<AIInsight> findLatestActiveInsights(@Param("userId") Long userId);
    
    // Find most recent insight for user
    Optional<AIInsight> findTopByUserUserIdAndIsActiveTrueOrderByCreatedAtDesc(Long userId);
    
    // Find insights by suggestion format (nutrition, exercise, general)
    @Query("SELECT ai FROM AIInsight ai WHERE ai.user.userId = :userId AND ai.suggestionFormat = :format " +
           "AND ai.isActive = true ORDER BY ai.createdAt DESC")
    List<AIInsight> findByUserIdAndSuggestionFormat(@Param("userId") Long userId, 
                                                    @Param("format") String format);
    
    // Find expired insights
    @Query("SELECT ai FROM AIInsight ai WHERE ai.expiresAt < :now AND ai.isActive = true")
    List<AIInsight> findExpiredInsights(@Param("now") LocalDateTime now);
    
    // Count active insights for user
    long countByUserUserIdAndIsActiveTrue(Long userId);
    
    // Find recent insights (within last N hours)
    @Query("SELECT ai FROM AIInsight ai WHERE ai.user.userId = :userId AND ai.createdAt >= :since " +
           "AND ai.isActive = true ORDER BY ai.createdAt DESC")
    List<AIInsight> findRecentInsights(@Param("userId") Long userId, @Param("since") LocalDateTime since);
    
    // Find insights for specific time range
    @Query("SELECT ai FROM AIInsight ai WHERE ai.user.userId = :userId " +
           "AND ai.createdAt BETWEEN :startDate AND :endDate ORDER BY ai.createdAt DESC")
    List<AIInsight> findInsightsByDateRange(@Param("userId") Long userId, 
                                           @Param("startDate") LocalDateTime startDate,
                                           @Param("endDate") LocalDateTime endDate);
}
