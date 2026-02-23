package codeine.daniyal.aircraft.instrumentation.verification.system.dto;

import codeine.daniyal.aircraft.instrumentation.verification.system.enums.PanelLocation;
import codeine.daniyal.aircraft.instrumentation.verification.system.enums.SwitchState;
import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CockpitSwitchDTO {
    private Long id;
    private String switchCode;
    private String switchName;
    private String description;
    private PanelLocation panelLocation;
    private SwitchState currentState;
    private SwitchState defaultState;
    private Boolean isGuarded;
    private Boolean guardOpen;
    private Boolean isMomentary;
    private Boolean isIlluminated;
    private String illuminationColor;
    private Boolean isAnnunciator;
    private String annunciatorStatus;
    private Integer positionCount;
    private Integer currentPosition;
    private String availableStates;
    private Boolean isCritical;
    private Boolean requiresConfirmation;
    private String systemAffected;
    private Boolean isOperational;
    private String failureMode;
    private LocalDateTime lastToggledAt;
}

