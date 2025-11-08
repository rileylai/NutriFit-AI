package au.edu.sydney.elec5619.prac03.group8.nutrifit_app.repository;

import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.model.Meal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
@Repository
public interface MealRepository extends JpaRepository<Meal, Long> {

    List<Meal> findByUserUserIdAndMealTimeBetween(Long userId, LocalDateTime startTime, LocalDateTime endTime);

    List<Meal> findByUserUserIdAndMealTimeBetweenOrderByMealTimeDesc(Long userId, LocalDateTime startTime, LocalDateTime endTime);

    List<Meal> findByUserUserId(Long userId);
}
