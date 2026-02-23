package codeine.daniyal.aircraft.instrumentation.verification.system.config;

import codeine.daniyal.aircraft.instrumentation.verification.system.entity.Aircraft;
import codeine.daniyal.aircraft.instrumentation.verification.system.enums.AircraftStatus;
import codeine.daniyal.aircraft.instrumentation.verification.system.repository.AircraftRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final AircraftRepository aircraftRepository;

    @Override
    public void run(String... args) throws Exception {
        if (aircraftRepository.count() == 0) {
            log.info("Initializing sample Airbus A380 aircraft data...");

            // Emirates A380
            aircraftRepository.save(Aircraft.builder()
                    .callsign("UAE001")
                    .registration("A6-EDA")
                    .aircraftType("A380-800")
                    .manufacturer("Airbus")
                    .model("A380-861")
                    .maxPassengers(489)
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
                    .currentLatitude(25.2532)
                    .currentLongitude(55.3657)
                    .currentAltitudeFeet(0)
                    .totalFlightHours(25000.0)
                    .totalCycles(8500)
                    .build());

            // Singapore Airlines A380
            aircraftRepository.save(Aircraft.builder()
                    .callsign("SIA321")
                    .registration("9V-SKA")
                    .aircraftType("A380-800")
                    .manufacturer("Airbus")
                    .model("A380-841")
                    .maxPassengers(471)
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
                    .engineType("Engine Alliance GP7200")
                    .status(AircraftStatus.PARKED)
                    .currentFuelKg(180000.0)
                    .currentLatitude(1.3644)
                    .currentLongitude(103.9915)
                    .currentAltitudeFeet(0)
                    .totalFlightHours(18000.0)
                    .totalCycles(6200)
                    .build());

            log.info("Sample aircraft data initialized successfully.");
        }
    }
}

