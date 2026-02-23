package codeine.daniyal.aircraft.instrumentation.verification.system.enums;

public enum PanelLocation {
    // Overhead Panel Sections
    OVERHEAD_APU("Overhead Panel - APU Section"),
    OVERHEAD_ELECTRICAL("Overhead Panel - Electrical Section"),
    OVERHEAD_HYDRAULIC("Overhead Panel - Hydraulic Section"),
    OVERHEAD_FUEL("Overhead Panel - Fuel Section"),
    OVERHEAD_AIR_CONDITIONING("Overhead Panel - Air Conditioning Section"),
    OVERHEAD_PRESSURIZATION("Overhead Panel - Pressurization Section"),
    OVERHEAD_ANTI_ICE("Overhead Panel - Anti-Ice Section"),
    OVERHEAD_LIGHTING("Overhead Panel - Lighting Section"),
    OVERHEAD_SIGNS("Overhead Panel - Signs Section"),
    OVERHEAD_OXYGEN("Overhead Panel - Oxygen Section"),
    OVERHEAD_CALLS("Overhead Panel - Calls Section"),
    OVERHEAD_WIPER("Overhead Panel - Wiper Section"),
    OVERHEAD_FIRE("Overhead Panel - Fire Section"),
    OVERHEAD_CARGO("Overhead Panel - Cargo Section"),
    OVERHEAD_VENTILATION("Overhead Panel - Ventilation Section"),
    OVERHEAD_ENGINE("Overhead Panel - Engine Section"),
    OVERHEAD_MAINTENANCE("Overhead Panel - Maintenance Section"),

    // Main Instrument Panel
    MAIN_PFD("Main Panel - Primary Flight Display"),
    MAIN_ND("Main Panel - Navigation Display"),
    MAIN_ECAM_UPPER("Main Panel - Upper ECAM"),
    MAIN_ECAM_LOWER("Main Panel - Lower ECAM"),
    MAIN_STANDBY("Main Panel - Standby Instruments"),
    MAIN_CLOCK("Main Panel - Clock"),

    // Glareshield
    GLARESHIELD_FCU("Glareshield - Flight Control Unit"),
    GLARESHIELD_EFIS("Glareshield - EFIS Control Panel"),
    GLARESHIELD_AUTOBRAKE("Glareshield - Autobrake Panel"),
    GLARESHIELD_WARNING("Glareshield - Warning Panel"),

    // Center Pedestal
    PEDESTAL_THROTTLE("Pedestal - Throttle Quadrant"),
    PEDESTAL_FLAPS("Pedestal - Flap Lever"),
    PEDESTAL_SPEEDBRAKE("Pedestal - Speedbrake Lever"),
    PEDESTAL_GEAR("Pedestal - Landing Gear Lever"),
    PEDESTAL_PARKING_BRAKE("Pedestal - Parking Brake"),
    PEDESTAL_TRIM("Pedestal - Trim Controls"),
    PEDESTAL_RMP("Pedestal - Radio Management Panel"),
    PEDESTAL_TCAS("Pedestal - TCAS Panel"),
    PEDESTAL_TRANSPONDER("Pedestal - Transponder Panel"),
    PEDESTAL_WEATHER_RADAR("Pedestal - Weather Radar Panel"),
    PEDESTAL_MCDU("Pedestal - MCDU"),
    PEDESTAL_SWITCHING("Pedestal - Switching Panel"),
    PEDESTAL_ENGINE_MASTER("Pedestal - Engine Master Switches"),
    PEDESTAL_ENGINE_MODE("Pedestal - Engine Mode Selector"),

    // Side Panels
    SIDE_STICK("Side Panel - Side Stick"),
    SIDE_TILLER("Side Panel - Nose Wheel Tiller"),

    // Rudder Pedals
    RUDDER_PEDALS("Floor - Rudder Pedals");

    private final String description;

    PanelLocation(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}

