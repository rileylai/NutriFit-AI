package au.edu.sydney.elec5619.prac03.group8.nutrifit_app.dto.Response.Nutrition;

public class DailyIntakeSummaryDTO {
    private Intake currentIntake;
    private Targets dailyTargets;

    public DailyIntakeSummaryDTO() {}

    public DailyIntakeSummaryDTO(Intake currentIntake, Targets dailyTargets) {
        this.currentIntake = currentIntake;
        this.dailyTargets = dailyTargets;
    }

    public Intake getCurrentIntake() { return currentIntake; }
    public void setCurrentIntake(Intake currentIntake) { this.currentIntake = currentIntake; }

    public Targets getDailyTargets() { return dailyTargets; }
    public void setDailyTargets(Targets dailyTargets) { this.dailyTargets = dailyTargets; }

    // Nested simple DTOs to mirror frontend expectations
    public static class Intake {
        private int calories;
        private double protein;
        private double carbs;
        private double fat;
        private double fiber;
        private double sodium;

        public Intake() {}

        public Intake(int calories, double protein, double carbs, double fat, double fiber, double sodium) {
            this.calories = calories;
            this.protein = protein;
            this.carbs = carbs;
            this.fat = fat;
            this.fiber = fiber;
            this.sodium = sodium;
        }

        public int getCalories() { return calories; }
        public void setCalories(int calories) { this.calories = calories; }
        public double getProtein() { return protein; }
        public void setProtein(double protein) { this.protein = protein; }
        public double getCarbs() { return carbs; }
        public void setCarbs(double carbs) { this.carbs = carbs; }
        public double getFat() { return fat; }
        public void setFat(double fat) { this.fat = fat; }
        public double getFiber() { return fiber; }
        public void setFiber(double fiber) { this.fiber = fiber; }
        public double getSodium() { return sodium; }
        public void setSodium(double sodium) { this.sodium = sodium; }
    }

    public static class Targets {
        private int calories;
        private int protein;
        private int carbs;
        private int fat;
        private int fiber;
        private int sodium;

        public Targets() {}

        public Targets(int calories, int protein, int carbs, int fat, int fiber, int sodium) {
            this.calories = calories;
            this.protein = protein;
            this.carbs = carbs;
            this.fat = fat;
            this.fiber = fiber;
            this.sodium = sodium;
        }

        public int getCalories() { return calories; }
        public void setCalories(int calories) { this.calories = calories; }
        public int getProtein() { return protein; }
        public void setProtein(int protein) { this.protein = protein; }
        public int getCarbs() { return carbs; }
        public void setCarbs(int carbs) { this.carbs = carbs; }
        public int getFat() { return fat; }
        public void setFat(int fat) { this.fat = fat; }
        public int getFiber() { return fiber; }
        public void setFiber(int fiber) { this.fiber = fiber; }
        public int getSodium() { return sodium; }
        public void setSodium(int sodium) { this.sodium = sodium; }
    }
}

