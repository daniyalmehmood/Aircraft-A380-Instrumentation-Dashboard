package codeine.daniyal.aircraft.instrumentation.verification.system.controller;

import codeine.daniyal.aircraft.instrumentation.verification.system.dto.*;
import codeine.daniyal.aircraft.instrumentation.verification.system.enums.FlightPhase;
import codeine.daniyal.aircraft.instrumentation.verification.system.service.SimulationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/simulations")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class SimulationController {

    private final SimulationService simulationService;

    @PostMapping
    public ResponseEntity<SimulationSessionDTO> createSimulation(@RequestBody CreateSimulationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(simulationService.createSimulation(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SimulationSessionDTO> getSimulation(@PathVariable Long id) {
        return ResponseEntity.ok(simulationService.getSimulation(id));
    }

    @GetMapping("/code/{sessionCode}")
    public ResponseEntity<SimulationSessionDTO> getSimulationByCode(@PathVariable String sessionCode) {
        return ResponseEntity.ok(simulationService.getSimulationByCode(sessionCode));
    }

    @GetMapping
    public ResponseEntity<List<SimulationSessionDTO>> getAllSimulations() {
        return ResponseEntity.ok(simulationService.getAllSimulations());
    }

    @GetMapping("/active")
    public ResponseEntity<List<SimulationSessionDTO>> getActiveSimulations() {
        return ResponseEntity.ok(simulationService.getActiveSimulations());
    }

    @PutMapping("/{id}/start")
    public ResponseEntity<SimulationSessionDTO> startSimulation(@PathVariable Long id) {
        return ResponseEntity.ok(simulationService.startSimulation(id));
    }

    @PutMapping("/{id}/pause")
    public ResponseEntity<SimulationSessionDTO> pauseSimulation(@PathVariable Long id) {
        return ResponseEntity.ok(simulationService.pauseSimulation(id));
    }

    @PutMapping("/{id}/resume")
    public ResponseEntity<SimulationSessionDTO> resumeSimulation(@PathVariable Long id) {
        return ResponseEntity.ok(simulationService.resumeSimulation(id));
    }

    @PutMapping("/{id}/stop")
    public ResponseEntity<SimulationSessionDTO> stopSimulation(@PathVariable Long id) {
        return ResponseEntity.ok(simulationService.stopSimulation(id));
    }

    @PutMapping("/{id}/phase")
    public ResponseEntity<SimulationSessionDTO> updateFlightPhase(@PathVariable Long id, @RequestParam FlightPhase phase) {
        return ResponseEntity.ok(simulationService.updateFlightPhase(id, phase));
    }

    @PutMapping("/{id}/parameters")
    public ResponseEntity<SimulationSessionDTO> updateParameters(@PathVariable Long id, @RequestBody SimulationSessionDTO dto) {
        return ResponseEntity.ok(simulationService.updateSimulationParameters(id, dto));
    }

    @PostMapping("/{id}/weather/regenerate")
    public ResponseEntity<SimulationSessionDTO> regenerateWeather(@PathVariable Long id) {
        return ResponseEntity.ok(simulationService.regenerateWeather(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSimulation(@PathVariable Long id) {
        simulationService.deleteSimulation(id);
        return ResponseEntity.noContent().build();
    }
}

