package codeine.daniyal.aircraft.instrumentation.verification.system.service;

import codeine.daniyal.aircraft.instrumentation.verification.system.dto.*;
import codeine.daniyal.aircraft.instrumentation.verification.system.entity.*;
import codeine.daniyal.aircraft.instrumentation.verification.system.enums.*;
import codeine.daniyal.aircraft.instrumentation.verification.system.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SimulationService {

    private final FlightSimulationSessionRepository sessionRepository;
    private final AircraftRepository aircraftRepository;
    private final WeatherGeneratorService weatherGeneratorService;
    private final CockpitControlService cockpitControlService;
    private final InstrumentService instrumentService;

    @Transactional
    public SimulationSessionDTO createSimulation(CreateSimulationRequest request) {
        String sessionCode = "SIM-" + System.currentTimeMillis();

        FlightSimulationSession session = FlightSimulationSession.builder()
                .sessionCode(sessionCode)
                .sessionName(request.getSessionName() != null ? request.getSessionName() : "Simulation " + sessionCode)
                .description(request.getDescription())
                .currentFlightPhase(FlightPhase.PREFLIGHT)
                .isActive(false)
                .isPaused(false)
                .departureAirport(request.getDepartureAirport())
                .arrivalAirport(request.getArrivalAirport())
                .alternateAirport(request.getAlternateAirport())
                .flightPlanRoute(request.getFlightPlanRoute())
                .currentAltitudeFeet(0)
                .currentAirspeedKnots(0.0)
                .currentGroundSpeedKnots(0.0)
                .currentVerticalSpeedFpm(0)
                .currentHeadingDegrees(0.0)
                .autopilotEngaged(false)
                .autothrottleEngaged(false)
                .flapPosition(0)
                .gearDown(true)
                .speedbrakePosition(0)
                .parkingBrakeSet(true)
                .engine1N1(0.0).engine2N1(0.0).engine3N1(0.0).engine4N1(0.0)
                .engine1Egt(0.0).engine2Egt(0.0).engine3Egt(0.0).engine4Egt(0.0)
                .build();

        if (request.getAircraftId() != null) {
            Aircraft ac = aircraftRepository.findById(request.getAircraftId())
                    .orElseThrow(() -> new RuntimeException("Aircraft not found: " + request.getAircraftId()));
            session.setAircraft(ac);
            session.setCurrentFuelKg(ac.getCurrentFuelKg());
        }

        FlightSimulationSession saved = sessionRepository.save(session);

        if (Boolean.TRUE.equals(request.getGenerateWeather())) {
            WeatherConditionDTO weatherDTO = weatherGeneratorService.generateWeatherForAirport(
                    request.getDepartureAirport(), null, null);
            WeatherCondition weather = weatherGeneratorService.mapToEntity(weatherDTO);
            saved.setWeatherCondition(weather);
        }

        if (Boolean.TRUE.equals(request.getInitializeAllSwitches())) {
            cockpitControlService.initializeA380CockpitSwitches(saved);
        }

        if (Boolean.TRUE.equals(request.getInitializeAllInstruments())) {
            instrumentService.initializeA380Instruments(saved);
        }

        return mapToDTO(sessionRepository.save(saved));
    }

    @Transactional
    public SimulationSessionDTO startSimulation(Long sessionId) {
        FlightSimulationSession session = getById(sessionId);
        session.start();
        return mapToDTO(sessionRepository.save(session));
    }

    @Transactional
    public SimulationSessionDTO pauseSimulation(Long sessionId) {
        FlightSimulationSession session = getById(sessionId);
        session.pause();
        return mapToDTO(sessionRepository.save(session));
    }

    @Transactional
    public SimulationSessionDTO resumeSimulation(Long sessionId) {
        FlightSimulationSession session = getById(sessionId);
        session.resume();
        return mapToDTO(sessionRepository.save(session));
    }

    @Transactional
    public SimulationSessionDTO stopSimulation(Long sessionId) {
        FlightSimulationSession session = getById(sessionId);
        session.stop();
        return mapToDTO(sessionRepository.save(session));
    }

    @Transactional
    public SimulationSessionDTO updateFlightPhase(Long sessionId, FlightPhase phase) {
        FlightSimulationSession session = getById(sessionId);
        session.setCurrentFlightPhase(phase);
        return mapToDTO(sessionRepository.save(session));
    }

    @Transactional
    public SimulationSessionDTO updateSimulationParameters(Long sessionId, SimulationSessionDTO dto) {
        FlightSimulationSession session = getById(sessionId);
        if (dto.getCurrentAltitudeFeet() != null) session.setCurrentAltitudeFeet(dto.getCurrentAltitudeFeet());
        if (dto.getCurrentAirspeedKnots() != null) session.setCurrentAirspeedKnots(dto.getCurrentAirspeedKnots());
        if (dto.getCurrentHeadingDegrees() != null) session.setCurrentHeadingDegrees(dto.getCurrentHeadingDegrees());
        if (dto.getCurrentFuelKg() != null) session.setCurrentFuelKg(dto.getCurrentFuelKg());
        if (dto.getAutopilotEngaged() != null) session.setAutopilotEngaged(dto.getAutopilotEngaged());
        if (dto.getFlapPosition() != null) session.setFlapPosition(dto.getFlapPosition());
        if (dto.getGearDown() != null) session.setGearDown(dto.getGearDown());
        return mapToDTO(sessionRepository.save(session));
    }

    public SimulationSessionDTO getSimulation(Long sessionId) {
        return mapToDTO(sessionRepository.findByIdWithAllDetails(sessionId)
                .orElseThrow(() -> new RuntimeException("Simulation not found: " + sessionId)));
    }

    public SimulationSessionDTO getSimulationByCode(String code) {
        return mapToDTO(sessionRepository.findBySessionCode(code)
                .orElseThrow(() -> new RuntimeException("Simulation not found: " + code)));
    }

    public List<SimulationSessionDTO> getActiveSimulations() {
        return sessionRepository.findActiveRunningSessions().stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    public List<SimulationSessionDTO> getAllSimulations() {
        return sessionRepository.findAll().stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Transactional
    public void deleteSimulation(Long sessionId) {
        sessionRepository.deleteById(sessionId);
    }

    @Transactional
    public SimulationSessionDTO regenerateWeather(Long sessionId) {
        FlightSimulationSession session = getById(sessionId);
        WeatherConditionDTO weatherDTO = weatherGeneratorService.generateWeatherForAirport(
                session.getDepartureAirport(), session.getCurrentLatitude(), session.getCurrentLongitude());
        session.setWeatherCondition(weatherGeneratorService.mapToEntity(weatherDTO));
        return mapToDTO(sessionRepository.save(session));
    }

    private FlightSimulationSession getById(Long id) {
        return sessionRepository.findById(id).orElseThrow(() -> new RuntimeException("Simulation not found: " + id));
    }

    public SimulationSessionDTO mapToDTO(FlightSimulationSession e) {
        SimulationSessionDTO.SimulationSessionDTOBuilder b = SimulationSessionDTO.builder()
                .id(e.getId())
                .sessionCode(e.getSessionCode())
                .sessionName(e.getSessionName())
                .description(e.getDescription())
                .currentFlightPhase(e.getCurrentFlightPhase())
                .isActive(e.getIsActive())
                .isPaused(e.getIsPaused())
                .currentAltitudeFeet(e.getCurrentAltitudeFeet())
                .currentAirspeedKnots(e.getCurrentAirspeedKnots())
                .currentGroundSpeedKnots(e.getCurrentGroundSpeedKnots())
                .currentVerticalSpeedFpm(e.getCurrentVerticalSpeedFpm())
                .currentHeadingDegrees(e.getCurrentHeadingDegrees())
                .currentPitchDegrees(e.getCurrentPitchDegrees())
                .currentBankDegrees(e.getCurrentBankDegrees())
                .currentLatitude(e.getCurrentLatitude())
                .currentLongitude(e.getCurrentLongitude())
                .currentFuelKg(e.getCurrentFuelKg())
                .fuelBurnRateKgHr(e.getFuelBurnRateKgHr())
                .autopilotEngaged(e.getAutopilotEngaged())
                .autothrottleEngaged(e.getAutothrottleEngaged())
                .selectedAltitudeFeet(e.getSelectedAltitudeFeet())
                .selectedHeadingDegrees(e.getSelectedHeadingDegrees())
                .selectedSpeedKnots(e.getSelectedSpeedKnots())
                .departureAirport(e.getDepartureAirport())
                .arrivalAirport(e.getArrivalAirport())
                .alternateAirport(e.getAlternateAirport())
                .flightPlanRoute(e.getFlightPlanRoute())
                .engine1N1(e.getEngine1N1()).engine2N1(e.getEngine2N1())
                .engine3N1(e.getEngine3N1()).engine4N1(e.getEngine4N1())
                .flapPosition(e.getFlapPosition())
                .gearDown(e.getGearDown())
                .speedbrakePosition(e.getSpeedbrakePosition())
                .parkingBrakeSet(e.getParkingBrakeSet())
                .startedAt(e.getStartedAt())
                .endedAt(e.getEndedAt())
                .createdAt(e.getCreatedAt());

        if (e.getAircraft() != null) {
            b.aircraftId(e.getAircraft().getId())
             .aircraftCallsign(e.getAircraft().getCallsign())
             .aircraftType(e.getAircraft().getAircraftType());
        }

        if (e.getWeatherCondition() != null) {
            b.weatherCondition(weatherGeneratorService.mapToDTO(e.getWeatherCondition()));
        }

        return b.build();
    }
}

