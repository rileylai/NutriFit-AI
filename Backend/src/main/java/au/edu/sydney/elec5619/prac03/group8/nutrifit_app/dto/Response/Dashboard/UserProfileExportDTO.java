package au.edu.sydney.elec5619.prac03.group8.nutrifit_app.dto.Response.Dashboard;

import java.time.LocalDateTime;

public class UserProfileExportDTO {
    private Long userId;
    private String username;
    private String email;
    private Double currentWeight;
    private Double currentHeight;
    private Double currentBMI;
    private Double currentBMR;
    private Integer age;
    private String gender;
    private String activityLevel;
    private LocalDateTime profileLastUpdated;

    public UserProfileExportDTO() {}

    public UserProfileExportDTO(Long userId, String username, String email, Double currentWeight,
                               Double currentHeight, Double currentBMI, Double currentBMR,
                               Integer age, String gender, String activityLevel,
                               LocalDateTime profileLastUpdated) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.currentWeight = currentWeight;
        this.currentHeight = currentHeight;
        this.currentBMI = currentBMI;
        this.currentBMR = currentBMR;
        this.age = age;
        this.gender = gender;
        this.activityLevel = activityLevel;
        this.profileLastUpdated = profileLastUpdated;
    }

    // Getters and Setters
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Double getCurrentWeight() { return currentWeight; }
    public void setCurrentWeight(Double currentWeight) { this.currentWeight = currentWeight; }

    public Double getCurrentHeight() { return currentHeight; }
    public void setCurrentHeight(Double currentHeight) { this.currentHeight = currentHeight; }

    public Double getCurrentBMI() { return currentBMI; }
    public void setCurrentBMI(Double currentBMI) { this.currentBMI = currentBMI; }

    public Double getCurrentBMR() { return currentBMR; }
    public void setCurrentBMR(Double currentBMR) { this.currentBMR = currentBMR; }

    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getActivityLevel() { return activityLevel; }
    public void setActivityLevel(String activityLevel) { this.activityLevel = activityLevel; }

    public LocalDateTime getProfileLastUpdated() { return profileLastUpdated; }
    public void setProfileLastUpdated(LocalDateTime profileLastUpdated) { this.profileLastUpdated = profileLastUpdated; }
}