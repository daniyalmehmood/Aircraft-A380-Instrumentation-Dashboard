package codeine.daniyal.aircraft.instrumentation.verification.system.service;

import codeine.daniyal.aircraft.instrumentation.verification.system.dto.WeatherConditionDTO;
import codeine.daniyal.aircraft.instrumentation.verification.system.entity.WeatherCondition;
import codeine.daniyal.aircraft.instrumentation.verification.system.enums.TurbulenceLevel;
import codeine.daniyal.aircraft.instrumentation.verification.system.enums.WeatherSeverity;
import codeine.daniyal.aircraft.instrumentation.verification.system.repository.WeatherConditionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class WeatherGeneratorService {

    private final WeatherConditionRepository weatherConditionRepository;
    private final Random random = new Random();

    /**
     * Generate random realistic weather conditions for aviation
     */
    public WeatherConditionDTO generateRandomWeather() {
        WeatherCondition weather = generateWeatherCondition();
        WeatherCondition saved = weatherConditionRepository.save(weather);
        return mapToDTO(saved);
    }

    /**
     * Generate weather for a specific airport
     */
    public WeatherConditionDTO generateWeatherForAirport(String airportIcao, Double latitude, Double longitude) {
        WeatherCondition weather = generateWeatherCondition();
        weather.setAirportIcao(airportIcao);
        weather.setLatitude(latitude);
        weather.setLongitude(longitude);
        WeatherCondition saved = weatherConditionRepository.save(weather);
        return mapToDTO(saved);
    }

    /**
     * Generate weather at specific altitude
     */
    public WeatherConditionDTO generateWeatherAtAltitude(Integer altitudeFeet) {
        WeatherCondition weather = generateWeatherCondition();
        weather.setAltitudeFeet(altitudeFeet);

        // Adjust temperature based on altitude (ISA: -2°C per 1000ft)
        double seaLevelTemp = weather.getOutsideAirTempCelsius();
        double altitudeAdjustment = (altitudeFeet / 1000.0) * 2.0;
        weather.setOutsideAirTempCelsius(seaLevelTemp - altitudeAdjustment);

        // Calculate density altitude
        weather.setDensityAltitudeFeet(calculateDensityAltitude(
            altitudeFeet,
            weather.getOutsideAirTempCelsius(),
            weather.getBarometricPressureHPa()
        ));

        WeatherCondition saved = weatherConditionRepository.save(weather);
        return mapToDTO(saved);
    }

    private WeatherCondition generateWeatherCondition() {
        WeatherCondition weather = new WeatherCondition();

        // Generate wind parameters
        double windDirection = random.nextDouble() * 360; // 0-360 degrees
        double windSpeed = generateWindSpeed();
        double windGust = windSpeed + random.nextDouble() * 15; // Gusts up to 15 knots above sustained

        weather.setWindDirectionDegrees(Math.round(windDirection * 10) / 10.0);
        weather.setWindSpeedKnots(Math.round(windSpeed * 10) / 10.0);
        weather.setWindGustKnots(windSpeed > 10 ? Math.round(windGust * 10) / 10.0 : null);

        // Calculate wind components (assuming runway heading 090)
        double runwayHeading = 90.0; // Default runway heading
        calculateWindComponents(weather, runwayHeading);

        // Generate wind shear parameters
        generateWindShear(weather);

        // Generate turbulence
        weather.setTurbulenceLevel(generateTurbulenceLevel());
        weather.setClearAirTurbulence(random.nextDouble() < 0.1); // 10% chance of CAT

        // Generate visibility
        generateVisibility(weather);

        // Generate cloud cover
        generateCloudCover(weather);

        // Generate temperature and pressure
        generateTemperatureAndPressure(weather);

        // Generate precipitation
        generatePrecipitation(weather);

        // Generate thunderstorm activity
        generateThunderstormActivity(weather);

        // Assess overall weather severity
        weather.setWeatherSeverity(assessWeatherSeverity(weather));

        // Generate METAR code
        weather.setMetarCode(generateMetarCode(weather));

        weather.setObservationTime(LocalDateTime.now());

        return weather;
    }

    private double generateWindSpeed() {
        // Weighted random - most days have lighter winds
        double rand = random.nextDouble();
        if (rand < 0.4) return random.nextDouble() * 10; // 40% chance: 0-10 knots
        if (rand < 0.7) return 10 + random.nextDouble() * 10; // 30% chance: 10-20 knots
        if (rand < 0.9) return 20 + random.nextDouble() * 15; // 20% chance: 20-35 knots
        return 35 + random.nextDouble() * 25; // 10% chance: 35-60 knots (strong winds)
    }

    private void calculateWindComponents(WeatherCondition weather, double runwayHeading) {
        double windDir = weather.getWindDirectionDegrees();
        double windSpeed = weather.getWindSpeedKnots();

        // Calculate angle difference
        double angleDiff = Math.toRadians(windDir - runwayHeading);

        // Head/Tail wind component (positive = headwind, negative = tailwind)
        double headwindComponent = windSpeed * Math.cos(angleDiff);

        // Crosswind component (absolute value)
        double crosswindComponent = Math.abs(windSpeed * Math.sin(angleDiff));

        if (headwindComponent >= 0) {
            weather.setHeadWindComponent(Math.round(headwindComponent * 10) / 10.0);
            weather.setTailWindComponent(0.0);
        } else {
            weather.setHeadWindComponent(0.0);
            weather.setTailWindComponent(Math.round(Math.abs(headwindComponent) * 10) / 10.0);
        }
        weather.setCrosswindComponent(Math.round(crosswindComponent * 10) / 10.0);
    }

    private void generateWindShear(WeatherCondition weather) {
        // Wind shear probability based on weather conditions
        double shearProbability = 0.05; // Base 5% chance

        if (weather.getWindSpeedKnots() > 25) shearProbability += 0.15;
        if (weather.getWindGustKnots() != null &&
            weather.getWindGustKnots() - weather.getWindSpeedKnots() > 10) {
            shearProbability += 0.20;
        }

        boolean windShearPresent = random.nextDouble() < shearProbability;
        weather.setWindShearPresent(windShearPresent);

        if (windShearPresent) {
            weather.setWindShearAltitudeFeet(random.nextInt(2000) + 100); // 100-2100 ft AGL
            weather.setWindShearIntensity(1 + random.nextDouble() * 4); // 1-5 scale
            weather.setMicroburstDetected(random.nextDouble() < 0.2); // 20% chance with shear
        } else {
            weather.setWindShearAltitudeFeet(null);
            weather.setWindShearIntensity(null);
            weather.setMicroburstDetected(false);
        }
    }

    private TurbulenceLevel generateTurbulenceLevel() {
        double rand = random.nextDouble();
        if (rand < 0.5) return TurbulenceLevel.NONE;
        if (rand < 0.75) return TurbulenceLevel.LIGHT;
        if (rand < 0.9) return TurbulenceLevel.MODERATE;
        if (rand < 0.98) return TurbulenceLevel.SEVERE;
        return TurbulenceLevel.EXTREME;
    }

    private void generateVisibility(WeatherCondition weather) {
        double rand = random.nextDouble();
        double visibilityMiles;

        if (rand < 0.6) visibilityMiles = 10.0; // 60% chance: unlimited visibility (10+ SM)
        else if (rand < 0.8) visibilityMiles = 5 + random.nextDouble() * 5; // 20% chance: 5-10 SM
        else if (rand < 0.9) visibilityMiles = 3 + random.nextDouble() * 2; // 10% chance: 3-5 SM
        else if (rand < 0.95) visibilityMiles = 1 + random.nextDouble() * 2; // 5% chance: 1-3 SM
        else visibilityMiles = random.nextDouble(); // 5% chance: <1 SM (fog/mist)

        weather.setVisibilityStatuteMiles(Math.round(visibilityMiles * 10) / 10.0);
        weather.setVisibilityMeters((double) Math.round(visibilityMiles * 1609.34)); // Convert to meters

        // RVR only relevant for low visibility
        if (visibilityMiles < 1) {
            weather.setRunwayVisualRangeMeters(200 + random.nextInt(1800)); // 200-2000m
        }
    }

    private void generateCloudCover(WeatherCondition weather) {
        double rand = random.nextDouble();

        if (rand < 0.3) {
            // Clear skies
            weather.setCloudCeilingFeet(null);
            weather.setCloudCoveragePercent(0);
            weather.setCloudType("CLR");
        } else if (rand < 0.5) {
            // Few clouds
            weather.setCloudCeilingFeet(3000 + random.nextInt(7000));
            weather.setCloudCoveragePercent(10 + random.nextInt(15));
            weather.setCloudType("FEW");
        } else if (rand < 0.7) {
            // Scattered clouds
            weather.setCloudCeilingFeet(2000 + random.nextInt(5000));
            weather.setCloudCoveragePercent(25 + random.nextInt(25));
            weather.setCloudType("SCT");
        } else if (rand < 0.85) {
            // Broken clouds
            weather.setCloudCeilingFeet(1000 + random.nextInt(4000));
            weather.setCloudCoveragePercent(50 + random.nextInt(25));
            weather.setCloudType("BKN");
        } else {
            // Overcast
            weather.setCloudCeilingFeet(500 + random.nextInt(3000));
            weather.setCloudCoveragePercent(75 + random.nextInt(25));
            weather.setCloudType("OVC");
        }

        // Cumulonimbus clouds (associated with thunderstorms)
        weather.setCumulonimbusPresent(random.nextDouble() < 0.05);
    }

    private void generateTemperatureAndPressure(WeatherCondition weather) {
        // Generate temperature (typical range: -40°C to +45°C at various altitudes)
        double baseTemp = 15 + (random.nextDouble() - 0.5) * 40; // -5°C to +35°C at sea level
        weather.setOutsideAirTempCelsius(Math.round(baseTemp * 10) / 10.0);

        // Dew point (must be <= temperature)
        double dewPoint = baseTemp - random.nextDouble() * 15;
        weather.setDewPointCelsius(Math.round(dewPoint * 10) / 10.0);

        // Relative humidity
        double tempDiff = baseTemp - dewPoint;
        int humidity = (int) Math.max(20, Math.min(100, 100 - tempDiff * 5));
        weather.setRelativeHumidityPercent(humidity);

        // Barometric pressure (standard: 1013.25 hPa / 29.92 inHg)
        double pressureHPa = 1013.25 + (random.nextDouble() - 0.5) * 50; // 988-1038 hPa
        weather.setBarometricPressureHPa(Math.round(pressureHPa * 100) / 100.0);
        weather.setBarometricPressureInHg(Math.round(pressureHPa * 0.02953 * 100) / 100.0);

        // QNH and QFE (simplified - same as pressure for sea level)
        weather.setQnh(weather.getBarometricPressureHPa());
        weather.setQfe(weather.getBarometricPressureHPa());

        // Calculate pressure altitude and density altitude
        int fieldElevation = 0; // Assume sea level for simplicity
        weather.setPressureAltitudeFeet(calculatePressureAltitude(fieldElevation, pressureHPa));
        weather.setDensityAltitudeFeet(calculateDensityAltitude(fieldElevation, baseTemp, pressureHPa));
    }

    private void generatePrecipitation(WeatherCondition weather) {
        double rand = random.nextDouble();

        if (rand < 0.7) {
            // No precipitation
            weather.setPrecipitationType(null);
            weather.setPrecipitationIntensity(null);
        } else {
            String[] types = {"RA", "SN", "DZ", "SH", "TS", "FG", "BR", "HZ"};
            String[] intensities = {"Light", "Moderate", "Heavy"};

            weather.setPrecipitationType(types[random.nextInt(types.length)]);
            weather.setPrecipitationIntensity(intensities[random.nextInt(intensities.length)]);
        }

        // Icing conditions
        if (weather.getOutsideAirTempCelsius() < 5 && weather.getOutsideAirTempCelsius() > -20) {
            if (weather.getPrecipitationType() != null ||
                (weather.getCloudCoveragePercent() != null && weather.getCloudCoveragePercent() > 50)) {
                weather.setIcingConditions(random.nextDouble() < 0.4);
                if (Boolean.TRUE.equals(weather.getIcingConditions())) {
                    String[] icingTypes = {"Rime", "Clear", "Mixed"};
                    String[] icingIntensities = {"Light", "Moderate", "Severe"};
                    weather.setIcingType(icingTypes[random.nextInt(icingTypes.length)]);
                    weather.setIcingIntensity(icingIntensities[random.nextInt(icingIntensities.length)]);
                }
            }
        } else {
            weather.setIcingConditions(false);
        }
    }

    private void generateThunderstormActivity(WeatherCondition weather) {
        boolean hasThunderstorm = Boolean.TRUE.equals(weather.getCumulonimbusPresent()) ||
                                   random.nextDouble() < 0.03;

        weather.setThunderstormActivity(hasThunderstorm);
        weather.setLightningDetected(hasThunderstorm && random.nextDouble() < 0.8);
        weather.setHailReported(hasThunderstorm && random.nextDouble() < 0.3);

        // Adjust turbulence for thunderstorms
        if (hasThunderstorm && weather.getTurbulenceLevel().getSeverity() < 2) {
            weather.setTurbulenceLevel(TurbulenceLevel.MODERATE);
        }
    }

    private WeatherSeverity assessWeatherSeverity(WeatherCondition weather) {
        int severityScore = 0;

        // Visibility scoring
        if (weather.getVisibilityStatuteMiles() < 1) severityScore += 3;
        else if (weather.getVisibilityStatuteMiles() < 3) severityScore += 2;
        else if (weather.getVisibilityStatuteMiles() < 5) severityScore += 1;

        // Ceiling scoring
        if (weather.getCloudCeilingFeet() != null) {
            if (weather.getCloudCeilingFeet() < 200) severityScore += 3;
            else if (weather.getCloudCeilingFeet() < 500) severityScore += 2;
            else if (weather.getCloudCeilingFeet() < 1000) severityScore += 1;
        }

        // Wind scoring
        if (weather.getWindSpeedKnots() > 40) severityScore += 3;
        else if (weather.getWindSpeedKnots() > 25) severityScore += 2;
        else if (weather.getWindSpeedKnots() > 15) severityScore += 1;

        // Turbulence scoring
        severityScore += weather.getTurbulenceLevel().getSeverity();

        // Other hazards
        if (Boolean.TRUE.equals(weather.getWindShearPresent())) severityScore += 2;
        if (Boolean.TRUE.equals(weather.getMicroburstDetected())) severityScore += 3;
        if (Boolean.TRUE.equals(weather.getThunderstormActivity())) severityScore += 2;
        if (Boolean.TRUE.equals(weather.getIcingConditions())) severityScore += 1;

        // Determine severity level
        if (severityScore >= 10) return WeatherSeverity.SEVERE;
        if (severityScore >= 7) return WeatherSeverity.HAZARDOUS;
        if (severityScore >= 5) return WeatherSeverity.LOW_IFR;
        if (severityScore >= 3) return WeatherSeverity.IFR;
        if (severityScore >= 1) return WeatherSeverity.MARGINAL;
        return WeatherSeverity.CLEAR;
    }

    private int calculatePressureAltitude(int fieldElevation, double pressureHPa) {
        // PA = Field Elevation + ((29.92 - Altimeter Setting) × 1000)
        double altimeterInHg = pressureHPa * 0.02953;
        return (int) (fieldElevation + ((29.92 - altimeterInHg) * 1000));
    }

    private int calculateDensityAltitude(int pressureAltitude, double temperatureCelsius, double pressureHPa) {
        // DA = PA + (120 × (OAT - ISA Temp))
        // ISA Temp at altitude = 15 - (2 × altitude in thousands)
        double isaTemp = 15 - (2 * (pressureAltitude / 1000.0));
        double tempDeviation = temperatureCelsius - isaTemp;
        return (int) (pressureAltitude + (120 * tempDeviation));
    }

    private String generateMetarCode(WeatherCondition weather) {
        StringBuilder metar = new StringBuilder();

        // Airport ICAO
        metar.append(weather.getAirportIcao() != null ? weather.getAirportIcao() : "XXXX");
        metar.append(" ");

        // Time
        LocalDateTime now = LocalDateTime.now();
        metar.append(String.format("%02d%02d%02dZ ", now.getDayOfMonth(), now.getHour(), now.getMinute()));

        // Wind
        metar.append(String.format("%03d%02d",
            weather.getWindDirectionDegrees().intValue(),
            weather.getWindSpeedKnots().intValue()));
        if (weather.getWindGustKnots() != null && weather.getWindGustKnots() > weather.getWindSpeedKnots()) {
            metar.append(String.format("G%02d", weather.getWindGustKnots().intValue()));
        }
        metar.append("KT ");

        // Visibility
        if (weather.getVisibilityStatuteMiles() >= 10) {
            metar.append("P6SM ");
        } else {
            metar.append(String.format("%.1fSM ", weather.getVisibilityStatuteMiles()));
        }

        // Weather phenomena
        if (weather.getPrecipitationType() != null) {
            metar.append(weather.getPrecipitationType()).append(" ");
        }

        // Clouds
        if (weather.getCloudType() != null && !"CLR".equals(weather.getCloudType())) {
            metar.append(String.format("%s%03d ",
                weather.getCloudType(),
                weather.getCloudCeilingFeet() != null ? weather.getCloudCeilingFeet() / 100 : 0));
        } else {
            metar.append("CLR ");
        }

        // Temperature/Dew point
        metar.append(String.format("%02d/%02d ",
            weather.getOutsideAirTempCelsius().intValue(),
            weather.getDewPointCelsius().intValue()));

        // Altimeter
        metar.append(String.format("A%04d", (int)(weather.getBarometricPressureInHg() * 100)));

        return metar.toString().trim();
    }

    public WeatherConditionDTO mapToDTO(WeatherCondition entity) {
        return WeatherConditionDTO.builder()
                .id(entity.getId())
                .windDirectionDegrees(entity.getWindDirectionDegrees())
                .windSpeedKnots(entity.getWindSpeedKnots())
                .windGustKnots(entity.getWindGustKnots())
                .headWindComponent(entity.getHeadWindComponent())
                .tailWindComponent(entity.getTailWindComponent())
                .crosswindComponent(entity.getCrosswindComponent())
                .windShearPresent(entity.getWindShearPresent())
                .windShearAltitudeFeet(entity.getWindShearAltitudeFeet())
                .windShearIntensity(entity.getWindShearIntensity())
                .microburstDetected(entity.getMicroburstDetected())
                .turbulenceLevel(entity.getTurbulenceLevel())
                .clearAirTurbulence(entity.getClearAirTurbulence())
                .visibilityStatuteMiles(entity.getVisibilityStatuteMiles())
                .visibilityMeters(entity.getVisibilityMeters())
                .runwayVisualRangeMeters(entity.getRunwayVisualRangeMeters())
                .cloudCeilingFeet(entity.getCloudCeilingFeet())
                .cloudCoveragePercent(entity.getCloudCoveragePercent())
                .cloudType(entity.getCloudType())
                .cumulonimbusPresent(entity.getCumulonimbusPresent())
                .outsideAirTempCelsius(entity.getOutsideAirTempCelsius())
                .dewPointCelsius(entity.getDewPointCelsius())
                .relativeHumidityPercent(entity.getRelativeHumidityPercent())
                .barometricPressureHPa(entity.getBarometricPressureHPa())
                .barometricPressureInHg(entity.getBarometricPressureInHg())
                .qnh(entity.getQnh())
                .qfe(entity.getQfe())
                .densityAltitudeFeet(entity.getDensityAltitudeFeet())
                .pressureAltitudeFeet(entity.getPressureAltitudeFeet())
                .precipitationType(entity.getPrecipitationType())
                .precipitationIntensity(entity.getPrecipitationIntensity())
                .icingConditions(entity.getIcingConditions())
                .icingType(entity.getIcingType())
                .icingIntensity(entity.getIcingIntensity())
                .thunderstormActivity(entity.getThunderstormActivity())
                .lightningDetected(entity.getLightningDetected())
                .hailReported(entity.getHailReported())
                .weatherSeverity(entity.getWeatherSeverity())
                .metarCode(entity.getMetarCode())
                .tafCode(entity.getTafCode())
                .latitude(entity.getLatitude())
                .longitude(entity.getLongitude())
                .altitudeFeet(entity.getAltitudeFeet())
                .airportIcao(entity.getAirportIcao())
                .observationTime(entity.getObservationTime())
                .build();
    }

    public WeatherCondition mapToEntity(WeatherConditionDTO dto) {
        return WeatherCondition.builder()
                .id(dto.getId())
                .windDirectionDegrees(dto.getWindDirectionDegrees())
                .windSpeedKnots(dto.getWindSpeedKnots())
                .windGustKnots(dto.getWindGustKnots())
                .headWindComponent(dto.getHeadWindComponent())
                .tailWindComponent(dto.getTailWindComponent())
                .crosswindComponent(dto.getCrosswindComponent())
                .windShearPresent(dto.getWindShearPresent())
                .windShearAltitudeFeet(dto.getWindShearAltitudeFeet())
                .windShearIntensity(dto.getWindShearIntensity())
                .microburstDetected(dto.getMicroburstDetected())
                .turbulenceLevel(dto.getTurbulenceLevel())
                .clearAirTurbulence(dto.getClearAirTurbulence())
                .visibilityStatuteMiles(dto.getVisibilityStatuteMiles())
                .visibilityMeters(dto.getVisibilityMeters())
                .runwayVisualRangeMeters(dto.getRunwayVisualRangeMeters())
                .cloudCeilingFeet(dto.getCloudCeilingFeet())
                .cloudCoveragePercent(dto.getCloudCoveragePercent())
                .cloudType(dto.getCloudType())
                .cumulonimbusPresent(dto.getCumulonimbusPresent())
                .outsideAirTempCelsius(dto.getOutsideAirTempCelsius())
                .dewPointCelsius(dto.getDewPointCelsius())
                .relativeHumidityPercent(dto.getRelativeHumidityPercent())
                .barometricPressureHPa(dto.getBarometricPressureHPa())
                .barometricPressureInHg(dto.getBarometricPressureInHg())
                .qnh(dto.getQnh())
                .qfe(dto.getQfe())
                .densityAltitudeFeet(dto.getDensityAltitudeFeet())
                .pressureAltitudeFeet(dto.getPressureAltitudeFeet())
                .precipitationType(dto.getPrecipitationType())
                .precipitationIntensity(dto.getPrecipitationIntensity())
                .icingConditions(dto.getIcingConditions())
                .icingType(dto.getIcingType())
                .icingIntensity(dto.getIcingIntensity())
                .thunderstormActivity(dto.getThunderstormActivity())
                .lightningDetected(dto.getLightningDetected())
                .hailReported(dto.getHailReported())
                .weatherSeverity(dto.getWeatherSeverity())
                .metarCode(dto.getMetarCode())
                .tafCode(dto.getTafCode())
                .latitude(dto.getLatitude())
                .longitude(dto.getLongitude())
                .altitudeFeet(dto.getAltitudeFeet())
                .airportIcao(dto.getAirportIcao())
                .observationTime(dto.getObservationTime())
                .build();
    }
}

