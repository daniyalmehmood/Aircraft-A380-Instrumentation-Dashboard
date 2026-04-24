# ✈️ Aircraft A380 Instrumentation Dashboard

A full-stack **Airbus A380 cockpit simulation and instrumentation verification system** built with Java Spring Boot and a real-time vanilla JavaScript dashboard. The system models every major aircraft system — primary flight instruments, engine parameters, cockpit switches, weather conditions, and more — and exposes a complete REST API alongside a live browser dashboard for running, monitoring, and verifying flight simulation sessions.

---

## 📸 Preview

<img width="1919" height="909" alt="A380 Instrumentation Dashboard" src="https://github.com/user-attachments/assets/2573667d-7e28-4add-832f-8bdb467feade" />

---

## 🚀 Features

- **Real-time A380 cockpit simulation** — live instrument readings generated per flight phase
- **Primary Flight Display (PFD)** — attitude indicator, airspeed, altitude, vertical speed, heading, Mach, and ground speed
- **Engine display** — N1 %, EGT, and fuel flow for all four engines with animated gauge bars
- **Interactive cockpit controls** — throttle slider, heading selector, flap/gear/autopilot/autothrottle toggles
- **Weather & Navigation panel** — wind, visibility, OAT, QNH, turbulence scale, and a canvas-based NAV display
- **System status board** — hydraulic, electrical, pneumatic, fuel, APU, autopilot, gear, and flaps
- **ECAM message log** — scrolling Electronic Centralised Aircraft Monitor alerts
- **Instrument verification engine** — tolerance-based pass/warn/fail checks with a live progress bar
- **Full REST API** — 35+ endpoints across 6 controllers covering aircraft, simulations, instruments, cockpit, weather, and health
- **Docker support** — one-command deployment with Docker Compose

---

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────┐
│                  Browser (Frontend)                 │
│       index.html  ·  dashboard.js  ·  styles.css    │
│          Served as Spring Boot static resources     │
└────────────────────┬────────────────────────────────┘
                     │ REST (JSON)
┌────────────────────▼────────────────────────────────┐
│            Spring Boot Application                  │
│  Controllers → Services → Repositories → MySQL DB  │
│                                                     │
│  /api/aircraft       /api/simulations               │
│  /api/instruments    /api/cockpit                   │
│  /api/weather        /api/health                    │
└────────────────────┬────────────────────────────────┘
                     │ JDBC
┌────────────────────▼────────────────────────────────┐
│              MySQL 8.0 Database                     │
│        aircraft_instrumentation_db                  │
└─────────────────────────────────────────────────────┘
```

---

## 🛠️ Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 25 |
| Framework | Spring Boot 4.0.0 |
| Persistence | Spring Data JPA (Hibernate) |
| Web Layer | Spring MVC (REST) |
| Database | MySQL 8.0 |
| Frontend | Vanilla HTML5, CSS3, JavaScript |
| Icons | Font Awesome 6.4 |
| Build Tool | Maven (Maven Wrapper included) |
| Containerisation | Docker + Docker Compose |
| Code Generation | Lombok |

---

## 📋 Prerequisites

- **Java 21+** (Docker image uses Eclipse Temurin 21)
- **Maven 3.9+** or use the included `./mvnw` wrapper
- **Docker & Docker Compose** _(optional, for containerised setup)_

---

## ⚙️ Getting Started

### Option 1 — Run Locally

**1. Clone the repository**
```bash
git clone https://github.com/daniyalmehmood/Aircraft-A380-Instrumentation-Dashboard.git
cd Aircraft-A380-Instrumentation-Dashboard
```

**2. Start a MySQL instance**
```bash
docker run -d \
  --name aircraft-mysql \
  -e MYSQL_ROOT_PASSWORD=root \
  -e MYSQL_DATABASE=aircraft_instrumentation_db \
  -p 3306:3306 \
  mysql:8.0
