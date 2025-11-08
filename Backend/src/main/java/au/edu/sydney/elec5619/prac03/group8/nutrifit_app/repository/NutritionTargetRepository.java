package au.edu.sydney.elec5619.prac03.group8.nutrifit_app.repository;

import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.model.NutritionTarget;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.hibernate.tool.schema.TargetType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface NutritionTargetRepository extends JpaRepository<NutritionTarget, Long> {
    
    // Get current active target
    @Query("SELECT nt FROM NutritionTarget nt WHERE nt.user.userId = :userId " +
           "AND nt.isActive = true AND (nt.endDate IS NULL OR nt.endDate > CURRENT_DATE)")
    Optional<NutritionTarget> findCurrentTargetByUserId(@Param("userId") Long userId);
    
    // Get all target for history
    @Query("SELECT nt FROM NutritionTarget nt WHERE nt.user.userId = :userId ORDER BY nt.createdAt DESC")
    List<NutritionTarget> findByUserIdOrderByCreatedAtDesc(Long userId);
    
    // Get target for specific date
    @Query("SELECT nt FROM NutritionTarget nt WHERE nt.user.userId = :userId " +
           "AND nt.startDate <= :date AND (nt.endDate IS NULL OR nt.endDate >= :date)")
    Optional<NutritionTarget> findTargetByUserIdAndDate(@Param("userId") Long userId, 
                                                          @Param("date") LocalDate date);
    
    // Get target by type
    @Query("SELECT nt FROM NutritionTarget nt WHERE nt.user.userId = :userId AND nt.targetType = :targetType")
    List<NutritionTarget> findByUserIdAndTargetType(Long userId, TargetType targetType);
}
