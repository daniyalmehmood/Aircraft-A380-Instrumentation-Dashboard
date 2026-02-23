package codeine.daniyal.aircraft.instrumentation.verification.system.dto;

import codeine.daniyal.aircraft.instrumentation.verification.system.enums.FlightPhase;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SimulationSessionDTO {
    private Long id;
    private String sessionCode;
    private String sessionName;
    private String description;

    private Long aircraftId;
    private String aircraftCallsign;
    private String aircraftType;

    private FlightPhase currentFlightPhase;
    private Boolean isActive;
    private Boolean isPaused;

    // Position and State
    private Integer currentAltitudeFeet;
    private Double currentAirspeedKnots;
    private Double currentGroundSpeedKnots;
    private Integer currentVerticalSpeedFpm;
    private Double currentHeadingDegrees;
    private Double currentPitchDegrees;
    private Double currentBankDegrees;
    private Double currentLatitude;
    private Double currentLongitude;
    private Double currentFuelKg;
    private Double fuelBurnRateKgHr;

    // Autopilot
    private Boolean autopilotEngaged;
    private Boolean autothrottleEngaged;
    private Integer selectedAltitudeFeet;
    private Double selectedHeadingDegrees;
    private Double selectedSpeedKnots;
    private Integer selectedVerticalSpeedFpm;
    private Double selectedMach;

    // Flight Plan
    private String departureAirport;
    private String arrivalAirport;
    private String alternateAirport;
    private String flightPlanRoute;
    private Integer estimatedFlightTimeMinutes;
    private Integer actualFlightTimeMinutes;

    // Engine Data
    private Double engine1N1;
    private Double engine2N1;
    private Double engine3N1;
    private Double engine4N1;
    private Double engine1Egt;
    private Double engine2Egt;
    private Double engine3Egt;
    private Double engine4Egt;

    // Control Surfaces
    private Integer flapPosition;
    private Integer slatPosition;
    private Boolean gearDown;
    private Integer speedbrakePosition;
    private Boolean parkingBrakeSet;

    // Timestamps
    private LocalDateTime startedAt;
    private LocalDateTime endedAt;
    private LocalDateTime createdAt;

    // Related data
    private WeatherConditionDTO weatherCondition;
    private List<CockpitSwitchDTO> cockpitSwitches;
    private List<FlightInstrumentDTO> instruments;
}