```

**3. Configure the datasource**

Edit `src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/aircraft_instrumentation_db?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=root
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.jpa.hibernate.ddl-auto=update
```

**4. Build and run**
```bash
./mvnw clean install
./mvnw spring-boot:run
```

**5. Open the dashboard**

Navigate to [http://localhost:8080](http://localhost:8080)

---

### Option 2 — Docker Compose (Recommended)

The `docker-compose.yml` spins up both the Spring Boot application and a MySQL 8 database with a single command.

**1. Clone the repository**
```bash
git clone https://github.com/daniyalmehmood/Aircraft-A380-Instrumentation-Dashboard.git
cd Aircraft-A380-Instrumentation-Dashboard
```

**2. Start the stack**
```bash
docker compose up
```

| Service | Container | Mapped Port |
|---|---|---|
| MySQL 8.0 | `aircraft_mysql` | `3307` → `3306` |
| Spring Boot App | `aircraft_app` | `3000` → `8080` |

**3. Open the dashboard**

Navigate to [http://localhost:3000](http://localhost:3000)

To stop:
```bash
docker compose down
```

> The database is persisted in a named Docker volume (`mysql_data`) and the app container waits for a healthy MySQL healthcheck before starting.

---

### Option 3 — Build Your Own Docker Image

```bash
./mvnw clean package -DskipTests
docker build -t a380-sim .
docker compose up
```

The `Dockerfile` uses a two-stage build: a JDK Alpine image for compilation and a leaner JRE Alpine image for the runtime layer.

---

## 📁 Project Structure

```
Aircraft-A380-Instrumentation-Dashboard/
├── src/main/
│   ├── java/codeine/daniyal/aircraft/instrumentation/verification/system/
│   │   ├── config/
│   │   │   └── DataInitializer.java          # Seeds Emirates & SIA A380 on first boot
│   │   ├── controller/
│   │   │   ├── AircraftController.java        # POST/GET/PUT/DELETE /api/aircraft
│   │   │   ├── SimulationController.java      # Simulation lifecycle endpoints
│   │   │   ├── InstrumentController.java      # Instrument management & verification
│   │   │   ├── CockpitController.java         # Cockpit switch panel endpoints
│   │   │   ├── WeatherController.java         # Random/airport/altitude weather
│   │   │   └── HealthController.java          # /api/health and /api/info
│   │   ├── dto/                               # Request/response transfer objects
│   │   ├── entity/                            # JPA entity classes
│   │   ├── enums/                             # Domain enumerations
│   │   ├── exception/
│   │   │   └── GlobalExceptionHandler.java    # Centralised error handling
│   │   ├── repository/                        # Spring Data JPA repositories
│   │   └── service/                           # Business logic layer
│   └── resources/
│       ├── application.properties
│       ├── application-docker.properties      # Docker-specific datasource config
│       └── static/
│           ├── index.html                     # Single-page cockpit dashboard
│           ├── css/styles.css
│           └── js/dashboard.js
├── .mvn/wrapper/                              # Maven wrapper binaries
├── Dockerfile                                 # Multi-stage Docker build
├── docker-compose.yml
├── mvnw / mvnw.cmd
└── pom.xml
```

---

## 📡 API Reference

All endpoints accept and return JSON. Base URL: `http://localhost:3000` (Docker) or `http://localhost:8080` (local).

### Aircraft — `/api/aircraft`

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/aircraft` | Create a custom aircraft |
| `POST` | `/api/aircraft/a380?callsign=&registration=` | Create a pre-configured A380 |
| `GET` | `/api/aircraft` | List all aircraft |
| `GET` | `/api/aircraft/{id}` | Get aircraft by ID |
| `GET` | `/api/aircraft/callsign/{callsign}` | Get aircraft by callsign |
| `GET` | `/api/aircraft/status/{status}` | Filter by `AircraftStatus` |
| `PUT` | `/api/aircraft/{id}/status?status=` | Update aircraft status |
| `PUT` | `/api/aircraft/{id}/fuel?fuelKg=` | Update fuel quantity |
| `DELETE` | `/api/aircraft/{id}` | Delete aircraft |

### Simulations — `/api/simulations`

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/simulations` | Create a new simulation session |
| `GET` | `/api/simulations` | List all sessions |
| `GET` | `/api/simulations/active` | List active sessions |
| `GET` | `/api/simulations/{id}` | Get session by ID |
| `GET` | `/api/simulations/code/{sessionCode}` | Get session by code |
| `PUT` | `/api/simulations/{id}/start` | Start a session |
| `PUT` | `/api/simulations/{id}/pause` | Pause a session |
| `PUT` | `/api/simulations/{id}/resume` | Resume a paused session |
| `PUT` | `/api/simulations/{id}/stop` | Stop a session |
| `PUT` | `/api/simulations/{id}/phase?phase=` | Update flight phase |
| `PUT` | `/api/simulations/{id}/parameters` | Update flight parameters |
| `POST` | `/api/simulations/{id}/weather/regenerate` | Regenerate weather |
| `DELETE` | `/api/simulations/{id}` | Delete session |

