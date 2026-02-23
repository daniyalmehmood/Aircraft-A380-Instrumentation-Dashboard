package codeine.daniyal.aircraft.instrumentation.verification.system.repository;

import codeine.daniyal.aircraft.instrumentation.verification.system.entity.Aircraft;
import codeine.daniyal.aircraft.instrumentation.verification.system.enums.AircraftStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AircraftRepository extends JpaRepository<Aircraft, Long> {

    Optional<Aircraft> findByCallsign(String callsign);

    Optional<Aircraft> findByRegistration(String registration);

    List<Aircraft> findByStatus(AircraftStatus status);

    List<Aircraft> findByAircraftType(String aircraftType);

    List<Aircraft> findByManufacturer(String manufacturer);

    List<Aircraft> findByModel(String model);

    @Query("SELECT a FROM Aircraft a WHERE a.status = :status AND a.totalFlightHours < :maxHours")
    List<Aircraft> findAvailableAircraftWithinHours(@Param("status") AircraftStatus status,
                                                     @Param("maxHours") Double maxHours);

    @Query("SELECT a FROM Aircraft a WHERE a.currentFuelKg >= :minFuel")
    List<Aircraft> findAircraftWithMinimumFuel(@Param("minFuel") Double minFuel);

    List<Aircraft> findByManufacturerAndModel(String manufacturer, String model);

    @Query("SELECT COUNT(a) FROM Aircraft a WHERE a.status = :status")
    long countByStatus(@Param("status") AircraftStatus status);
}


