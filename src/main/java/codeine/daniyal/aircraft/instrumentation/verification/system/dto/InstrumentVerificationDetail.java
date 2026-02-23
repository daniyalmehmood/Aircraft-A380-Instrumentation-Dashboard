package codeine.daniyal.aircraft.instrumentation.verification.system.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InstrumentVerificationDetail {

    private Long instrumentId;
    private String instrumentType;
    private String instrumentName;
    private Double currentValue;
    private Double expectedValue;
    private Double deviation;
    private Double deviationPercent;
    private String status;
    private Boolean isCritical;
    private String message;
}
