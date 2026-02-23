package codeine.daniyal.aircraft.instrumentation.verification.system.enums;

public enum SwitchState {
    OFF("Switch is in OFF position"),
    ON("Switch is in ON position"),
    AUTO("Switch is in AUTO mode"),
    MANUAL("Switch is in MANUAL mode"),
    ARMED("Switch is ARMED"),
    DISARMED("Switch is DISARMED"),
    OPEN("Switch/Valve is OPEN"),
    CLOSED("Switch/Valve is CLOSED"),
    LOW("Switch is in LOW setting"),
    HIGH("Switch is in HIGH setting"),
    NORMAL("Switch is in NORMAL position"),
    ALTERNATE("Switch is in ALTERNATE position"),
    TEST("Switch is in TEST position"),
    FAULT("Switch indicates FAULT condition");

    private final String description;

    SwitchState(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}

