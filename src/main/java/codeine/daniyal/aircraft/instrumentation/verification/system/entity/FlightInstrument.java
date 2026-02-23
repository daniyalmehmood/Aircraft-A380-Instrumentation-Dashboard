package codeine.daniyal.aircraft.instrumentation.verification.system.entity;

import codeine.daniyal.aircraft.instrumentation.verification.system.enums.InstrumentType;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "flight_instruments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FlightInstrument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InstrumentType instrumentType;

    @Column(name = "instrument_name")
    private String instrumentName;

    @Column(name = "current_value")
    private Double currentValue;

    @Column(name = "expected_value")
    private Double expectedValue;

    @Column(name = "min_value")
    private Double minValue;

    @Column(name = "max_value")
    private Double maxValue;

    @Column(name = "tolerance_percent")
    private Double tolerancePercent;

    @Column(name = "tolerance_absolute")
    private Double toleranceAbsolute;

    @Column(name = "unit")
    private String unit;

    @Column(name = "is_critical")
    private Boolean isCritical;

    @Column(name = "is_operational")
    private Boolean isOperational;

    @Column(name = "verification_status")
    private String verificationStatus;

    @Column(name = "last_verified_at")
    private LocalDateTime lastVerifiedAt;

    @Column(name = "failure_mode")
    private String failureMode;

    @Column(name = "calibration_date")
    private LocalDateTime calibrationDate;

    @Column(name = "next_calibration_date")
    private LocalDateTime nextCalibrationDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "simulation_session_id")
    private FlightSimulationSession simulationSession;

    @OneToMany(mappedBy = "instrument", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<InstrumentReading> readings = new ArrayList<>();

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (isOperational == null) {
            isOperational = true;
        }
        if (isCritical == null) {
            isCritical = false;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public boolean isWithinTolerance() {
        if (currentValue == null || expectedValue == null) {
            return false;
        }

        double difference = Math.abs(currentValue - expectedValue);

        if (toleranceAbsolute != null) {
            return difference <= toleranceAbsolute;
        }

        if (tolerancePercent != null && expectedValue != 0) {
            double percentDiff = (difference / Math.abs(expectedValue)) * 100;
            return percentDiff <= tolerancePercent;
        }

        return currentValue.equals(expectedValue);
    }
}

