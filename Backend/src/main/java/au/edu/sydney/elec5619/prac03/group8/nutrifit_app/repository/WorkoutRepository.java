package au.edu.sydney.elec5619.prac03.group8.nutrifit_app.repository;

import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.model.Workout;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WorkoutRepository extends JpaRepository<Workout, Long> {

    List<Workout> findByUserUserId(Long userId, Sort sort);

    List<Workout> findByUserUserIdAndWorkoutDate(Long userId, LocalDate workoutDate, Sort sort);

    List<Workout> findByUserUserIdAndWorkoutDateBetween(Long userId, LocalDate startDate, LocalDate endDate);

    List<Workout> findByUserUserIdAndWorkoutDate(Long userId, LocalDate workoutDate);
}
