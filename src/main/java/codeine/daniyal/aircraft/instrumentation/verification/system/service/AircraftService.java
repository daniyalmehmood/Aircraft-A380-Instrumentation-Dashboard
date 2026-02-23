package codeine.daniyal.aircraft.instrumentation.verification.system.service;

import codeine.daniyal.aircraft.instrumentation.verification.system.dto.AircraftDTO;
import codeine.daniyal.aircraft.instrumentation.verification.system.entity.Aircraft;
import codeine.daniyal.aircraft.instrumentation.verification.system.enums.AircraftStatus;
import codeine.daniyal.aircraft.instrumentation.verification.system.repository.AircraftRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AircraftService {

    private final AircraftRepository aircraftRepository;

    @Transactional
    public AircraftDTO createAircraft(AircraftDTO dto) {
        Aircraft aircraft = mapToEntity(dto);
        Aircraft saved = aircraftRepository.save(aircraft);
        return mapToDTO(saved);
    }

    @Transactional
    public AircraftDTO createDefaultA380(String callsign, String registration) {
        Aircraft aircraft = Aircraft.builder()
                .callsign(callsign)
                .registration(registration)
                .aircraftType("A380-800")
                .manufacturer("Airbus")
                .model("A380-800")
                .maxPassengers(853)
                .maxCargoKg(60000.0)
                .maxFuelKg(320000.0)
                .emptyWeightKg(277000.0)
                .maxTakeoffWeightKg(575000.0)
                .maxLandingWeightKg(394000.0)
                .cruiseSpeedKnots(488.0)
                .maxSpeedKnots(518.0)
                .serviceCeilingFeet(43000)
                .rangeNauticalMiles(8200)
                .engineCount(4)
                .engineType("Rolls-Royce Trent 900")
                .status(AircraftStatus.PARKED)
                .currentFuelKg(150000.0)
                .totalFlightHours(0.0)
                .totalCycles(0)
                .build();
        Aircraft saved = aircraftRepository.save(aircraft);
        return mapToDTO(saved);
    }

    public AircraftDTO getAircraftById(Long id) {
        Aircraft aircraft = aircraftRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Aircraft not found with ID: " + id));
        return mapToDTO(aircraft);
    }

    public AircraftDTO getAircraftByCallsign(String callsign) {
        Aircraft aircraft = aircraftRepository.findByCallsign(callsign)
                .orElseThrow(() -> new RuntimeException("Aircraft not found with callsign: " + callsign));
        return mapToDTO(aircraft);
    }

    public List<AircraftDTO> getAllAircraft() {
        return aircraftRepository.findAll().stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    public List<AircraftDTO> getAircraftByStatus(AircraftStatus status) {
        return aircraftRepository.findByStatus(status).stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Transactional
    public AircraftDTO updateStatus(Long id, AircraftStatus status) {
        Aircraft aircraft = aircraftRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Aircraft not found with ID: " + id));
        aircraft.setStatus(status);
        return mapToDTO(aircraftRepository.save(aircraft));
    }

    @Transactional
    public AircraftDTO updateFuel(Long id, Double fuelKg) {
        Aircraft aircraft = aircraftRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Aircraft not found with ID: " + id));
        aircraft.setCurrentFuelKg(fuelKg);
        return mapToDTO(aircraftRepository.save(aircraft));
    }

    @Transactional
    public AircraftDTO updatePosition(Long id, Double latitude, Double longitude, Integer altitude, Double heading, Double speed) {
        Aircraft aircraft = aircraftRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Aircraft not found with ID: " + id));
        aircraft.setCurrentLatitude(latitude);
        aircraft.setCurrentLongitude(longitude);
        aircraft.setCurrentAltitudeFeet(altitude);
        aircraft.setCurrentHeading(heading);
        aircraft.setCurrentSpeedKnots(speed);
        return mapToDTO(aircraftRepository.save(aircraft));
    }

    @Transactional
    public void deleteAircraft(Long id) {
        aircraftRepository.deleteById(id);
    }

    public AircraftDTO mapToDTO(Aircraft entity) {
        return AircraftDTO.builder()
                .id(entity.getId())
                .callsign(entity.getCallsign())
                .registration(entity.getRegistration())
                .aircraftType(entity.getAircraftType())
                .manufacturer(entity.getManufacturer())
                .model(entity.getModel())
                .maxPassengers(entity.getMaxPassengers())
                .maxCargoKg(entity.getMaxCargoKg())
                .maxFuelKg(entity.getMaxFuelKg())
                .emptyWeightKg(entity.getEmptyWeightKg())
                .maxTakeoffWeightKg(entity.getMaxTakeoffWeightKg())
                .maxLandingWeightKg(entity.getMaxLandingWeightKg())
                .cruiseSpeedKnots(entity.getCruiseSpeedKnots())
                .maxSpeedKnots(entity.getMaxSpeedKnots())
                .serviceCeilingFeet(entity.getServiceCeilingFeet())
                .rangeNauticalMiles(entity.getRangeNauticalMiles())
                .engineCount(entity.getEngineCount())
                .engineType(entity.getEngineType())
                .status(entity.getStatus())
                .currentFuelKg(entity.getCurrentFuelKg())
                .currentLatitude(entity.getCurrentLatitude())
                .currentLongitude(entity.getCurrentLongitude())
                .currentAltitudeFeet(entity.getCurrentAltitudeFeet())
                .currentHeading(entity.getCurrentHeading())
                .currentSpeedKnots(entity.getCurrentSpeedKnots())
                .lastMaintenanceDate(entity.getLastMaintenanceDate())
                .nextMaintenanceDate(entity.getNextMaintenanceDate())
                .totalFlightHours(entity.getTotalFlightHours())
                .totalCycles(entity.getTotalCycles())
                .build();
    }

    public Aircraft mapToEntity(AircraftDTO dto) {
        return Aircraft.builder()
                .id(dto.getId())
                .callsign(dto.getCallsign())
                .registration(dto.getRegistration())
                .aircraftType(dto.getAircraftType())
                .manufacturer(dto.getManufacturer())
                .model(dto.getModel())
                .maxPassengers(dto.getMaxPassengers())
                .maxCargoKg(dto.getMaxCargoKg())
                .maxFuelKg(dto.getMaxFuelKg())
                .emptyWeightKg(dto.getEmptyWeightKg())
                .maxTakeoffWeightKg(dto.getMaxTakeoffWeightKg())
                .maxLandingWeightKg(dto.getMaxLandingWeightKg())
                .cruiseSpeedKnots(dto.getCruiseSpeedKnots())
                .maxSpeedKnots(dto.getMaxSpeedKnots())
                .serviceCeilingFeet(dto.getServiceCeilingFeet())
                .rangeNauticalMiles(dto.getRangeNauticalMiles())
                .engineCount(dto.getEngineCount())
                .engineType(dto.getEngineType())
                .status(dto.getStatus())
                .currentFuelKg(dto.getCurrentFuelKg())
                .currentLatitude(dto.getCurrentLatitude())
                .currentLongitude(dto.getCurrentLongitude())
                .currentAltitudeFeet(dto.getCurrentAltitudeFeet())
                .currentHeading(dto.getCurrentHeading())
                .currentSpeedKnots(dto.getCurrentSpeedKnots())
                .lastMaintenanceDate(dto.getLastMaintenanceDate())
                .nextMaintenanceDate(dto.getNextMaintenanceDate())
                .totalFlightHours(dto.getTotalFlightHours())
                .totalCycles(dto.getTotalCycles())
                .build();
    }
}

