package codeine.daniyal.aircraft.instrumentation.verification.system.controller;

import codeine.daniyal.aircraft.instrumentation.verification.system.dto.WeatherConditionDTO;
import codeine.daniyal.aircraft.instrumentation.verification.system.service.WeatherGeneratorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/weather")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class WeatherController {

    private final WeatherGeneratorService weatherGeneratorService;

    @GetMapping("/generate")
    public ResponseEntity<WeatherConditionDTO> generateRandomWeather() {
        return ResponseEntity.ok(weatherGeneratorService.generateRandomWeather());
    }

    @GetMapping("/generate/airport/{icao}")
    public ResponseEntity<WeatherConditionDTO> generateWeatherForAirport(
            @PathVariable String icao,
            @RequestParam(required = false) Double latitude,
            @RequestParam(required = false) Double longitude) {
        return ResponseEntity.ok(weatherGeneratorService.generateWeatherForAirport(icao, latitude, longitude));
    }

    @GetMapping("/generate/altitude/{feet}")
    public ResponseEntity<WeatherConditionDTO> generateWeatherAtAltitude(@PathVariable Integer feet) {
        return ResponseEntity.ok(weatherGeneratorService.generateWeatherAtAltitude(feet));
    }
}

