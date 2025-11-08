package au.edu.sydney.elec5619.prac03.group8.nutrifit_app.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "nutrition_targets")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NutritionTarget {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
    
    // Macronutrients and calories (grams and kcal)
    private Integer dailyCalories;
    private Integer dailyProtein;
    private Integer dailyCarbs;
    private Integer dailyFats;
    private Integer dailyFiber;        // grams
    private Integer dailySodium;       // mg
    private Integer dailySugar;        // grams

    // Vitamins and minerals
    private Integer dailyCholesterol;  // mg
    private Double dailyVitaminC;      // mg
    private Double dailyCalcium;       // mg
    private Double dailyIron;          // mg
    
    // Target context
    @Enumerated(EnumType.STRING)
    private TargetType targetType;  // CUTTING, BULKING, MAINTENANCE
    
    @Column(nullable = false)
    private LocalDate startDate;
    
    private LocalDate endDate;  // null = currently active
    
    @Column(nullable = false, columnDefinition = "boolean default true")
    private Boolean isActive;
    
    private String description;  // "Summer cut phase", "Bulk season"
    
    @CreationTimestamp
    private LocalDateTime createdAt;
}

enum TargetType {
    CUTTING,      // Weight loss
    BULKING,      // Weight/muscle gain  
    MAINTENANCE,  // Maintain current weight
    RECOMP        // Body recomposition
}
