package codeine.daniyal.aircraft.instrumentation.verification.system.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateSimulationRequest {
    private String sessionName;
    private String description;
    private Long aircraftId;
    private String departureAirport;
    private String arrivalAirport;
    private String alternateAirport;
    private String flightPlanRoute;
    private Boolean generateWeather;
    private Boolean initializeAllSwitches;
    private Boolean initializeAllInstruments;
}

