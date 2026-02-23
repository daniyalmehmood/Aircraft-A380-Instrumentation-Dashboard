package codeine.daniyal.aircraft.instrumentation.verification.system.controller;

import codeine.daniyal.aircraft.instrumentation.verification.system.dto.AircraftDTO;
import codeine.daniyal.aircraft.instrumentation.verification.system.enums.AircraftStatus;
import codeine.daniyal.aircraft.instrumentation.verification.system.service.AircraftService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/aircraft")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AircraftController {

    private final AircraftService aircraftService;

    @PostMapping
    public ResponseEntity<AircraftDTO> createAircraft(@RequestBody AircraftDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(aircraftService.createAircraft(dto));
    }

    @PostMapping("/a380")
    public ResponseEntity<AircraftDTO> createDefaultA380(
            @RequestParam String callsign,
            @RequestParam String registration) {
        return ResponseEntity.status(HttpStatus.CREATED).body(aircraftService.createDefaultA380(callsign, registration));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AircraftDTO> getAircraftById(@PathVariable Long id) {
        return ResponseEntity.ok(aircraftService.getAircraftById(id));
    }

    @GetMapping("/callsign/{callsign}")
    public ResponseEntity<AircraftDTO> getAircraftByCallsign(@PathVariable String callsign) {
        return ResponseEntity.ok(aircraftService.getAircraftByCallsign(callsign));
    }

    @GetMapping
    public ResponseEntity<List<AircraftDTO>> getAllAircraft() {
        return ResponseEntity.ok(aircraftService.getAllAircraft());
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<AircraftDTO>> getAircraftByStatus(@PathVariable AircraftStatus status) {
        return ResponseEntity.ok(aircraftService.getAircraftByStatus(status));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<AircraftDTO> updateStatus(@PathVariable Long id, @RequestParam AircraftStatus status) {
        return ResponseEntity.ok(aircraftService.updateStatus(id, status));
    }

    @PutMapping("/{id}/fuel")
    public ResponseEntity<AircraftDTO> updateFuel(@PathVariable Long id, @RequestParam Double fuelKg) {
        return ResponseEntity.ok(aircraftService.updateFuel(id, fuelKg));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAircraft(@PathVariable Long id) {
        aircraftService.deleteAircraft(id);
        return ResponseEntity.noContent().build();
    }
}

