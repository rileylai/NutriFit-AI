package au.edu.sydney.elec5619.prac03.group8.nutrifit_app.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "daily_summaries")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DailySummary {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "summary_id")
    private Long summaryId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
    
    @Column(name = "summary_date")
    private LocalDate summaryDate;
    
    @Column(name = "total_calories_consumed", precision = 8, scale = 2)
    private BigDecimal totalCaloriesConsumed;
    
    @Column(name = "total_calories_burned", precision = 8, scale = 2)
    private BigDecimal totalCaloriesBurned;
    
    @Column(name = "total_protein_g", precision = 8, scale = 2)
    private BigDecimal totalProteinG;
    
    @Column(name = "total_carbs_g", precision = 8, scale = 2)
    private BigDecimal totalCarbsG;
    
    @Column(name = "total_fat_g", precision = 8, scale = 2)
    private BigDecimal totalFatG;
    
    @Column(name = "workout_count")
    private Integer workoutCount;
    
    @Column(name = "net_calories", precision = 8, scale = 2)
    private BigDecimal netCalories;
    
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}