package codeine.daniyal.aircraft.instrumentation.verification.system.dto;

import codeine.daniyal.aircraft.instrumentation.verification.system.enums.InstrumentType;
import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FlightInstrumentDTO {
    private Long id;
    private InstrumentType instrumentType;
    private String instrumentName;
    private Double currentValue;
    private Double expectedValue;
    private Double minValue;
    private Double maxValue;
    private Double tolerancePercent;
    private Double toleranceAbsolute;
    private String unit;
    private Boolean isCritical;
    private Boolean isOperational;
    private String verificationStatus;
    private LocalDateTime lastVerifiedAt;
    private String failureMode;
    private LocalDateTime calibrationDate;
    private LocalDateTime nextCalibrationDate;
    private Boolean withinTolerance;
}

