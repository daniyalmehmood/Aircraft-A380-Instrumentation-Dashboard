package codeine.daniyal.aircraft.instrumentation.verification.system.dto;

import codeine.daniyal.aircraft.instrumentation.verification.system.enums.SwitchState;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SwitchToggleRequest {
    private Long switchId;
    private String switchCode;
    private SwitchState newState;
    private Boolean openGuard;
    private Boolean confirmAction;
}

