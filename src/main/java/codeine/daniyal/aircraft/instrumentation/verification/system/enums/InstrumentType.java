package codeine.daniyal.aircraft.instrumentation.verification.system.enums;

public enum InstrumentType {
    // Primary Flight Display (PFD) Instruments
    AIRSPEED_INDICATOR("Airspeed Indicator", "knots"),
    ALTIMETER("Altimeter", "feet"),
    VERTICAL_SPEED_INDICATOR("Vertical Speed Indicator", "ft/min"),
    ATTITUDE_INDICATOR("Attitude Indicator", "degrees"),
    HEADING_INDICATOR("Heading Indicator", "degrees"),
    TURN_COORDINATOR("Turn Coordinator", "degrees/second"),

    // Navigation Display (ND) Instruments
    HSI("Horizontal Situation Indicator", "degrees"),
    DME("Distance Measuring Equipment", "nautical miles"),
    VOR("VHF Omnidirectional Range", "degrees"),
    ADF("Automatic Direction Finder", "degrees"),
    GPS("Global Positioning System", "coordinates"),

    // Engine Instruments
    N1_INDICATOR("N1 Engine Speed", "percent"),
    N2_INDICATOR("N2 Engine Speed", "percent"),
    EGT("Exhaust Gas Temperature", "celsius"),
    FUEL_FLOW("Fuel Flow", "kg/hr"),
    OIL_PRESSURE("Oil Pressure", "psi"),
    OIL_TEMPERATURE("Oil Temperature", "celsius"),
    EPR("Engine Pressure Ratio", "ratio"),

    // System Instruments
    FUEL_QUANTITY("Fuel Quantity", "kg"),
    HYDRAULIC_PRESSURE("Hydraulic Pressure", "psi"),
    ELECTRICAL_VOLTAGE("Electrical Voltage", "volts"),
    ELECTRICAL_AMPERAGE("Electrical Amperage", "amps"),
    CABIN_ALTITUDE("Cabin Altitude", "feet"),
    CABIN_PRESSURE_DIFF("Cabin Pressure Differential", "psi"),

    // Environmental Instruments
    OUTSIDE_AIR_TEMP("Outside Air Temperature", "celsius"),
    TOTAL_AIR_TEMP("Total Air Temperature", "celsius"),
    STATIC_AIR_TEMP("Static Air Temperature", "celsius"),

    // Radio Altimeter
    RADIO_ALTIMETER("Radio Altimeter", "feet"),

    // Mach Indicator
    MACH_INDICATOR("Mach Number", "mach"),

    // Ground Speed
    GROUND_SPEED("Ground Speed", "knots"),

    // True Airspeed
    TRUE_AIRSPEED("True Airspeed", "knots"),

    // Angle of Attack
    AOA_INDICATOR("Angle of Attack", "degrees"),

    // Flap Position
    FLAP_POSITION("Flap Position", "degrees"),

    // Gear Position
    GEAR_POSITION("Landing Gear Position", "status");

    private final String displayName;
    private final String unit;

    InstrumentType(String displayName, String unit) {
        this.displayName = displayName;
        this.unit = unit;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getUnit() {
        return unit;
    }
}

