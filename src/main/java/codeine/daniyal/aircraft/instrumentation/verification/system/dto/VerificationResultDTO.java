package codeine.daniyal.aircraft.instrumentation.verification.system.dto;

import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VerificationResultDTO {
    private Long sessionId;
    private String sessionCode;
    private Integer totalInstruments;
    private Integer passedCount;
    private Integer failedCount;
    private Integer warningCount;
    private Double passRate;
    private Boolean overallPass;
    private List<InstrumentVerificationDetail> details;

}


