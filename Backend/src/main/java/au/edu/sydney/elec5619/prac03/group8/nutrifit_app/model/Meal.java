package au.edu.sydney.elec5619.prac03.group8.nutrifit_app.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "meals")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Meal {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "meal_id")
    private Long mealId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
    
    @Column(name = "meal_description")
    private String mealDescription;
    
    @Column(name = "photo_url")
    private String photoUrl;
    
    @Column(name = "total_calories", precision = 8, scale = 2)
    private BigDecimal totalCalories;
    
    @Column(name = "protein_g", precision = 6, scale = 2)
    private BigDecimal proteinG;
    
    @Column(name = "carbs_g", precision = 6, scale = 2)
    private BigDecimal carbsG;
    
    @Column(name = "fat_g", precision = 6, scale = 2)
    private BigDecimal fatG;
    
    @Column(name = "is_ai_generated")
    private Boolean isAiGenerated = false;
    
    @Column(name = "user_edited")
    private Boolean userEdited = false;
    
    @Column(name = "role")
    private String role;
    
    @Column(name = "meal_time")
    private LocalDateTime mealTime;
    
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Relationship
    @OneToMany(mappedBy = "meal", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<MealItem> mealItems;
}

