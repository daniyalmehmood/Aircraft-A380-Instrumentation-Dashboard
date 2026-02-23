package codeine.daniyal.aircraft.instrumentation.verification.system.entity;

import codeine.daniyal.aircraft.instrumentation.verification.system.enums.AircraftStatus;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "aircraft")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Aircraft {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String callsign;

    @Column(nullable = false)
    private String registration;

    @Column(nullable = false)
    private String aircraftType;

    @Column(nullable = false)
    private String manufacturer;

    @Column(nullable = false)
    private String model;

    @Column(name = "max_passengers")
    private Integer maxPassengers;

    @Column(name = "max_cargo_kg")
    private Double maxCargoKg;

    @Column(name = "max_fuel_kg")
    private Double maxFuelKg;

    @Column(name = "empty_weight_kg")
    private Double emptyWeightKg;

    @Column(name = "max_takeoff_weight_kg")
    private Double maxTakeoffWeightKg;

    @Column(name = "max_landing_weight_kg")
    private Double maxLandingWeightKg;

    @Column(name = "cruise_speed_knots")
    private Double cruiseSpeedKnots;

    @Column(name = "max_speed_knots")
    private Double maxSpeedKnots;

    @Column(name = "service_ceiling_feet")
    private Integer serviceCeilingFeet;

    @Column(name = "range_nm")
    private Integer rangeNauticalMiles;

    @Column(name = "engine_count")
    private Integer engineCount;

    @Column(name = "engine_type")
    private String engineType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AircraftStatus status;

    @Column(name = "current_fuel_kg")
    private Double currentFuelKg;

    @Column(name = "current_latitude")
    private Double currentLatitude;

    @Column(name = "current_longitude")
    private Double currentLongitude;

    @Column(name = "current_altitude_feet")
    private Integer currentAltitudeFeet;

    @Column(name = "current_heading")
    private Double currentHeading;

    @Column(name = "current_speed_knots")
    private Double currentSpeedKnots;

    @Column(name = "last_maintenance_date")
    private LocalDateTime lastMaintenanceDate;

    @Column(name = "next_maintenance_date")
    private LocalDateTime nextMaintenanceDate;

    @Column(name = "total_flight_hours")
    private Double totalFlightHours;

    @Column(name = "total_cycles")
    private Integer totalCycles;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
