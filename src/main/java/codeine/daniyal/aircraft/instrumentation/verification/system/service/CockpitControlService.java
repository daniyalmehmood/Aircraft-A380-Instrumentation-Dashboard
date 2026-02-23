package codeine.daniyal.aircraft.instrumentation.verification.system.service;

import codeine.daniyal.aircraft.instrumentation.verification.system.dto.CockpitSwitchDTO;
import codeine.daniyal.aircraft.instrumentation.verification.system.dto.SwitchToggleRequest;
import codeine.daniyal.aircraft.instrumentation.verification.system.entity.CockpitSwitch;
import codeine.daniyal.aircraft.instrumentation.verification.system.entity.FlightSimulationSession;
import codeine.daniyal.aircraft.instrumentation.verification.system.enums.PanelLocation;
import codeine.daniyal.aircraft.instrumentation.verification.system.enums.SwitchState;
import codeine.daniyal.aircraft.instrumentation.verification.system.repository.CockpitSwitchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CockpitControlService {

    private final CockpitSwitchRepository cockpitSwitchRepository;

    @Transactional
    public List<CockpitSwitch> initializeA380CockpitSwitches(FlightSimulationSession session) {
        List<CockpitSwitch> switches = new ArrayList<>();

        // APU Switches
        switches.add(createSwitch("APU_MASTER", "APU Master", "APU master switch", PanelLocation.OVERHEAD_APU, SwitchState.OFF, "APU", session));
        switches.add(createSwitch("APU_START", "APU Start", "APU start button", PanelLocation.OVERHEAD_APU, SwitchState.OFF, "APU", session));

        // Electrical Switches
        switches.add(createSwitch("BAT_1", "Battery 1", "Main battery 1", PanelLocation.OVERHEAD_ELECTRICAL, SwitchState.AUTO, "Electrical", session));
        switches.add(createSwitch("BAT_2", "Battery 2", "Main battery 2", PanelLocation.OVERHEAD_ELECTRICAL, SwitchState.AUTO, "Electrical", session));
        switches.add(createSwitch("GEN_1", "Generator 1", "Engine 1 generator", PanelLocation.OVERHEAD_ELECTRICAL, SwitchState.ON, "Electrical", session));
        switches.add(createSwitch("GEN_2", "Generator 2", "Engine 2 generator", PanelLocation.OVERHEAD_ELECTRICAL, SwitchState.ON, "Electrical", session));
        switches.add(createSwitch("GEN_3", "Generator 3", "Engine 3 generator", PanelLocation.OVERHEAD_ELECTRICAL, SwitchState.ON, "Electrical", session));
        switches.add(createSwitch("GEN_4", "Generator 4", "Engine 4 generator", PanelLocation.OVERHEAD_ELECTRICAL, SwitchState.ON, "Electrical", session));

        // Hydraulic Switches
        switches.add(createSwitch("HYD_GREEN", "Green Hydraulic", "Green hydraulic system", PanelLocation.OVERHEAD_HYDRAULIC, SwitchState.ON, "Hydraulic", session));
        switches.add(createSwitch("HYD_YELLOW", "Yellow Hydraulic", "Yellow hydraulic system", PanelLocation.OVERHEAD_HYDRAULIC, SwitchState.ON, "Hydraulic", session));

        // Fuel Switches
        switches.add(createSwitch("FUEL_PUMP_L", "Left Fuel Pump", "Left tank fuel pump", PanelLocation.OVERHEAD_FUEL, SwitchState.ON, "Fuel", session));
        switches.add(createSwitch("FUEL_PUMP_R", "Right Fuel Pump", "Right tank fuel pump", PanelLocation.OVERHEAD_FUEL, SwitchState.ON, "Fuel", session));
        switches.add(createSwitch("FUEL_XFEED", "Fuel Crossfeed", "Fuel crossfeed valve", PanelLocation.OVERHEAD_FUEL, SwitchState.CLOSED, "Fuel", session));

        // Air Conditioning
        switches.add(createSwitch("PACK_1", "Pack 1", "Air conditioning pack 1", PanelLocation.OVERHEAD_AIR_CONDITIONING, SwitchState.AUTO, "Air Conditioning", session));
        switches.add(createSwitch("PACK_2", "Pack 2", "Air conditioning pack 2", PanelLocation.OVERHEAD_AIR_CONDITIONING, SwitchState.AUTO, "Air Conditioning", session));

        // Anti-Ice
        switches.add(createSwitch("WING_ANTI_ICE", "Wing Anti-Ice", "Wing anti-ice system", PanelLocation.OVERHEAD_ANTI_ICE, SwitchState.OFF, "Anti-Ice", session));
        switches.add(createSwitch("ENG_ANTI_ICE_1", "Engine 1 Anti-Ice", "Engine 1 anti-ice", PanelLocation.OVERHEAD_ANTI_ICE, SwitchState.OFF, "Anti-Ice", session));
        switches.add(createSwitch("ENG_ANTI_ICE_2", "Engine 2 Anti-Ice", "Engine 2 anti-ice", PanelLocation.OVERHEAD_ANTI_ICE, SwitchState.OFF, "Anti-Ice", session));

        // Lighting
        switches.add(createSwitch("NAV_LIGHTS", "Navigation Lights", "Navigation lights", PanelLocation.OVERHEAD_LIGHTING, SwitchState.OFF, "Lighting", session));
        switches.add(createSwitch("BEACON", "Beacon", "Anti-collision beacon", PanelLocation.OVERHEAD_LIGHTING, SwitchState.OFF, "Lighting", session));
        switches.add(createSwitch("STROBE", "Strobe", "Strobe lights", PanelLocation.OVERHEAD_LIGHTING, SwitchState.OFF, "Lighting", session));
        switches.add(createSwitch("LANDING_L", "Left Landing Light", "Left landing light", PanelLocation.OVERHEAD_LIGHTING, SwitchState.OFF, "Lighting", session));
        switches.add(createSwitch("LANDING_R", "Right Landing Light", "Right landing light", PanelLocation.OVERHEAD_LIGHTING, SwitchState.OFF, "Lighting", session));

        // Signs
        switches.add(createSwitch("SEATBELT_SIGN", "Seatbelt Sign", "Fasten seatbelt sign", PanelLocation.OVERHEAD_SIGNS, SwitchState.OFF, "Cabin", session));
        switches.add(createSwitch("NO_SMOKING", "No Smoking Sign", "No smoking sign", PanelLocation.OVERHEAD_SIGNS, SwitchState.ON, "Cabin", session));

        // FCU - Autopilot
        switches.add(createSwitch("AP_1", "Autopilot 1", "Autopilot 1 engage", PanelLocation.GLARESHIELD_FCU, SwitchState.OFF, "Flight Controls", session));
        switches.add(createSwitch("AP_2", "Autopilot 2", "Autopilot 2 engage", PanelLocation.GLARESHIELD_FCU, SwitchState.OFF, "Flight Controls", session));
        switches.add(createSwitch("ATHR", "Autothrust", "Autothrust engage", PanelLocation.GLARESHIELD_FCU, SwitchState.OFF, "Flight Controls", session));
        switches.add(createSwitch("FD_1", "Flight Director 1", "Captain flight director", PanelLocation.GLARESHIELD_FCU, SwitchState.OFF, "Flight Controls", session));
        switches.add(createSwitch("LOC", "Localizer", "Localizer mode", PanelLocation.GLARESHIELD_FCU, SwitchState.OFF, "Navigation", session));
        switches.add(createSwitch("APPR", "Approach", "Approach mode", PanelLocation.GLARESHIELD_FCU, SwitchState.OFF, "Navigation", session));

        // Pedestal
        switches.add(createSwitch("FLAPS", "Flap Lever", "Flap lever position", PanelLocation.PEDESTAL_FLAPS, SwitchState.OFF, "Flight Controls", session));
        switches.add(createSwitch("SPEEDBRAKE", "Speedbrake", "Speedbrake lever", PanelLocation.PEDESTAL_SPEEDBRAKE, SwitchState.OFF, "Flight Controls", session));
        switches.add(createSwitch("GEAR", "Landing Gear", "Landing gear lever", PanelLocation.PEDESTAL_GEAR, SwitchState.OFF, "Landing Gear", session));
        switches.add(createSwitch("PARK_BRAKE", "Parking Brake", "Parking brake", PanelLocation.PEDESTAL_PARKING_BRAKE, SwitchState.ON, "Brakes", session));

        // Engine Master
        switches.add(createSwitch("ENG_MASTER_1", "Engine 1 Master", "Engine 1 master", PanelLocation.PEDESTAL_ENGINE_MASTER, SwitchState.OFF, "Engine", session));
        switches.add(createSwitch("ENG_MASTER_2", "Engine 2 Master", "Engine 2 master", PanelLocation.PEDESTAL_ENGINE_MASTER, SwitchState.OFF, "Engine", session));
        switches.add(createSwitch("ENG_MASTER_3", "Engine 3 Master", "Engine 3 master", PanelLocation.PEDESTAL_ENGINE_MASTER, SwitchState.OFF, "Engine", session));
        switches.add(createSwitch("ENG_MASTER_4", "Engine 4 Master", "Engine 4 master", PanelLocation.PEDESTAL_ENGINE_MASTER, SwitchState.OFF, "Engine", session));

        // Transponder
        switches.add(createSwitch("XPDR_MODE", "Transponder Mode", "Transponder mode", PanelLocation.PEDESTAL_TRANSPONDER, SwitchState.OFF, "Transponder", session));

        return cockpitSwitchRepository.saveAll(switches);
    }

    private CockpitSwitch createSwitch(String code, String name, String desc, PanelLocation loc, SwitchState state, String system, FlightSimulationSession session) {
        return CockpitSwitch.builder()
                .switchCode(code)
                .switchName(name)
                .description(desc)
                .panelLocation(loc)
                .currentState(state)
                .defaultState(state)
                .isGuarded(false)
                .guardOpen(false)
                .isMomentary(false)
                .isIlluminated(false)
                .isAnnunciator(false)
                .isCritical(false)
                .requiresConfirmation(false)
                .systemAffected(system)
                .isOperational(true)
                .simulationSession(session)
                .build();
    }

    @Transactional
    public CockpitSwitchDTO toggleSwitch(SwitchToggleRequest request) {
        CockpitSwitch sw;
        if (request.getSwitchId() != null) {
            sw = cockpitSwitchRepository.findById(request.getSwitchId())
                    .orElseThrow(() -> new RuntimeException("Switch not found: " + request.getSwitchId()));
        } else {
            sw = cockpitSwitchRepository.findBySwitchCode(request.getSwitchCode())
                    .orElseThrow(() -> new RuntimeException("Switch not found: " + request.getSwitchCode()));
        }

        if (request.getNewState() != null) {
            sw.setState(request.getNewState());
        } else {
            sw.toggle();
        }
        return mapToDTO(cockpitSwitchRepository.save(sw));
    }

    public List<CockpitSwitchDTO> getSwitchesByPanel(PanelLocation location) {
        return cockpitSwitchRepository.findByPanelLocation(location).stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    public List<CockpitSwitchDTO> getSwitchesBySession(Long sessionId) {
        return cockpitSwitchRepository.findBySimulationSessionId(sessionId).stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    public List<CockpitSwitchDTO> getSwitchesBySystem(String system) {
        return cockpitSwitchRepository.findBySystemAffected(system).stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Transactional
    public void resetAllSwitchesToDefault(Long sessionId) {
        List<CockpitSwitch> switches = cockpitSwitchRepository.findBySimulationSessionId(sessionId);
        for (CockpitSwitch sw : switches) {
            sw.setCurrentState(sw.getDefaultState());
            sw.setLastToggledAt(LocalDateTime.now());
        }
        cockpitSwitchRepository.saveAll(switches);
    }

    public Optional<CockpitSwitchDTO> getSwitchById(Long id) {
        return cockpitSwitchRepository.findById(id).map(this::mapToDTO);
    }

    public CockpitSwitchDTO mapToDTO(CockpitSwitch entity) {
        return CockpitSwitchDTO.builder()
                .id(entity.getId())
                .switchCode(entity.getSwitchCode())
                .switchName(entity.getSwitchName())
                .description(entity.getDescription())
                .panelLocation(entity.getPanelLocation())
                .currentState(entity.getCurrentState())
                .defaultState(entity.getDefaultState())
                .isGuarded(entity.getIsGuarded())
                .guardOpen(entity.getGuardOpen())
                .isMomentary(entity.getIsMomentary())
                .isIlluminated(entity.getIsIlluminated())
                .illuminationColor(entity.getIlluminationColor())
                .isAnnunciator(entity.getIsAnnunciator())
                .annunciatorStatus(entity.getAnnunciatorStatus())
                .positionCount(entity.getPositionCount())
                .currentPosition(entity.getCurrentPosition())
                .availableStates(entity.getAvailableStates())
                .isCritical(entity.getIsCritical())
                .requiresConfirmation(entity.getRequiresConfirmation())
                .systemAffected(entity.getSystemAffected())
                .isOperational(entity.getIsOperational())
                .failureMode(entity.getFailureMode())
                .lastToggledAt(entity.getLastToggledAt())
                .build();
    }
}

