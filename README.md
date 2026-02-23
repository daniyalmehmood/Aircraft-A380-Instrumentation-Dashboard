# ✈️ Aircraft A380 Instrumentation Dashboard

A real-time web-based instrumentation dashboard that simulates the cockpit displays of an Airbus A380. Built with a Java Spring Boot backend and a vanilla JavaScript/HTML/CSS frontend, the project visualizes key flight parameters — speed, altitude, heading, engine data, and more — in a clean, aviation-inspired UI.

---

## 📸 Preview

> <img width="1919" height="909" alt="image" src="https://github.com/user-attachments/assets/2573667d-7e28-4add-832f-8bdb467feade" />


---

## 🚀 Features

- Real-time simulation of A380 flight instrument data
- Primary Flight Display (PFD) instruments: airspeed, altitude, attitude, vertical speed, and heading
- Engine display: thrust, RPM, fuel flow, and temperature indicators
- Responsive and aviation-styled UI
- REST API backend serving live instrument data
- Docker support for quick deployment

---

## 🛠️ Tech Stack

| Layer | Technology |
|-------|-----------|
| Backend | Java, Spring Boot, Maven |
| Frontend | JavaScript, HTML5, CSS3 |
| Containerization | Docker, Docker Compose |

---

## 📋 Prerequisites

- **Java 17+**
- **Maven 3.8+** (or use the included `./mvnw` wrapper)
- **Docker & Docker Compose** _(optional, for containerized setup)_

---

## ⚙️ Getting Started

### Option 1: Run Locally

1. **Clone the repository**
   ```bash
   git clone https://github.com/daniyalmehmood/Aircraft-A380-Instrumentation-Dashboard.git
   cd Aircraft-A380-Instrumentation-Dashboard
   ```

2. **Build the project**
   ```bash
   ./mvnw clean install
   ```

3. **Run the application**
   ```bash
   ./mvnw spring-boot:run
   ```

4. **Open the dashboard**

   Navigate to [http://localhost:8080](http://localhost:8080) in your browser.

---

### Option 2: Run with Docker

1. **Clone the repository**
   ```bash
   git clone https://github.com/daniyalmehmood/Aircraft-A380-Instrumentation-Dashboard.git
   cd Aircraft-A380-Instrumentation-Dashboard
   ```

2. **Build and start the container**
   ```bash
   docker-compose up --build
   ```

3. **Open the dashboard**

   Navigate to [http://localhost:8080](http://localhost:8080) in your browser.

To stop the container:
```bash
docker-compose down
```

---

## 📁 Project Structure

```
Aircraft-A380-Instrumentation-Dashboard/
├── src/
│   └── main/
│       ├── java/           # Spring Boot application & REST controllers
│       └── resources/
│           ├── static/     # Frontend assets (JS, CSS, HTML)
│           └── templates/  # Thymeleaf templates (if used)
├── Dockerfile
├── docker-compose.yml
├── pom.xml
└── README.md
```

---

## 🔌 API Overview

The backend exposes REST endpoints that supply instrument data to the frontend. Example endpoints may include:

| Endpoint | Description |
|----------|-------------|
| `GET /api/instruments` | Returns current simulated instrument readings |
| `GET /api/engines` | Returns engine parameter data |

> 
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
- GitHub: [@daniyalmehmood](https://github.com/daniyalmehmood)
