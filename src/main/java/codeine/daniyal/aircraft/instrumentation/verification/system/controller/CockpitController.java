package codeine.daniyal.aircraft.instrumentation.verification.system.controller;

import codeine.daniyal.aircraft.instrumentation.verification.system.dto.CockpitSwitchDTO;
import codeine.daniyal.aircraft.instrumentation.verification.system.dto.SwitchToggleRequest;
import codeine.daniyal.aircraft.instrumentation.verification.system.enums.PanelLocation;
import codeine.daniyal.aircraft.instrumentation.verification.system.service.CockpitControlService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/cockpit")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CockpitController {

    private final CockpitControlService cockpitControlService;

    @GetMapping("/sessions/{sessionId}/switches")
    public ResponseEntity<List<CockpitSwitchDTO>> getSwitchesBySession(@PathVariable Long sessionId) {
        return ResponseEntity.ok(cockpitControlService.getSwitchesBySession(sessionId));
    }

    @GetMapping("/panels/{location}")
    public ResponseEntity<List<CockpitSwitchDTO>> getSwitchesByPanel(@PathVariable PanelLocation location) {
        return ResponseEntity.ok(cockpitControlService.getSwitchesByPanel(location));
    }

    @GetMapping("/panels")
    public ResponseEntity<List<Map<String, String>>> getAllPanelLocations() {
        List<Map<String, String>> panels = Arrays.stream(PanelLocation.values())
                .map(p -> Map.of("name", p.name(), "description", p.getDescription()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(panels);
    }

    @GetMapping("/switches/{id}")
    public ResponseEntity<CockpitSwitchDTO> getSwitchById(@PathVariable Long id) {
        return cockpitControlService.getSwitchById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/switches/toggle")
    public ResponseEntity<CockpitSwitchDTO> toggleSwitch(@RequestBody SwitchToggleRequest request) {
        return ResponseEntity.ok(cockpitControlService.toggleSwitch(request));
    }

    @GetMapping("/systems/{system}")
    public ResponseEntity<List<CockpitSwitchDTO>> getSwitchesBySystem(@PathVariable String system) {
        return ResponseEntity.ok(cockpitControlService.getSwitchesBySystem(system));
    }

    @PostMapping("/sessions/{sessionId}/switches/reset")
    public ResponseEntity<Void> resetSwitchesToDefault(@PathVariable Long sessionId) {
        cockpitControlService.resetAllSwitchesToDefault(sessionId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/sessions/{sessionId}/overhead")
    public ResponseEntity<Map<String, List<CockpitSwitchDTO>>> getOverheadPanelSwitches(@PathVariable Long sessionId) {
        List<CockpitSwitchDTO> allSwitches = cockpitControlService.getSwitchesBySession(sessionId);
        Map<String, List<CockpitSwitchDTO>> overheadSwitches = allSwitches.stream()
                .filter(s -> s.getPanelLocation().name().startsWith("OVERHEAD"))
                .collect(Collectors.groupingBy(s -> s.getPanelLocation().name()));
        return ResponseEntity.ok(overheadSwitches);
    }

    @GetMapping("/sessions/{sessionId}/pedestal")
    public ResponseEntity<Map<String, List<CockpitSwitchDTO>>> getPedestalSwitches(@PathVariable Long sessionId) {
        List<CockpitSwitchDTO> allSwitches = cockpitControlService.getSwitchesBySession(sessionId);
        Map<String, List<CockpitSwitchDTO>> pedestalSwitches = allSwitches.stream()
                .filter(s -> s.getPanelLocation().name().startsWith("PEDESTAL"))
                .collect(Collectors.groupingBy(s -> s.getPanelLocation().name()));
        return ResponseEntity.ok(pedestalSwitches);
    }
}

