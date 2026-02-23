package codeine.daniyal.aircraft.instrumentation.verification.system.enums;

public enum WeatherSeverity {
    CLEAR("Clear conditions - VFR"),
    MARGINAL("Marginal VFR conditions"),
    IFR("Instrument Flight Rules conditions"),
    LOW_IFR("Low IFR conditions"),
    HAZARDOUS("Hazardous weather conditions"),
    SEVERE("Severe weather - Flight not recommended");

    private final String description;

    WeatherSeverity(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}

