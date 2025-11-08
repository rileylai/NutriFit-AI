package au.edu.sydney.elec5619.prac03.group8.nutrifit_app.config;
import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.model.User;
import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TestUserLoader {

    @Bean
    CommandLineRunner initTestUser(UserRepository userRepository) {
        return args -> {
            if (!userRepository.existsByEmail("test@example.com")) {
                User user = new User();
                user.setEmail("test@example.com");
                user.setUserName("TestUser");
                user.setPasswordHash("hashed_password"); // 可以用明碼測試
                user.setEmailVerified(true);

                userRepository.save(user);
                System.out.println("✅ Test user created: test@example.com");
            }
        };
    }
}
