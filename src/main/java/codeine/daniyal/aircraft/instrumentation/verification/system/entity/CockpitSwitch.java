package codeine.daniyal.aircraft.instrumentation.verification.system.entity;

import codeine.daniyal.aircraft.instrumentation.verification.system.enums.PanelLocation;
import codeine.daniyal.aircraft.instrumentation.verification.system.enums.SwitchState;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "cockpit_switches")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CockpitSwitch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "switch_code", nullable = false)
    private String switchCode;

    @Column(name = "switch_name", nullable = false)
    private String switchName;

    @Column(name = "description", length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "panel_location", nullable = false)
    private PanelLocation panelLocation;

    @Enumerated(EnumType.STRING)
    @Column(name = "current_state", nullable = false)
    private SwitchState currentState;

    @Enumerated(EnumType.STRING)
    @Column(name = "default_state")
    private SwitchState defaultState;

    @Column(name = "is_guarded")
    private Boolean isGuarded;

    @Column(name = "guard_open")
    private Boolean guardOpen;

    @Column(name = "is_momentary")
    private Boolean isMomentary;

    @Column(name = "is_illuminated")
    private Boolean isIlluminated;

    @Column(name = "illumination_color")
    private String illuminationColor;

    @Column(name = "is_annunciator")
    private Boolean isAnnunciator;

    @Column(name = "annunciator_status")
    private String annunciatorStatus;

    @Column(name = "position_count")
    private Integer positionCount;

    @Column(name = "current_position")
    private Integer currentPosition;

    @Column(name = "available_states")
    private String availableStates;

    @Column(name = "is_critical")
    private Boolean isCritical;

    @Column(name = "requires_confirmation")
    private Boolean requiresConfirmation;

    @Column(name = "system_affected")
    private String systemAffected;

    @Column(name = "is_operational")
    private Boolean isOperational;

    @Column(name = "failure_mode")
    private String failureMode;

    @Column(name = "last_toggled_at")
    private LocalDateTime lastToggledAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "simulation_session_id")
    private FlightSimulationSession simulationSession;

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
        if (isGuarded == null) {
            isGuarded = false;
        }
        if (guardOpen == null) {
            guardOpen = false;
        }
        if (isMomentary == null) {
            isMomentary = false;
        }
        if (isIlluminated == null) {
            isIlluminated = false;
        }
        if (isAnnunciator == null) {
            isAnnunciator = false;
        }
        if (isCritical == null) {
            isCritical = false;
        }
        if (requiresConfirmation == null) {
            requiresConfirmation = false;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public void toggle() {
        if (currentState == SwitchState.ON) {
            currentState = SwitchState.OFF;
        } else if (currentState == SwitchState.OFF) {
            currentState = SwitchState.ON;
        }
        lastToggledAt = LocalDateTime.now();
    }

    public void setState(SwitchState newState) {
        this.currentState = newState;
        lastToggledAt = LocalDateTime.now();
    }
}

