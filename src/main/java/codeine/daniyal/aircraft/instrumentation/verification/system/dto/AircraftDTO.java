package codeine.daniyal.aircraft.instrumentation.verification.system.dto;

import codeine.daniyal.aircraft.instrumentation.verification.system.enums.AircraftStatus;
import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AircraftDTO {
    private Long id;
    private String callsign;
    private String registration;
    private String aircraftType;
    private String manufacturer;
    private String model;
    private Integer maxPassengers;
    private Double maxCargoKg;
    private Double maxFuelKg;
    private Double emptyWeightKg;
    private Double maxTakeoffWeightKg;
    private Double maxLandingWeightKg;
    private Double cruiseSpeedKnots;
    private Double maxSpeedKnots;
    private Integer serviceCeilingFeet;
    private Integer rangeNauticalMiles;
    private Integer engineCount;
    private String engineType;
    private AircraftStatus status;
    private Double currentFuelKg;
    private Double currentLatitude;
    private Double currentLongitude;
    private Integer currentAltitudeFeet;
    private Double currentHeading;
    private Double currentSpeedKnots;
    private LocalDateTime lastMaintenanceDate;
    private LocalDateTime nextMaintenanceDate;
    private Double totalFlightHours;
    private Integer totalCycles;
}

