package codeine.daniyal.aircraft.instrumentation.verification.system.repository;

import codeine.daniyal.aircraft.instrumentation.verification.system.entity.FlightInstrument;
import codeine.daniyal.aircraft.instrumentation.verification.system.enums.InstrumentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FlightInstrumentRepository extends JpaRepository<FlightInstrument, Long> {

    List<FlightInstrument> findBySimulationSessionId(Long sessionId);

    List<FlightInstrument> findByInstrumentType(InstrumentType instrumentType);

    List<FlightInstrument> findByIsCritical(Boolean isCritical);

    List<FlightInstrument> findByIsOperational(Boolean isOperational);

    Optional<FlightInstrument> findBySimulationSessionIdAndInstrumentType(Long sessionId,
                                                                           InstrumentType instrumentType);

    @Query("SELECT f FROM FlightInstrument f WHERE f.simulationSession.id = :sessionId AND f.isCritical = true")
    List<FlightInstrument> findCriticalInstrumentsBySession(@Param("sessionId") Long sessionId);

    @Query("SELECT f FROM FlightInstrument f WHERE f.simulationSession.id = :sessionId " +
           "AND f.verificationStatus = :status")
    List<FlightInstrument> findBySessionAndVerificationStatus(@Param("sessionId") Long sessionId,
                                                               @Param("status") String status);

    @Query("SELECT f FROM FlightInstrument f WHERE f.isOperational = false")
    List<FlightInstrument> findFailedInstruments();

    @Query("SELECT f FROM FlightInstrument f WHERE f.simulationSession.id = :sessionId " +
           "AND f.isOperational = false")
    List<FlightInstrument> findFailedInstrumentsBySession(@Param("sessionId") Long sessionId);

    @Query("SELECT COUNT(f) FROM FlightInstrument f WHERE f.simulationSession.id = :sessionId " +
           "AND f.verificationStatus = 'PASSED'")
    long countPassedInstrumentsBySession(@Param("sessionId") Long sessionId);

    @Query("SELECT COUNT(f) FROM FlightInstrument f WHERE f.simulationSession.id = :sessionId " +
           "AND f.verificationStatus = 'FAILED'")
    long countFailedInstrumentsBySession(@Param("sessionId") Long sessionId);
}

