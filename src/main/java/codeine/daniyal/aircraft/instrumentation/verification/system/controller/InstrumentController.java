package codeine.daniyal.aircraft.instrumentation.verification.system.controller;

import codeine.daniyal.aircraft.instrumentation.verification.system.dto.*;
import codeine.daniyal.aircraft.instrumentation.verification.system.enums.FlightPhase;
import codeine.daniyal.aircraft.instrumentation.verification.system.service.InstrumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/instruments")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class InstrumentController {

    private final InstrumentService instrumentService;

    @GetMapping("/sessions/{sessionId}")
    public ResponseEntity<List<FlightInstrumentDTO>> getInstrumentsBySession(@PathVariable Long sessionId) {
        return ResponseEntity.ok(instrumentService.getInstrumentsBySession(sessionId));
    }

    @GetMapping("/sessions/{sessionId}/critical")
    public ResponseEntity<List<FlightInstrumentDTO>> getCriticalInstruments(@PathVariable Long sessionId) {
        return ResponseEntity.ok(instrumentService.getCriticalInstruments(sessionId));
    }

    @PostMapping("/sessions/{sessionId}/generate")
    public ResponseEntity<List<FlightInstrumentDTO>> generateReadings(
            @PathVariable Long sessionId,
            @RequestParam FlightPhase phase) {
        return ResponseEntity.ok(instrumentService.generateRandomReadings(sessionId, phase));
    }

    @GetMapping("/sessions/{sessionId}/verify")
    public ResponseEntity<VerificationResultDTO> verifyInstruments(@PathVariable Long sessionId) {
        return ResponseEntity.ok(instrumentService.verifyInstruments(sessionId));
    }

    @GetMapping("/{instrumentId}/readings")
    public ResponseEntity<List<InstrumentReadingDTO>> getReadingsHistory(@PathVariable Long instrumentId) {
        return ResponseEntity.ok(instrumentService.getReadingsHistory(instrumentId));
    }

    @PutMapping("/{instrumentId}/value")
    public ResponseEntity<FlightInstrumentDTO> updateInstrumentValue(
            @PathVariable Long instrumentId,
            @RequestParam Double value) {
        return ResponseEntity.ok(instrumentService.updateInstrumentValue(instrumentId, value));
    }

    @PostMapping("/{instrumentId}/fail")
    public ResponseEntity<FlightInstrumentDTO> simulateFailure(
            @PathVariable Long instrumentId,
            @RequestParam String failureMode) {
        return ResponseEntity.ok(instrumentService.simulateFailure(instrumentId, failureMode));
    }

    @PostMapping("/{instrumentId}/restore")
    public ResponseEntity<FlightInstrumentDTO> restoreInstrument(@PathVariable Long instrumentId) {
        return ResponseEntity.ok(instrumentService.restoreInstrument(instrumentId));
    }
}

