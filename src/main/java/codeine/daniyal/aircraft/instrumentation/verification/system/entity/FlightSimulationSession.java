package codeine.daniyal.aircraft.instrumentation.verification.system.entity;

import codeine.daniyal.aircraft.instrumentation.verification.system.enums.FlightPhase;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "flight_simulation_sessions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FlightSimulationSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "session_code", nullable = false, unique = true)
    private String sessionCode;

    @Column(name = "session_name")
    private String sessionName;

    @Column(name = "description", length = 1000)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "aircraft_id")
    private Aircraft aircraft;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "weather_condition_id")
    private WeatherCondition weatherCondition;

    @Enumerated(EnumType.STRING)
    @Column(name = "current_flight_phase")
    private FlightPhase currentFlightPhase;

    @Column(name = "is_active")
    private Boolean isActive;

    @Column(name = "is_paused")
    private Boolean isPaused;

    // Current Position and State
    @Column(name = "current_altitude_feet")
    private Integer currentAltitudeFeet;

    @Column(name = "current_airspeed_knots")
    private Double currentAirspeedKnots;

    @Column(name = "current_ground_speed_knots")
    private Double currentGroundSpeedKnots;

    @Column(name = "current_vertical_speed_fpm")
    private Integer currentVerticalSpeedFpm;

    @Column(name = "current_heading_degrees")
    private Double currentHeadingDegrees;

    @Column(name = "current_pitch_degrees")
    private Double currentPitchDegrees;

    @Column(name = "current_bank_degrees")
    private Double currentBankDegrees;

    @Column(name = "current_latitude")
    private Double currentLatitude;

    @Column(name = "current_longitude")
    private Double currentLongitude;

    @Column(name = "current_fuel_kg")
    private Double currentFuelKg;

    @Column(name = "fuel_burn_rate_kg_hr")
    private Double fuelBurnRateKgHr;

    // Autopilot Settings
    @Column(name = "autopilot_engaged")
    private Boolean autopilotEngaged;

    @Column(name = "autothrottle_engaged")
    private Boolean autothrottleEngaged;

    @Column(name = "selected_altitude_feet")
    private Integer selectedAltitudeFeet;

    @Column(name = "selected_heading_degrees")
    private Double selectedHeadingDegrees;

    @Column(name = "selected_speed_knots")
    private Double selectedSpeedKnots;

    @Column(name = "selected_vertical_speed_fpm")
    private Integer selectedVerticalSpeedFpm;

    @Column(name = "selected_mach")
    private Double selectedMach;

    // Flight Plan Information
    @Column(name = "departure_airport")
    private String departureAirport;

    @Column(name = "arrival_airport")
    private String arrivalAirport;

    @Column(name = "alternate_airport")
    private String alternateAirport;

    @Column(name = "flight_plan_route", length = 2000)
    private String flightPlanRoute;

    @Column(name = "estimated_flight_time_minutes")
    private Integer estimatedFlightTimeMinutes;

    @Column(name = "actual_flight_time_minutes")
    private Integer actualFlightTimeMinutes;

    // Engine Data
    @Column(name = "engine1_n1")
    private Double engine1N1;

    @Column(name = "engine2_n1")
    private Double engine2N1;

    @Column(name = "engine3_n1")
    private Double engine3N1;

    @Column(name = "engine4_n1")
    private Double engine4N1;

    @Column(name = "engine1_egt")
    private Double engine1Egt;

    @Column(name = "engine2_egt")
    private Double engine2Egt;

    @Column(name = "engine3_egt")
    private Double engine3Egt;

    @Column(name = "engine4_egt")
    private Double engine4Egt;

    // Control Surfaces
    @Column(name = "flap_position")
    private Integer flapPosition;

    @Column(name = "slat_position")
    private Integer slatPosition;

    @Column(name = "gear_down")
    private Boolean gearDown;

    @Column(name = "speedbrake_position")
    private Integer speedbrakePosition;

    @Column(name = "parking_brake_set")
    private Boolean parkingBrakeSet;

    // Relationships
    @OneToMany(mappedBy = "simulationSession", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<FlightInstrument> instruments = new ArrayList<>();

    @OneToMany(mappedBy = "simulationSession", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<CockpitSwitch> cockpitSwitches = new ArrayList<>();

    @OneToMany(mappedBy = "simulationSession", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<InstrumentReading> readings = new ArrayList<>();

    // Timestamps
    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "ended_at")
    private LocalDateTime endedAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (isActive == null) {
            isActive = false;
        }
        if (isPaused == null) {
            isPaused = false;
        }
        if (currentFlightPhase == null) {
            currentFlightPhase = FlightPhase.PREFLIGHT;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public void start() {
        this.isActive = true;
        this.isPaused = false;
        this.startedAt = LocalDateTime.now();
    }

    public void pause() {
        this.isPaused = true;
    }

    public void resume() {
        this.isPaused = false;
    }

    public void stop() {
        this.isActive = false;
        this.isPaused = false;
        this.endedAt = LocalDateTime.now();
    }
}

