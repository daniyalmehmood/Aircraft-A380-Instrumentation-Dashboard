package codeine.daniyal.aircraft.instrumentation.verification.system.dto;

import codeine.daniyal.aircraft.instrumentation.verification.system.enums.TurbulenceLevel;
import codeine.daniyal.aircraft.instrumentation.verification.system.enums.WeatherSeverity;
import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WeatherConditionDTO {
    private Long id;

    // Wind Components
    private Double windDirectionDegrees;
    private Double windSpeedKnots;
    private Double windGustKnots;
    private Double headWindComponent;
    private Double tailWindComponent;
    private Double crosswindComponent;

    // Wind Shear
    private Boolean windShearPresent;
    private Integer windShearAltitudeFeet;
    private Double windShearIntensity;
    private Boolean microburstDetected;

    // Turbulence
    private TurbulenceLevel turbulenceLevel;
    private Boolean clearAirTurbulence;

    // Visibility
    private Double visibilityStatuteMiles;
    private Double visibilityMeters;
    private Integer runwayVisualRangeMeters;

    // Cloud Cover
    private Integer cloudCeilingFeet;
    private Integer cloudCoveragePercent;
    private String cloudType;
    private Boolean cumulonimbusPresent;

    // Temperature and Pressure
    private Double outsideAirTempCelsius;
    private Double dewPointCelsius;
    private Integer relativeHumidityPercent;
    private Double barometricPressureHPa;
    private Double barometricPressureInHg;
    private Double qnh;
    private Double qfe;

    // Altitude Calculations
    private Integer densityAltitudeFeet;
    private Integer pressureAltitudeFeet;

    // Precipitation
    private String precipitationType;
    private String precipitationIntensity;
    private Boolean icingConditions;
    private String icingType;
    private String icingIntensity;

    // Thunderstorm
    private Boolean thunderstormActivity;
    private Boolean lightningDetected;
    private Boolean hailReported;

    // Overall Assessment
    private WeatherSeverity weatherSeverity;
    private String metarCode;
    private String tafCode;

    // Location
    private Double latitude;
    private Double longitude;
    private Integer altitudeFeet;
    private String airportIcao;
    private LocalDateTime observationTime;
}
