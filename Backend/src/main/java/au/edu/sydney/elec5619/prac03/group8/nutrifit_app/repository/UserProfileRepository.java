package au.edu.sydney.elec5619.prac03.group8.nutrifit_app.repository;

import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.model.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {
    Optional<UserProfile> findByUserUserId(Long userId);
    boolean existsByUserUserId(Long userId);
}

