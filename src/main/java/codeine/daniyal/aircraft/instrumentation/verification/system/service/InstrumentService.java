package codeine.daniyal.aircraft.instrumentation.verification.system.service;

import codeine.daniyal.aircraft.instrumentation.verification.system.dto.*;
import codeine.daniyal.aircraft.instrumentation.verification.system.entity.*;
import codeine.daniyal.aircraft.instrumentation.verification.system.enums.*;
import codeine.daniyal.aircraft.instrumentation.verification.system.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InstrumentService {

    private final FlightInstrumentRepository instrumentRepository;
    private final InstrumentReadingRepository readingRepository;
    private final Random random = new Random();

    @Transactional
    public List<FlightInstrument> initializeA380Instruments(FlightSimulationSession session) {
        List<FlightInstrument> instruments = new ArrayList<>();

        instruments.add(createInstrument(InstrumentType.AIRSPEED_INDICATOR, "Airspeed Indicator", 0.0, 0.0, 0.0, 400.0, 2.0, 5.0, true, session));
        instruments.add(createInstrument(InstrumentType.ALTIMETER, "Altimeter", 0.0, 0.0, -1000.0, 45000.0, 1.0, 50.0, true, session));
        instruments.add(createInstrument(InstrumentType.VERTICAL_SPEED_INDICATOR, "VSI", 0.0, 0.0, -6000.0, 6000.0, 5.0, 100.0, true, session));
        instruments.add(createInstrument(InstrumentType.ATTITUDE_INDICATOR, "Attitude", 0.0, 0.0, -90.0, 90.0, 1.0, 1.0, true, session));
        instruments.add(createInstrument(InstrumentType.HEADING_INDICATOR, "Heading", 0.0, 0.0, 0.0, 360.0, 1.0, 2.0, true, session));
        instruments.add(createInstrument(InstrumentType.MACH_INDICATOR, "Mach", 0.0, 0.0, 0.0, 1.0, 1.0, 0.01, true, session));
        instruments.add(createInstrument(InstrumentType.RADIO_ALTIMETER, "Radio Alt", 0.0, 0.0, 0.0, 2500.0, 2.0, 5.0, true, session));

        for (int i = 1; i <= 4; i++) {
            instruments.add(createInstrument(InstrumentType.N1_INDICATOR, "Engine " + i + " N1", 0.0, 0.0, 0.0, 110.0, 1.0, 0.5, true, session));
            instruments.add(createInstrument(InstrumentType.EGT, "Engine " + i + " EGT", 0.0, 0.0, 0.0, 1000.0, 2.0, 10.0, true, session));
            instruments.add(createInstrument(InstrumentType.FUEL_FLOW, "Engine " + i + " FF", 0.0, 0.0, 0.0, 10000.0, 2.0, 50.0, true, session));
        }

        instruments.add(createInstrument(InstrumentType.FUEL_QUANTITY, "Total Fuel", 0.0, 0.0, 0.0, 320000.0, 1.0, 500.0, true, session));
        instruments.add(createInstrument(InstrumentType.HYDRAULIC_PRESSURE, "Green Hyd", 3000.0, 3000.0, 0.0, 5000.0, 2.0, 50.0, true, session));
        instruments.add(createInstrument(InstrumentType.ELECTRICAL_VOLTAGE, "Bus Voltage", 28.0, 28.0, 24.0, 30.0, 2.0, 0.5, true, session));
        instruments.add(createInstrument(InstrumentType.CABIN_ALTITUDE, "Cabin Alt", 0.0, 0.0, -1000.0, 15000.0, 2.0, 100.0, true, session));
        instruments.add(createInstrument(InstrumentType.OUTSIDE_AIR_TEMP, "OAT", 15.0, 15.0, -80.0, 60.0, 2.0, 1.0, false, session));

        return instrumentRepository.saveAll(instruments);
    }

    private FlightInstrument createInstrument(InstrumentType type, String name, Double current, Double expected,
                                               Double min, Double max, Double tolPercent, Double tolAbs,
                                               Boolean critical, FlightSimulationSession session) {
        return FlightInstrument.builder()
                .instrumentType(type)
                .instrumentName(name)
                .currentValue(current)
                .expectedValue(expected)
                .minValue(min)
                .maxValue(max)
                .tolerancePercent(tolPercent)
                .toleranceAbsolute(tolAbs)
                .unit(type.getUnit())
                .isCritical(critical)
                .isOperational(true)
                .verificationStatus("PENDING")
                .simulationSession(session)
                .build();
    }

    @Transactional
    public List<FlightInstrumentDTO> generateRandomReadings(Long sessionId, FlightPhase phase) {
        List<FlightInstrument> instruments = instrumentRepository.findBySimulationSessionId(sessionId);
        for (FlightInstrument inst : instruments) {
            double newValue = generateValueForPhase(inst, phase);
            inst.setCurrentValue(newValue);

            InstrumentReading reading = InstrumentReading.builder()
                    .instrument(inst)
                    .simulationSession(inst.getSimulationSession())
                    .readingValue(newValue)
                    .expectedValue(inst.getExpectedValue())
                    .isWithinTolerance(inst.isWithinTolerance())
                    .isAnomaly(false)
                    .flightPhase(phase.name())
                    .timestamp(LocalDateTime.now())
                    .build();
            readingRepository.save(reading);
        }
        instrumentRepository.saveAll(instruments);
        return instruments.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    private double generateValueForPhase(FlightInstrument inst, FlightPhase phase) {
        double min = inst.getMinValue() != null ? inst.getMinValue() : 0;
        double max = inst.getMaxValue() != null ? inst.getMaxValue() : 100;

        return switch (inst.getInstrumentType()) {
            case AIRSPEED_INDICATOR -> switch (phase) {
                case CRUISE -> 280 + random.nextDouble() * 20;
                case CLIMB -> 250 + random.nextDouble() * 50;
                case APPROACH -> 140 + random.nextDouble() * 30;
                default -> random.nextDouble() * 50;
            };
            case ALTIMETER -> switch (phase) {
                case CRUISE -> 35000 + random.nextDouble() * 8000;
                case CLIMB -> 10000 + random.nextDouble() * 25000;
                case APPROACH -> 2000 + random.nextDouble() * 5000;
                default -> random.nextDouble() * 1000;
            };
            case N1_INDICATOR -> switch (phase) {
                case CRUISE -> 75 + random.nextDouble() * 10;
                case TAKEOFF -> 85 + random.nextDouble() * 15;
                case TAXI -> 25 + random.nextDouble() * 10;
                default -> random.nextDouble() * 30;
            };
            default -> min + random.nextDouble() * (max - min);
        };
    }

    @Transactional
    public VerificationResultDTO verifyInstruments(Long sessionId) {
        List<FlightInstrument> instruments = instrumentRepository.findBySimulationSessionId(sessionId);
        int passed = 0, failed = 0, warnings = 0;
        List<InstrumentVerificationDetail> details = new ArrayList<>();

        for (FlightInstrument inst : instruments) {
            boolean ok = inst.isWithinTolerance();
            String status = ok ? "PASSED" : (Boolean.TRUE.equals(inst.getIsCritical()) ? "FAILED" : "WARNING");
            if (ok) passed++; else if ("FAILED".equals(status)) failed++; else warnings++;
            inst.setVerificationStatus(status);
            inst.setLastVerifiedAt(LocalDateTime.now());

            details.add(InstrumentVerificationDetail.builder()
                    .instrumentId(inst.getId())
                    .instrumentType(inst.getInstrumentType().name())
                    .instrumentName(inst.getInstrumentName())
                    .currentValue(inst.getCurrentValue())
                    .expectedValue(inst.getExpectedValue())
                    .status(status)
                    .isCritical(inst.getIsCritical())
                    .message(ok ? "Within tolerance" : "Out of tolerance")
                    .build());
        }
        instrumentRepository.saveAll(instruments);

        return VerificationResultDTO.builder()
                .sessionId(sessionId)
                .totalInstruments(instruments.size())
                .passedCount(passed)
                .failedCount(failed)
                .warningCount(warnings)
                .passRate(instruments.isEmpty() ? 0 : (double) passed / instruments.size() * 100)
                .overallPass(failed == 0)
                .details(details)
                .build();
    }

    public List<InstrumentReadingDTO> getReadingsHistory(Long instrumentId) {
        return readingRepository.findByInstrumentIdOrderByTimestampDesc(instrumentId).stream()
                .map(this::mapReadingToDTO).collect(Collectors.toList());
    }

    public List<FlightInstrumentDTO> getInstrumentsBySession(Long sessionId) {
        return instrumentRepository.findBySimulationSessionId(sessionId).stream()
                .map(this::mapToDTO).collect(Collectors.toList());
    }

    public List<FlightInstrumentDTO> getCriticalInstruments(Long sessionId) {
        return instrumentRepository.findCriticalInstrumentsBySession(sessionId).stream()
                .map(this::mapToDTO).collect(Collectors.toList());
    }

    @Transactional
    public FlightInstrumentDTO updateInstrumentValue(Long instrumentId, Double newValue) {
        FlightInstrument inst = instrumentRepository.findById(instrumentId)
                .orElseThrow(() -> new RuntimeException("Instrument not found: " + instrumentId));
        inst.setCurrentValue(newValue);
        return mapToDTO(instrumentRepository.save(inst));
    }

    @Transactional
    public FlightInstrumentDTO simulateFailure(Long instrumentId, String failureMode) {
        FlightInstrument inst = instrumentRepository.findById(instrumentId)
                .orElseThrow(() -> new RuntimeException("Instrument not found: " + instrumentId));
        inst.setIsOperational(false);
        inst.setFailureMode(failureMode);
        inst.setVerificationStatus("FAILED");
        return mapToDTO(instrumentRepository.save(inst));
    }

    @Transactional
    public FlightInstrumentDTO restoreInstrument(Long instrumentId) {
        FlightInstrument inst = instrumentRepository.findById(instrumentId)
                .orElseThrow(() -> new RuntimeException("Instrument not found: " + instrumentId));
        inst.setIsOperational(true);
        inst.setFailureMode(null);
        inst.setVerificationStatus("PENDING");
        return mapToDTO(instrumentRepository.save(inst));
    }

    public FlightInstrumentDTO mapToDTO(FlightInstrument e) {
        return FlightInstrumentDTO.builder()
                .id(e.getId())
                .instrumentType(e.getInstrumentType())
                .instrumentName(e.getInstrumentName())
                .currentValue(e.getCurrentValue())
                .expectedValue(e.getExpectedValue())
                .minValue(e.getMinValue())
                .maxValue(e.getMaxValue())
                .tolerancePercent(e.getTolerancePercent())
                .toleranceAbsolute(e.getToleranceAbsolute())
                .unit(e.getUnit())
                .isCritical(e.getIsCritical())
                .isOperational(e.getIsOperational())
                .verificationStatus(e.getVerificationStatus())
                .lastVerifiedAt(e.getLastVerifiedAt())
                .failureMode(e.getFailureMode())
                .calibrationDate(e.getCalibrationDate())
                .nextCalibrationDate(e.getNextCalibrationDate())
                .withinTolerance(e.isWithinTolerance())
                .build();
    }

    private InstrumentReadingDTO mapReadingToDTO(InstrumentReading e) {
        return InstrumentReadingDTO.builder()
                .id(e.getId())
                .instrumentId(e.getInstrument().getId())
                .instrumentType(e.getInstrument().getInstrumentType().name())
                .simulationSessionId(e.getSimulationSession() != null ? e.getSimulationSession().getId() : null)
                .readingValue(e.getReadingValue())
                .expectedValue(e.getExpectedValue())
                .deviation(e.getDeviation())
                .deviationPercent(e.getDeviationPercent())
                .isWithinTolerance(e.getIsWithinTolerance())
                .isAnomaly(e.getIsAnomaly())
                .anomalyDescription(e.getAnomalyDescription())
                .flightPhase(e.getFlightPhase())
                .altitudeFeet(e.getAltitudeFeet())
                .airspeedKnots(e.getAirspeedKnots())
                .headingDegrees(e.getHeadingDegrees())
                .latitude(e.getLatitude())
                .longitude(e.getLongitude())
                .timestamp(e.getTimestamp())
                .build();
    }
}

