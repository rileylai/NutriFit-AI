package au.edu.sydney.elec5619.prac03.group8.nutrifit_app.dto.Response.Dashboard;

import java.time.LocalDateTime;
import java.util.List;

public class AchievementsExportDTO {
    private List<AchievementDTO> completedAchievements;
    private List<AchievementDTO> inProgressAchievements;
    private StreaksDTO currentStreaks;
    private List<MilestoneDTO> milestones;
    private Integer totalAchievements;
    private Integer totalPoints;
    private String currentLevel;
    private String motivationalMessage;

    public AchievementsExportDTO() {}

    public AchievementsExportDTO(List<AchievementDTO> completedAchievements, List<AchievementDTO> inProgressAchievements,
                               StreaksDTO currentStreaks, List<MilestoneDTO> milestones, Integer totalAchievements,
                               Integer totalPoints, String currentLevel, String motivationalMessage) {
        this.completedAchievements = completedAchievements;
        this.inProgressAchievements = inProgressAchievements;
        this.currentStreaks = currentStreaks;
        this.milestones = milestones;
        this.totalAchievements = totalAchievements;
        this.totalPoints = totalPoints;
        this.currentLevel = currentLevel;
        this.motivationalMessage = motivationalMessage;
    }

    // Getters and Setters
    public List<AchievementDTO> getCompletedAchievements() { return completedAchievements; }
    public void setCompletedAchievements(List<AchievementDTO> completedAchievements) { this.completedAchievements = completedAchievements; }

    public List<AchievementDTO> getInProgressAchievements() { return inProgressAchievements; }
    public void setInProgressAchievements(List<AchievementDTO> inProgressAchievements) { this.inProgressAchievements = inProgressAchievements; }

    public StreaksDTO getCurrentStreaks() { return currentStreaks; }
    public void setCurrentStreaks(StreaksDTO currentStreaks) { this.currentStreaks = currentStreaks; }

    public List<MilestoneDTO> getMilestones() { return milestones; }
    public void setMilestones(List<MilestoneDTO> milestones) { this.milestones = milestones; }

    public Integer getTotalAchievements() { return totalAchievements; }
    public void setTotalAchievements(Integer totalAchievements) { this.totalAchievements = totalAchievements; }

    public Integer getTotalPoints() { return totalPoints; }
    public void setTotalPoints(Integer totalPoints) { this.totalPoints = totalPoints; }

    public String getCurrentLevel() { return currentLevel; }
    public void setCurrentLevel(String currentLevel) { this.currentLevel = currentLevel; }

    public String getMotivationalMessage() { return motivationalMessage; }
    public void setMotivationalMessage(String motivationalMessage) { this.motivationalMessage = motivationalMessage; }

    public static class AchievementDTO {
        private String achievementId;
        private String title;
        private String description;
        private String category;
        private Integer points;
        private String status;
        private Double progress;
        private LocalDateTime achievedAt;
        private String icon;

        public AchievementDTO() {}

        public AchievementDTO(String achievementId, String title, String description, String category,
                            Integer points, String status, Double progress, LocalDateTime achievedAt, String icon) {
            this.achievementId = achievementId;
            this.title = title;
            this.description = description;
            this.category = category;
            this.points = points;
            this.status = status;
            this.progress = progress;
            this.achievedAt = achievedAt;
            this.icon = icon;
        }

        // Getters and Setters
        public String getAchievementId() { return achievementId; }
        public void setAchievementId(String achievementId) { this.achievementId = achievementId; }

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }

        public Integer getPoints() { return points; }
        public void setPoints(Integer points) { this.points = points; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public Double getProgress() { return progress; }
        public void setProgress(Double progress) { this.progress = progress; }

        public LocalDateTime getAchievedAt() { return achievedAt; }
        public void setAchievedAt(LocalDateTime achievedAt) { this.achievedAt = achievedAt; }

        public String getIcon() { return icon; }
        public void setIcon(String icon) { this.icon = icon; }
    }

    public static class MilestoneDTO {
        private String milestoneId;
        private String title;
        private String description;
        private String metric;
        private Double targetValue;
        private Double currentValue;
        private Double progress;
        private LocalDateTime achievedAt;
        private String status;

        public MilestoneDTO() {}

        public MilestoneDTO(String milestoneId, String title, String description, String metric,
                          Double targetValue, Double currentValue, Double progress,
                          LocalDateTime achievedAt, String status) {
            this.milestoneId = milestoneId;
            this.title = title;
            this.description = description;
            this.metric = metric;
            this.targetValue = targetValue;
            this.currentValue = currentValue;
            this.progress = progress;
            this.achievedAt = achievedAt;
            this.status = status;
        }

        // Getters and Setters
        public String getMilestoneId() { return milestoneId; }
        public void setMilestoneId(String milestoneId) { this.milestoneId = milestoneId; }

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public String getMetric() { return metric; }
        public void setMetric(String metric) { this.metric = metric; }

        public Double getTargetValue() { return targetValue; }
        public void setTargetValue(Double targetValue) { this.targetValue = targetValue; }

        public Double getCurrentValue() { return currentValue; }
        public void setCurrentValue(Double currentValue) { this.currentValue = currentValue; }

        public Double getProgress() { return progress; }
        public void setProgress(Double progress) { this.progress = progress; }

        public LocalDateTime getAchievedAt() { return achievedAt; }
        public void setAchievedAt(LocalDateTime achievedAt) { this.achievedAt = achievedAt; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }
}