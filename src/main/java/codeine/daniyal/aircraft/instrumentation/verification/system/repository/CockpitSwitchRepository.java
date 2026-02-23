package codeine.daniyal.aircraft.instrumentation.verification.system.repository;

import codeine.daniyal.aircraft.instrumentation.verification.system.entity.CockpitSwitch;
import codeine.daniyal.aircraft.instrumentation.verification.system.enums.PanelLocation;
import codeine.daniyal.aircraft.instrumentation.verification.system.enums.SwitchState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CockpitSwitchRepository extends JpaRepository<CockpitSwitch, Long> {

    List<CockpitSwitch> findBySimulationSessionId(Long sessionId);

    List<CockpitSwitch> findByPanelLocation(PanelLocation panelLocation);

    List<CockpitSwitch> findByCurrentState(SwitchState state);

    Optional<CockpitSwitch> findBySwitchCode(String switchCode);

    Optional<CockpitSwitch> findBySimulationSessionIdAndSwitchCode(Long sessionId, String switchCode);

    List<CockpitSwitch> findBySimulationSessionIdAndPanelLocation(Long sessionId, PanelLocation panelLocation);

    @Query("SELECT c FROM CockpitSwitch c WHERE c.simulationSession.id = :sessionId AND c.isCritical = true")
    List<CockpitSwitch> findCriticalSwitchesBySession(@Param("sessionId") Long sessionId);

    @Query("SELECT c FROM CockpitSwitch c WHERE c.simulationSession.id = :sessionId " +
           "AND c.currentState != c.defaultState")
    List<CockpitSwitch> findNonDefaultSwitchesBySession(@Param("sessionId") Long sessionId);

    @Query("SELECT c FROM CockpitSwitch c WHERE c.isGuarded = true AND c.guardOpen = true")
    List<CockpitSwitch> findOpenGuardedSwitches();

    @Query("SELECT c FROM CockpitSwitch c WHERE c.simulationSession.id = :sessionId " +
           "AND c.isOperational = false")
    List<CockpitSwitch> findFailedSwitchesBySession(@Param("sessionId") Long sessionId);

    @Query("SELECT c FROM CockpitSwitch c WHERE c.systemAffected = :system")
    List<CockpitSwitch> findBySystemAffected(@Param("system") String system);

    @Query("SELECT c FROM CockpitSwitch c WHERE c.panelLocation IN :locations")
    List<CockpitSwitch> findByPanelLocationIn(@Param("locations") List<PanelLocation> locations);

    @Query("SELECT DISTINCT c.systemAffected FROM CockpitSwitch c WHERE c.systemAffected IS NOT NULL")
    List<String> findAllAffectedSystems();
}

