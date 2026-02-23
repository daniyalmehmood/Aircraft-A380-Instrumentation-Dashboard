package codeine.daniyal.aircraft.instrumentation.verification.system.dto;

import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InstrumentReadingDTO {
    private Long id;
    private Long instrumentId;
    private String instrumentType;
    private Long simulationSessionId;
    private Double readingValue;
    private Double expectedValue;
    private Double deviation;
    private Double deviationPercent;
    private Boolean isWithinTolerance;
    private Boolean isAnomaly;
    private String anomalyDescription;
    private String flightPhase;
    private Integer altitudeFeet;
    private Double airspeedKnots;
    private Double headingDegrees;
    private Double latitude;
    private Double longitude;
    private LocalDateTime timestamp;
}

