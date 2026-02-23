package codeine.daniyal.aircraft.instrumentation.verification.system.repository;

import codeine.daniyal.aircraft.instrumentation.verification.system.entity.WeatherCondition;
import codeine.daniyal.aircraft.instrumentation.verification.system.enums.TurbulenceLevel;
import codeine.daniyal.aircraft.instrumentation.verification.system.enums.WeatherSeverity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface WeatherConditionRepository extends JpaRepository<WeatherCondition, Long> {

    List<WeatherCondition> findByAirportIcao(String airportIcao);

    Optional<WeatherCondition> findTopByAirportIcaoOrderByObservationTimeDesc(String airportIcao);

    List<WeatherCondition> findByWeatherSeverity(WeatherSeverity weatherSeverity);

    List<WeatherCondition> findByTurbulenceLevel(TurbulenceLevel turbulenceLevel);

    List<WeatherCondition> findByWindShearPresent(Boolean windShearPresent);

    @Query("SELECT w FROM WeatherCondition w WHERE w.observationTime BETWEEN :start AND :end")
    List<WeatherCondition> findByObservationTimeBetween(@Param("start") LocalDateTime start,
                                                         @Param("end") LocalDateTime end);

    @Query("SELECT w FROM WeatherCondition w WHERE w.visibilityStatuteMiles < :minVisibility")
    List<WeatherCondition> findLowVisibilityConditions(@Param("minVisibility") Double minVisibility);

    @Query("SELECT w FROM WeatherCondition w WHERE w.windSpeedKnots > :windSpeed")
    List<WeatherCondition> findHighWindConditions(@Param("windSpeed") Double windSpeed);

    @Query("SELECT w FROM WeatherCondition w WHERE w.thunderstormActivity = true")
    List<WeatherCondition> findThunderstormConditions();

    @Query("SELECT w FROM WeatherCondition w WHERE w.icingConditions = true")
    List<WeatherCondition> findIcingConditions();

    @Query("SELECT w FROM WeatherCondition w WHERE w.latitude BETWEEN :minLat AND :maxLat " +
           "AND w.longitude BETWEEN :minLon AND :maxLon")
    List<WeatherCondition> findByLocationBounds(@Param("minLat") Double minLat,
                                                 @Param("maxLat") Double maxLat,
                                                 @Param("minLon") Double minLon,
                                                 @Param("maxLon") Double maxLon);
}

