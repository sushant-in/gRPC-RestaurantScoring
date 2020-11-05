package scoring.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import scoring.v1.InspectionType;

import java.time.Instant;
import java.util.List;

@Document
public class Inspection {
    @Id
    private String id;
    @DBRef
    private Restaurant restaurant;
    private InspectionType type;
    private int score;
    private List<Violation> violations;
    private Instant date;

    public Inspection() {
    }

    public Inspection(String id, Restaurant restaurant, InspectionType type, int score, List<Violation> violations, Instant date) {
        this.id = id;
        this.restaurant = restaurant;
        this.type = type;
        this.score = score;
        this.violations = violations;
        this.date = date;
    }

    public String getId() {
        return id;
    }

    public Restaurant getRestaurant() {
        return restaurant;
    }

    public InspectionType getType() {
        return type;
    }

    public int getScore() {
        return score;
    }

    public List<Violation> getViolations() {
        return violations;
    }

    public Instant getDate() {
        return date;
    }
}
