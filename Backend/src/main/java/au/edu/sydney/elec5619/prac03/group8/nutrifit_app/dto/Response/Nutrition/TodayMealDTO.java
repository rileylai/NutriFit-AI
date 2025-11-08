package au.edu.sydney.elec5619.prac03.group8.nutrifit_app.dto.Response.Nutrition;

public class TodayMealDTO {
    private Long id;
    private String name;
    private int calories;
    private double protein;
    private double carbs;
    private double fat;
    private String time; // HH:mm
    private String type; // breakfast | lunch | dinner | snack

    public TodayMealDTO() {}

    public TodayMealDTO(Long id, String name, int calories, double protein, double carbs, double fat, String time, String type) {
        this.id = id;
        this.name = name;
        this.calories = calories;
        this.protein = protein;
        this.carbs = carbs;
        this.fat = fat;
        this.time = time;
        this.type = type;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getCalories() { return calories; }
    public void setCalories(int calories) { this.calories = calories; }
    public double getProtein() { return protein; }
    public void setProtein(double protein) { this.protein = protein; }
    public double getCarbs() { return carbs; }
    public void setCarbs(double carbs) { this.carbs = carbs; }
    public double getFat() { return fat; }
    public void setFat(double fat) { this.fat = fat; }
    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
}

