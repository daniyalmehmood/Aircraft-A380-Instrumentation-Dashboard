package codeine.daniyal.aircraft.instrumentation.verification.system.enums;

public enum TurbulenceLevel {
    NONE("No turbulence", 0),
    LIGHT("Light turbulence - slight, erratic changes in altitude/attitude", 1),
    MODERATE("Moderate turbulence - changes in altitude/attitude, aircraft remains in control", 2),
    SEVERE("Severe turbulence - large, abrupt changes, may momentarily lose control", 3),
    EXTREME("Extreme turbulence - violently tossed, practically impossible to control", 4);

    private final String description;
    private final int severity;

    TurbulenceLevel(String description, int severity) {
        this.description = description;
        this.severity = severity;
    }

    public String getDescription() {
        return description;
    }

    public int getSeverity() {
        return severity;
    }
}
