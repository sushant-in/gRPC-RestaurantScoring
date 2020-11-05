package scoring.domain;

import scoring.v1.RiskCategory;

public class Violation {
    private String id;
    private String description;
    private RiskCategory risk;

    public Violation() {
    }

    public Violation(String id, String desc, RiskCategory risk) {
        this.id = id;
        this.description = desc;
        this.risk = risk;
    }

    public String getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public RiskCategory getRisk() {
        return risk;
    }
}