**Create Simulation — example request body:**
```json
{
  "sessionName": "DXB-SIN Training",
  "description": "Emirates A380 Dubai to Singapore",
  "aircraftId": 1,
  "departureAirport": "OMDB",
  "arrivalAirport": "WSSS",
  "alternateAirport": "WMKK",
  "flightPlanRoute": "OMDB DCT LOVIL DCT WSSS",
  "generateWeather": true,
  "initializeAllSwitches": true,
  "initializeAllInstruments": true
}
```

### Instruments — `/api/instruments`

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/instruments/sessions/{sessionId}` | All instruments for a session |
| `GET` | `/api/instruments/sessions/{sessionId}/critical` | Critical instruments only |
| `POST` | `/api/instruments/sessions/{sessionId}/generate?phase=` | Generate phase-appropriate readings |
| `GET` | `/api/instruments/sessions/{sessionId}/verify` | Run full verification |
| `GET` | `/api/instruments/{instrumentId}/readings` | Reading history |
| `PUT` | `/api/instruments/{instrumentId}/value?value=` | Manually set a value |
| `POST` | `/api/instruments/{instrumentId}/fail?failureMode=` | Simulate instrument failure |
| `POST` | `/api/instruments/{instrumentId}/restore` | Restore a failed instrument |

### Cockpit — `/api/cockpit`

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/cockpit/sessions/{sessionId}/switches` | All switches for a session |
| `GET` | `/api/cockpit/sessions/{sessionId}/overhead` | Overhead panel switches grouped by section |
| `GET` | `/api/cockpit/sessions/{sessionId}/pedestal` | Pedestal switches grouped by section |
| `GET` | `/api/cockpit/panels` | All panel locations with descriptions |
| `GET` | `/api/cockpit/panels/{location}` | Switches at a specific panel |
| `PUT` | `/api/cockpit/switches/toggle` | Toggle or set a switch state |
| `GET` | `/api/cockpit/systems/{system}` | Switches by affected system |
| `POST` | `/api/cockpit/sessions/{sessionId}/switches/reset` | Reset all switches to defaults |

### Weather — `/api/weather`

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/weather/generate` | Fully random weather |
| `GET` | `/api/weather/generate/airport/{icao}` | Weather for a specific airport |
| `GET` | `/api/weather/generate/altitude/{feet}` | Weather at a given altitude |

### Health — `/api`

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/health` | Returns `{ status: "UP", timestamp, application }` |
| `GET` | `/api/info` | Returns name, description, and version |

---

## 🗃️ Data Model

### Aircraft
Stores full Airbus A380 specifications alongside real-time position and operational data including callsign, registration, MTOW, service ceiling, range, engine type, fuel levels, current lat/lon/altitude, and maintenance schedule.

On first application start, two aircraft are seeded automatically:
- **Emirates** `UAE001` — A380-861, Rolls-Royce Trent 900, positioned at Dubai (OMDB)
- **Singapore Airlines** `SIA321` — A380-841, Engine Alliance GP7200, positioned at Changi (WSSS)

### FlightSimulationSession
The central simulation aggregate. Tracks full flight state (altitude, airspeed, vertical speed, pitch, bank, heading), autopilot settings (engaged state, selected altitude/speed/heading/V/S/Mach), all four engine N1 % and EGT values, control surfaces (flaps, slats, speedbrake, gear, parking brake), flight plan (departure, arrival, alternate, route string), and session lifecycle timestamps. Has one-to-many relationships with `FlightInstrument`, `CockpitSwitch`, and `InstrumentReading`.

### FlightInstrument
Models a single cockpit instrument with tolerance-based verification. Stores `currentValue` vs `expectedValue` and checks them against `tolerancePercent` or `toleranceAbsolute` via a built-in `isWithinTolerance()` method. Tracks calibration dates and supports failure simulation.

### InstrumentReading
Time-series log of every instrument sample with deviation, deviation %, anomaly flag, and a full flight-state snapshot (altitude, airspeed, heading, lat/lon, flight phase) captured at the moment of each reading.

