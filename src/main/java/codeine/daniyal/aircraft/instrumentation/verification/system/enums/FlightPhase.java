package codeine.daniyal.aircraft.instrumentation.verification.system.enums;

public enum FlightPhase {
    PREFLIGHT("Pre-flight checks and preparation"),
    ENGINE_START("Engine startup sequence"),
    TAXI("Taxiing to/from runway"),
    TAKEOFF("Takeoff roll and initial climb"),
    CLIMB("Climbing to cruise altitude"),
    CRUISE("Level flight at cruise altitude"),
    DESCENT("Descending from cruise altitude"),
    APPROACH("Approach to landing"),
    LANDING("Landing and rollout"),
    GO_AROUND("Missed approach and go-around"),
    HOLDING("Holding pattern"),
    EMERGENCY("Emergency situation"),
    SHUTDOWN("Engine shutdown and securing aircraft");

    private final String description;

    FlightPhase(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}

