package au.edu.sydney.elec5619.prac03.group8.nutrifit_app.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "meal_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MealItem {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "item_id")
    private Long itemId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meal_id")
    private Meal meal;
    
    @Column(name = "food_name")
    private String foodName;
    
    @Column(name = "portion_size", precision = 6, scale = 2)
    private BigDecimal portionSize;
    
    @Column(name = "portion_unit")
    private String portionUnit;
    
    @Column(name = "calories", precision = 6, scale = 2)
    private BigDecimal calories;
    
    @Column(name = "protein_g", precision = 6, scale = 2)
    private BigDecimal proteinG;
    
    @Column(name = "carbs_g", precision = 6, scale = 2)
    private BigDecimal carbsG;
    
    @Column(name = "fat_g", precision = 6, scale = 2)
    private BigDecimal fatG;
    
    @Column(name = "confidence_score", precision = 4, scale = 2)
    private BigDecimal confidenceScore;
}

