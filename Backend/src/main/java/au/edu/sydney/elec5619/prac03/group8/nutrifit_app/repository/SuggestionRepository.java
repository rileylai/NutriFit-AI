package au.edu.sydney.elec5619.prac03.group8.nutrifit_app.repository;

import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.model.Suggestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SuggestionRepository extends JpaRepository<Suggestion, Long> {
    Optional<Suggestion> findTopByUserUserIdAndIsActiveTrueOrderByCreatedAtDesc(Long userId);
}
