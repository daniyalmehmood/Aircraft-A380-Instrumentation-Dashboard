package codeine.daniyal.aircraft.instrumentation.verification.system.repository;

import codeine.daniyal.aircraft.instrumentation.verification.system.entity.InstrumentReading;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface InstrumentReadingRepository extends JpaRepository<InstrumentReading, Long> {

    List<InstrumentReading> findByInstrumentId(Long instrumentId);

    List<InstrumentReading> findBySimulationSessionId(Long sessionId);

    List<InstrumentReading> findByInstrumentIdOrderByTimestampDesc(Long instrumentId);

    @Query("SELECT r FROM InstrumentReading r WHERE r.instrument.id = :instrumentId " +
           "ORDER BY r.timestamp DESC LIMIT 1")
    InstrumentReading findLatestReadingByInstrument(@Param("instrumentId") Long instrumentId);

    @Query("SELECT r FROM InstrumentReading r WHERE r.simulationSession.id = :sessionId " +
           "AND r.timestamp BETWEEN :start AND :end")
    List<InstrumentReading> findBySessionAndTimeRange(@Param("sessionId") Long sessionId,
                                                       @Param("start") LocalDateTime start,
                                                       @Param("end") LocalDateTime end);

    @Query("SELECT r FROM InstrumentReading r WHERE r.isAnomaly = true")
    List<InstrumentReading> findAnomalies();

    @Query("SELECT r FROM InstrumentReading r WHERE r.simulationSession.id = :sessionId AND r.isAnomaly = true")
    List<InstrumentReading> findAnomaliesBySession(@Param("sessionId") Long sessionId);

    @Query("SELECT r FROM InstrumentReading r WHERE r.isWithinTolerance = false")
    List<InstrumentReading> findOutOfToleranceReadings();

    @Query("SELECT r FROM InstrumentReading r WHERE r.simulationSession.id = :sessionId " +
           "AND r.isWithinTolerance = false")
    List<InstrumentReading> findOutOfToleranceReadingsBySession(@Param("sessionId") Long sessionId);

    @Query("SELECT AVG(r.readingValue) FROM InstrumentReading r WHERE r.instrument.id = :instrumentId")
    Double findAverageReadingByInstrument(@Param("instrumentId") Long instrumentId);

    @Query("SELECT MIN(r.readingValue) FROM InstrumentReading r WHERE r.instrument.id = :instrumentId")
    Double findMinReadingByInstrument(@Param("instrumentId") Long instrumentId);

    @Query("SELECT MAX(r.readingValue) FROM InstrumentReading r WHERE r.instrument.id = :instrumentId")
    Double findMaxReadingByInstrument(@Param("instrumentId") Long instrumentId);

    @Query("SELECT r FROM InstrumentReading r WHERE r.flightPhase = :flightPhase")
    List<InstrumentReading> findByFlightPhase(@Param("flightPhase") String flightPhase);

    void deleteBySimulationSessionId(Long sessionId);

    void deleteByInstrumentId(Long instrumentId);
}

