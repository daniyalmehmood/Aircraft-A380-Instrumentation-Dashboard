package codeine.daniyal.aircraft.instrumentation.verification.system.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "instrument_readings")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InstrumentReading {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "instrument_id", nullable = false)
    private FlightInstrument instrument;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "simulation_session_id")
    private FlightSimulationSession simulationSession;

    @Column(name = "reading_value", nullable = false)
    private Double readingValue;

    @Column(name = "expected_value")
    private Double expectedValue;

    @Column(name = "deviation")
    private Double deviation;

    @Column(name = "deviation_percent")
    private Double deviationPercent;

    @Column(name = "is_within_tolerance")
    private Boolean isWithinTolerance;

    @Column(name = "is_anomaly")
    private Boolean isAnomaly;

    @Column(name = "anomaly_description")
    private String anomalyDescription;

    @Column(name = "flight_phase")
    private String flightPhase;

    @Column(name = "altitude_feet")
    private Integer altitudeFeet;

    @Column(name = "airspeed_knots")
    private Double airspeedKnots;

    @Column(name = "heading_degrees")
    private Double headingDegrees;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }
        calculateDeviation();
    }

    private void calculateDeviation() {
        if (readingValue != null && expectedValue != null) {
            deviation = readingValue - expectedValue;
            if (expectedValue != 0) {
                deviationPercent = (deviation / Math.abs(expectedValue)) * 100;
            }
        }
    }
}