### WeatherCondition
Comprehensive meteorological model covering wind (direction, speed, gusts, shear, microburst), turbulence level, visibility, RVR, cloud ceiling, OAT, dew point, barometric pressure (hPa + inHg), QNH, QFE, density/pressure altitude, precipitation, icing, thunderstorm activity, and full METAR/TAF strings.

### CockpitSwitch
Models every physical cockpit control including panel location (50+ positions), current/default state, guarded switch support, illumination colour, annunciator status, and safety flags like `requiresConfirmation` and `isCritical`.

---

## 📊 Domain Enumerations

| Enum | Values |
|---|---|
| `FlightPhase` | `PREFLIGHT` `ENGINE_START` `TAXI` `TAKEOFF` `CLIMB` `CRUISE` `DESCENT` `APPROACH` `LANDING` `GO_AROUND` `HOLDING` `EMERGENCY` `SHUTDOWN` |
| `AircraftStatus` | `PARKED` `BOARDING` `PUSHBACK` `TAXIING` `HOLDING_SHORT` `TAKING_OFF` `AIRBORNE` `LANDING` `MAINTENANCE` `OUT_OF_SERVICE` |
| `InstrumentType` | 35+ types across PFD (airspeed, altimeter, VSI, attitude, heading), ND (HSI, DME, VOR, ADF, GPS), Engine (N1, N2, EGT, fuel flow, oil pressure/temp, EPR), Systems (fuel qty, hydraulics, electrical, cabin pressure), Environmental (OAT, TAT, SAT), plus Mach, radio altimeter, AoA, flap/gear position |
| `PanelLocation` | 50+ locations covering the full A380 cockpit: overhead sections (APU, electrical, hydraulic, fuel, pressurisation, anti-ice, fire, etc.), main panel (PFD, ND, upper/lower ECAM, standby), glareshield (FCU, EFIS, autobrake), pedestal (throttle, flaps, gear, MCDU, RMP, engine master), and side panels |
| `SwitchState` | `OFF` `ON` `AUTO` `MANUAL` `ARMED` `DISARMED` `OPEN` `CLOSED` `LOW` `HIGH` `NORMAL` `ALTERNATE` `TEST` `FAULT` |
| `TurbulenceLevel` | `NONE (0)` `LIGHT (1)` `MODERATE (2)` `SEVERE (3)` `EXTREME (4)` |
| `WeatherSeverity` | `CLEAR` `MARGINAL` `IFR` `LOW_IFR` `HAZARDOUS` `SEVERE` |

---

## 🖥️ Using the Dashboard

1. Open the dashboard URL in your browser
2. Click **New Sim** in the footer control bar — the backend initialises all A380 instruments, cockpit switches, and generates departure weather
3. Click **Start** — the header updates with callsign, flight phase, and an active status badge
4. Use the **flight phase selector** and **Readings** button to generate phase-appropriate instrument readings
5. Click **Run Verification** to check all instruments against their expected values and tolerance bands
6. Click **Weather** to regenerate meteorological conditions
7. Use **Fail** to inject an instrument failure and observe ECAM messaging and master warning annunciators
8. Click **Stop** to end the session and record the final timestamp

---

## ⚙️ Configuration

### Environment Variables (Docker)

| Variable | Description | Default |
|---|---|---|
| `SPRING_DATASOURCE_URL` | Full JDBC connection string | `jdbc:mysql://db:3306/aircraft_instrumentation_db...` |
| `SPRING_DATASOURCE_USERNAME` | Database username | `root` |
| `SPRING_DATASOURCE_PASSWORD` | Database password | `root` |

### Spring Profiles

- **Default** — connects to `localhost` MySQL (local development)
- **`docker`** — connects via `host.docker.internal` (used inside the container)

---

## 🧪 Building & Testing

```bash
# Compile
./mvnw compile

# Run tests
./mvnw test

# Package JAR (skip tests)
./mvnw clean package -DskipTests

# Build Docker image locally
docker build -t a380-sim:local .
```

---

## 🤝 Contributing

Contributions are welcome! To get started:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/your-feature`)
3. Commit your changes (`git commit -m 'Add your feature'`)
4. Push to the branch (`git push origin feature/your-feature`)
5. Open a Pull Request

---

## 📄 License

This project is open-source.

---

## 👤 Author

**Daniyal Mehmood**
GitHub: [@daniyalmehmood](https://github.com/daniyalmehmood)
