package codeine.daniyal.aircraft.instrumentation.verification.system.repository;

import codeine.daniyal.aircraft.instrumentation.verification.system.entity.FlightSimulationSession;
import codeine.daniyal.aircraft.instrumentation.verification.system.enums.FlightPhase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface FlightSimulationSessionRepository extends JpaRepository<FlightSimulationSession, Long> {

    Optional<FlightSimulationSession> findBySessionCode(String sessionCode);

    List<FlightSimulationSession> findByIsActive(Boolean isActive);

    List<FlightSimulationSession> findByIsPaused(Boolean isPaused);

    List<FlightSimulationSession> findByCurrentFlightPhase(FlightPhase flightPhase);

    List<FlightSimulationSession> findByAircraftId(Long aircraftId);

    @Query("SELECT s FROM FlightSimulationSession s WHERE s.isActive = true AND s.isPaused = false")
    List<FlightSimulationSession> findActiveRunningSessions();

    @Query("SELECT s FROM FlightSimulationSession s WHERE s.departureAirport = :airport OR s.arrivalAirport = :airport")
    List<FlightSimulationSession> findByAirport(@Param("airport") String airport);

    @Query("SELECT s FROM FlightSimulationSession s WHERE s.startedAt BETWEEN :start AND :end")
    List<FlightSimulationSession> findByStartedAtBetween(@Param("start") LocalDateTime start,
                                                          @Param("end") LocalDateTime end);

    @Query("SELECT s FROM FlightSimulationSession s WHERE s.createdAt >= :date")
    List<FlightSimulationSession> findSessionsCreatedAfter(@Param("date") LocalDateTime date);

    @Query("SELECT COUNT(s) FROM FlightSimulationSession s WHERE s.isActive = true")
    long countActiveSessions();

    @Query("SELECT s FROM FlightSimulationSession s WHERE s.aircraft.id = :aircraftId AND s.isActive = true")
    Optional<FlightSimulationSession> findActiveSessionByAircraft(@Param("aircraftId") Long aircraftId);

    @Query("SELECT s FROM FlightSimulationSession s LEFT JOIN FETCH s.instruments WHERE s.id = :id")
    Optional<FlightSimulationSession> findByIdWithInstruments(@Param("id") Long id);

    @Query("SELECT s FROM FlightSimulationSession s LEFT JOIN FETCH s.cockpitSwitches WHERE s.id = :id")
    Optional<FlightSimulationSession> findByIdWithSwitches(@Param("id") Long id);

    @Query("SELECT s FROM FlightSimulationSession s " +
           "LEFT JOIN FETCH s.instruments " +
           "LEFT JOIN FETCH s.cockpitSwitches " +
           "LEFT JOIN FETCH s.weatherCondition " +
           "WHERE s.id = :id")
    Optional<FlightSimulationSession> findByIdWithAllDetails(@Param("id") Long id);
}

