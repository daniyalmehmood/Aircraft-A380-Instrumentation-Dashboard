package codeine.daniyal.aircraft.instrumentation.verification.system.entity;

import codeine.daniyal.aircraft.instrumentation.verification.system.enums.TurbulenceLevel;
import codeine.daniyal.aircraft.instrumentation.verification.system.enums.WeatherSeverity;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "weather_conditions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WeatherCondition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Wind Components
    @Column(name = "wind_direction_degrees")
    private Double windDirectionDegrees;

    @Column(name = "wind_speed_knots")
    private Double windSpeedKnots;

    @Column(name = "wind_gust_knots")
    private Double windGustKnots;

    @Column(name = "head_wind_component")
    private Double headWindComponent;

    @Column(name = "tail_wind_component")
    private Double tailWindComponent;

    @Column(name = "crosswind_component")
    private Double crosswindComponent;

    // Wind Shear
    @Column(name = "wind_shear_present")
    private Boolean windShearPresent;

    @Column(name = "wind_shear_altitude_feet")
    private Integer windShearAltitudeFeet;

    @Column(name = "wind_shear_intensity")
    private Double windShearIntensity;

    @Column(name = "microburst_detected")
    private Boolean microburstDetected;

    // Turbulence
    @Enumerated(EnumType.STRING)
    @Column(name = "turbulence_level")
    private TurbulenceLevel turbulenceLevel;

    @Column(name = "clear_air_turbulence")
    private Boolean clearAirTurbulence;

    // Visibility
    @Column(name = "visibility_statute_miles")
    private Double visibilityStatuteMiles;

    @Column(name = "visibility_meters")
    private Double visibilityMeters;

    @Column(name = "runway_visual_range_meters")
    private Integer runwayVisualRangeMeters;

    // Cloud Cover
    @Column(name = "cloud_ceiling_feet")
    private Integer cloudCeilingFeet;

    @Column(name = "cloud_coverage_percent")
    private Integer cloudCoveragePercent;

    @Column(name = "cloud_type")
    private String cloudType;

    @Column(name = "cumulonimbus_present")
    private Boolean cumulonimbusPresent;

    // Temperature and Pressure
    @Column(name = "outside_air_temp_celsius")
    private Double outsideAirTempCelsius;

    @Column(name = "dew_point_celsius")
    private Double dewPointCelsius;

    @Column(name = "relative_humidity_percent")
    private Integer relativeHumidityPercent;

    @Column(name = "barometric_pressure_hpa")
    private Double barometricPressureHPa;

    @Column(name = "barometric_pressure_inhg")
    private Double barometricPressureInHg;

    @Column(name = "qnh")
    private Double qnh;

    @Column(name = "qfe")
    private Double qfe;

    // Altitude Calculations
    @Column(name = "density_altitude_feet")
    private Integer densityAltitudeFeet;

    @Column(name = "pressure_altitude_feet")
    private Integer pressureAltitudeFeet;

    // Precipitation
    @Column(name = "precipitation_type")
    private String precipitationType;

    @Column(name = "precipitation_intensity")
    private String precipitationIntensity;

    @Column(name = "icing_conditions")
    private Boolean icingConditions;

    @Column(name = "icing_type")
    private String icingType;

    @Column(name = "icing_intensity")
    private String icingIntensity;

    // Thunderstorm
    @Column(name = "thunderstorm_activity")
    private Boolean thunderstormActivity;

    @Column(name = "lightning_detected")
    private Boolean lightningDetected;

    @Column(name = "hail_reported")
    private Boolean hailReported;

    // Overall Weather Assessment
    @Enumerated(EnumType.STRING)
    @Column(name = "weather_severity")
    private WeatherSeverity weatherSeverity;

    @Column(name = "metar_code")
    private String metarCode;

    @Column(name = "taf_code", length = 1000)
    private String tafCode;

    // Location Information
    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @Column(name = "altitude_feet")
    private Integer altitudeFeet;

    @Column(name = "airport_icao")
    private String airportIcao;

    @Column(name = "observation_time")
    private LocalDateTime observationTime;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (observationTime == null) {
            observationTime = LocalDateTime.now();
        }
    }
}

