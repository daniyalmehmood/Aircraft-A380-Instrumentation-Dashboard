package codeine.daniyal.aircraft.instrumentation.verification.system.enums;

public enum AircraftStatus {
    PARKED("Aircraft is parked at gate"),
    BOARDING("Passengers boarding"),
    PUSHBACK("Aircraft pushback in progress"),
    TAXIING("Aircraft taxiing"),
    HOLDING_SHORT("Holding short of runway"),
    TAKING_OFF("Taking off"),
    AIRBORNE("Aircraft is airborne"),
    LANDING("Aircraft landing"),
    MAINTENANCE("Aircraft under maintenance"),
    OUT_OF_SERVICE("Aircraft out of service");

    private final String description;

    AircraftStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}

