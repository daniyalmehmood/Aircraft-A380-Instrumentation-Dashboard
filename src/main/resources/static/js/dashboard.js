// ===== Configuration =====
const API_BASE_URL = `${window.location.origin}/api`;
const INSTRUMENTATION_MODE = 'LOCAL'; // LOCAL keeps PFD tied to flightState physics, REMOTE uses backend data
const PFD_INSTRUMENT_TYPES = new Set([
    'AIRSPEED_INDICATOR',
    'ALTIMETER',
    'VERTICAL_SPEED_INDICATOR',
    'HEADING_INDICATOR',
    'MACH_INDICATOR',
    'GROUND_SPEED'
]);

function isLocalPfdMode() {
    return INSTRUMENTATION_MODE === 'LOCAL';
}

function shouldWritePfdFromBackend() {
    return !isLocalPfdMode();
}

function toggleFlightStateAdherence(enabled) {
    if (enabled) {
        addEcamMessage('info', 'PFD linked to backend instrumentation data.');
    } else {
        addEcamMessage('info', 'PFD linked to onboard physics simulation.');
    }
}

let currentSessionId = null;
let updateInterval = null;
let alertActive = false;
let warningCount = 0;
let dangerCount = 0;
let demoMode = false;
let currentFuel = 180000; // Starting fuel
let maxFuel = 320000;
let fuelBurnRate = 0;
let lastAlertTime = 0;
let alertCooldown = 10000; // 10 seconds between random alerts
let randomEventEnabled = true;
let gearState = 'down'; // down, up, transit
let spoilerState = 'ret'; // ret, armed, deployed
let lastFuelAlertLevel = 'normal';

// ===== Flight State Variables =====
let flightState = {
    airspeed: 0, altitude: 0, heading: 0, verticalSpeed: 0, mach: 0, groundSpeed: 0, pitch: 0, bank: 0, throttle: 0,
    engines: {
        1: { running: false, n1: 0, egt: 0, ff: 0 }, 2: { running: false, n1: 0, egt: 0, ff: 0 },
        3: { running: false, n1: 0, egt: 0, ff: 0 }, 4: { running: false, n1: 0, egt: 0, ff: 0 }
    },
    flaps: 0, gear: 'down', spoilers: 'ret',
    autopilot: { engaged: false, vs: 0 },
    autothrottle: false, parkingBrake: true, onGround: true, weight: 457000
};

// Flight physics constants
const PHYSICS = {
    operatingEmptyWeight: 277000, maxThrust: 1400000, dragCoefficient: 0.025, liftCoefficient: 0.4,
    wingArea: 845, airDensity: 1.225, gravity: 9.81, thrustPerEngine: 350000,
    climbBaselineRatio: 0.12, climbGain: 40000, descentGain: 15000, pitchToVsFactor: 350
};

// ===== Systems State =====
let tcasState = { targets: [], lastSpawnTime: 0, spawnInterval: 10000 };
let navDisplay = { mode: 'TCAS', canvas: null, ctx: null, weatherData: [], terrainData: [] };

function getConfigurationDragPenalty() {
    let penalty = 0;
    if (flightState.gear === 'down') penalty += 0.05;
    else if (flightState.gear === 'transit') penalty += 0.08;
    const flapDragPenalty = [0, 0.03, 0.06, 0.1, 0.15];
    penalty += flapDragPenalty[flightState.flaps] || 0;
    if (flightState.spoilers === 'deployed') penalty += 0.12;
    return penalty;
}

function getLiftMultiplier() {
    const flapLiftBoost = [1.0, 1.08, 1.15, 1.22, 1.3];
    let multiplier = flapLiftBoost[flightState.flaps] || 1.0;
    if (flightState.spoilers === 'deployed') multiplier *= 0.75;
    return multiplier;
}

// ===== Initialization =====
document.addEventListener('DOMContentLoaded', () => {
    updateDateTime();
    setInterval(updateDateTime, 1000);
    addEcamMessage('info', 'Dashboard initialized. Ready to create simulation.');
    checkApiConnection();
    initializeInteractiveElements();
    initializeNavDisplay();
    setInterval(mainUpdateLoop, 1000); // Physics and Nav Display loop
});

function mainUpdateLoop() {
    updateFlightPhysics();
    updateTcasSystem();
    drawNavDisplay();
}

function updateDateTime() {
    const now = new Date();
    const formatted = now.toISOString().replace('T', ' ').substring(0, 19) + ' UTC';
    document.getElementById('datetime').textContent = formatted;
}

function initializeInteractiveElements() {
    document.querySelectorAll('.instrument').forEach(inst => {
        inst.addEventListener('click', () => showInstrumentDetails(inst));
    });
    document.querySelectorAll('.engine').forEach(eng => {
        eng.addEventListener('click', () => showEngineDetails(eng));
    });
    document.querySelectorAll('.system-item').forEach(sys => {
        sys.addEventListener('click', () => toggleSystemStatus(sys));
    });
    document.getElementById('master-warning').addEventListener('click', () => {
        if (document.getElementById('master-warning').classList.contains('active')) acknowledgeWarning('warning');
    });
    document.getElementById('master-caution').addEventListener('click', () => {
        if (document.getElementById('master-caution').classList.contains('active')) acknowledgeWarning('caution');
    });
    initializeEventListeners();
}

function initializeEventListeners() {
    document.getElementById('btn-new-sim').addEventListener('click', createSimulation);
    document.getElementById('btn-start').addEventListener('click', startSimulation);
    document.getElementById('btn-pause').addEventListener('click', pauseSimulation);
    document.getElementById('btn-stop').addEventListener('click', stopSimulation);
    document.getElementById('flight-phase-select').addEventListener('change', changeFlightPhase);
    document.getElementById('btn-generate-readings').addEventListener('click', generateReadings);
    document.getElementById('btn-regenerate-weather').addEventListener('click', regenerateWeather);
    document.getElementById('random-events-toggle').addEventListener('change', toggleRandomEvents);
    document.getElementById('btn-simulate-warning').addEventListener('click', simulateWarning);
    document.getElementById('btn-simulate-failure').addEventListener('click', simulateFailure);
    document.getElementById('btn-refuel').addEventListener('click', refuelAircraft);
    document.getElementById('btn-clear-alerts').addEventListener('click', clearAlerts);
    document.getElementById('verify-btn').addEventListener('click', runVerification);
    document.getElementById('modal-close-btn').addEventListener('click', closeModal);
    document.getElementById('modal-acknowledge-btn').addEventListener('click', acknowledgeAlert);
    document.getElementById('throttle-slider').addEventListener('input', (e) => updateThrottle(e.target.value));
    document.getElementById('flaps-select').addEventListener('change', (e) => updateFlaps(e.target.value));
    document.getElementById('gear-btn').addEventListener('click', toggleGear);
    document.getElementById('spoiler-btn').addEventListener('click', toggleSpoilers);
    document.getElementById('park-brake-btn').addEventListener('click', toggleParkingBrake);
    document.getElementById('ap-btn').addEventListener('click', toggleAutopilot);
    document.getElementById('athr-btn').addEventListener('click', toggleAutothrottle);
    document.getElementById('pitch-up-btn').addEventListener('click', () => adjustPitch(1));
    document.getElementById('pitch-down-btn').addEventListener('click', () => adjustPitch(-1));
    document.getElementById('vs-up-btn').addEventListener('click', () => adjustAutopilotVS(500));
    document.getElementById('vs-down-btn').addEventListener('click', () => adjustAutopilotVS(-500));
    document.getElementById('hdg-minus-10-btn').addEventListener('click', () => adjustHeading(-10));
    document.getElementById('hdg-minus-1-btn').addEventListener('click', () => adjustHeading(-1));
    document.getElementById('hdg-plus-1-btn').addEventListener('click', () => adjustHeading(1));
    document.getElementById('hdg-plus-10-btn').addEventListener('click', () => adjustHeading(10));

    // CORRECTED: Added null checks to prevent startup error if elements don't exist
    const ndTcasBtn = document.getElementById('nd-mode-tcas');
    if (ndTcasBtn) ndTcasBtn.addEventListener('click', () => setNavDisplayMode('TCAS'));
    const ndWxrBtn = document.getElementById('nd-mode-wxr');
    if (ndWxrBtn) ndWxrBtn.addEventListener('click', () => setNavDisplayMode('WXR'));
    const ndTerrBtn = document.getElementById('nd-mode-terr');
    if (ndTerrBtn) ndTerrBtn.addEventListener('click', () => setNavDisplayMode('TERR'));
}

function showInstrumentDetails(inst) {
    const label = inst.querySelector('.instrument-label')?.textContent || 'Unknown';
    const value = inst.querySelector('.instrument-value')?.textContent || '0';
    const unit = inst.querySelector('.instrument-unit')?.textContent || '';
    const status = inst.classList.contains('danger') ? 'FAILED' :
                   inst.classList.contains('warning') ? 'WARNING' : 'NORMAL';

    const statusColor = status === 'FAILED' ? 'var(--danger-color)' :
                        status === 'WARNING' ? 'var(--warning-color)' : 'var(--success-color)';

    showInfoModal('Instrument Details', `
        <div style="text-align: center;">
            <h2 style="color: var(--accent-cyan); margin-bottom: 15px;">${label}</h2>
            <div style="font-size: 48px; font-weight: bold; color: var(--accent-green); font-family: 'Courier New', monospace;">
                ${value} <span style="font-size: 20px;">${unit}</span>
            </div>
            <div style="margin-top: 15px; padding: 10px; background: rgba(0,0,0,0.3); border-radius: 5px;">
                Status: <span style="color: ${statusColor}; font-weight: bold;">${status}</span>
            </div>
            <button onclick="testInstrument('${inst.id}')" style="margin-top: 15px; padding: 10px 20px; background: var(--accent-blue); border: none; color: white; border-radius: 5px; cursor: pointer;">
                Test Instrument
            </button>
        </div>
    `);
}

function showEngineDetails(eng) {
    const title = eng.querySelector('.engine-title')?.textContent || 'Engine';
    const n1 = eng.querySelector('[id$="-n1"]')?.textContent || '0%';
    const egt = eng.querySelector('[id$="-egt"]')?.textContent || '0°C';
    const ff = eng.querySelector('[id$="-ff"]')?.textContent || '0 kg/h';

    showInfoModal('Engine Details', `
        <div style="text-align: center;">
            <h2 style="color: var(--accent-cyan); margin-bottom: 20px;">${title}</h2>
            <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 15px;">
                <div style="background: rgba(0,0,0,0.3); padding: 15px; border-radius: 8px;">
                    <div style="color: var(--text-secondary); font-size: 12px;">N1</div>
                    <div style="font-size: 24px; color: var(--accent-green);">${n1}</div>
                </div>
                <div style="background: rgba(0,0,0,0.3); padding: 15px; border-radius: 8px;">
                    <div style="color: var(--text-secondary); font-size: 12px;">EGT</div>
                    <div style="font-size: 24px; color: var(--accent-orange);">${egt}</div>
                </div>
                <div style="background: rgba(0,0,0,0.3); padding: 15px; border-radius: 8px; grid-column: span 2;">
                    <div style="color: var(--text-secondary); font-size: 12px;">FUEL FLOW</div>
                    <div style="font-size: 24px; color: var(--accent-cyan);">${ff}</div>
                </div>
            </div>
            <div style="margin-top: 20px; display: flex; gap: 10px; justify-content: center;">
                <button onclick="shutdownEngine('${eng.id}')" style="padding: 10px 20px; background: var(--danger-color); border: none; color: white; border-radius: 5px; cursor: pointer;">
                    Shutdown
                </button>
                <button onclick="startEngine('${eng.id}')" style="padding: 10px 20px; background: var(--success-color); border: none; color: white; border-radius: 5px; cursor: pointer;">
                    Start
                </button>
            </div>
        </div>
    `);
}

function toggleSystemStatus(sys) {
    const statusEl = sys.querySelector('.system-status');
    const currentStatus = statusEl?.textContent;
    const systemName = sys.querySelector('.system-name')?.textContent;
    const systemId = sys.id;

    // Toggle between states
    if (currentStatus === 'OK' || currentStatus === 'ON' || currentStatus === 'DOWN' || currentStatus === 'UP' || currentStatus === 'RET') {
        // Turn system off/fail
        if(statusEl) statusEl.textContent = 'FAIL';
        sys.classList.remove('ok');
        sys.classList.add('danger');
        addEcamMessage('danger', `${systemName} SYSTEM FAILURE`);
        triggerCaution();

        // Apply system-specific effects
        applySystemFailureEffects(systemId, true);
    } else {
        // Restore system based on its type
        let restoredStatus = 'OK';
        if (systemId === 'sys-apu' || systemId === 'sys-autopilot') restoredStatus = 'OFF';
        if (systemId === 'sys-gear') restoredStatus = 'DOWN';
        if (systemId === 'sys-flaps') restoredStatus = 'UP';

        if(statusEl) statusEl.textContent = restoredStatus;
        sys.classList.remove('warning', 'danger');
        sys.classList.add('ok');
        addEcamMessage('info', `${systemName} system restored`);

        // Remove failure effects
        applySystemFailureEffects(systemId, false);
    }
}

function applySystemFailureEffects(systemId, failed) {
    switch (systemId) {
        case 'sys-hydraulic':
            if (failed) {
                addEcamMessage('warning', 'HYD - Flight controls degraded');
                addEcamMessage('warning', 'HYD - Gear may not extend normally');
            } else {
                addEcamMessage('info', 'HYD - Normal operation restored');
            }
            break;

        case 'sys-electrical':
            if (failed) {
                addEcamMessage('danger', 'ELEC - Multiple system failures possible');
                addEcamMessage('warning', 'ELEC - Switching to backup power');
                document.querySelectorAll('.instrument').forEach((inst) => {
                    if (Math.random() > 0.7) {
                        inst.classList.add('warning');
                    }
                });
            } else {
                document.querySelectorAll('.instrument').forEach(inst => {
                    inst.classList.remove('warning');
                });
                addEcamMessage('info', 'ELEC - All buses powered');
            }
            break;

        case 'sys-pneumatic':
            if (failed) {
                addEcamMessage('warning', 'BLEED - Cabin pressurization affected');
                addEcamMessage('warning', 'BLEED - Engine anti-ice unavailable');
            } else {
                addEcamMessage('info', 'BLEED - Normal operation');
            }
            break;

        case 'sys-fuel':
            if (failed) {
                addEcamMessage('danger', 'FUEL - Cross-feed required');
                addEcamMessage('warning', 'FUEL - Monitor fuel balance');
                // Example of a physics effect: increase fuel consumption
                fuelBurnRate *= 1.5;
            } else {
                // Restore the original fuel burn rate
                fuelBurnRate = fuelBurnRate / 1.5;
                addEcamMessage('info', 'FUEL - System normal');
            }
            break;

        case 'sys-apu':
            if (failed) {
                addEcamMessage('warning', 'APU - Unavailable for restart');
            } else {
                addEcamMessage('info', 'APU - Available');
            }
            break;

        case 'sys-autopilot':
            if (failed) {
                if (flightState.autopilot.engaged) toggleAutopilot(); // Turn it off
                addEcamMessage('warning', 'A/P DISCONNECT - Manual flight required');
            } else {
                addEcamMessage('info', 'A/P - System available');
            }
            break;

        case 'sys-gear':
            if (failed) {
                addEcamMessage('danger', 'GEAR - Manual extension may be required');
                addEcamMessage('warning', 'GEAR - Check three greens');
            }
            break;

        case 'sys-flaps':
            if (failed) {
                addEcamMessage('danger', 'FLAPS - Asymmetric flap possible');
                addEcamMessage('warning', 'FLAPS - Use alternate extension');
            }
            break;
    }
}

function testInstrument(instId) {
    closeInfoModal();
    const inst = document.getElementById(instId);
    if (inst) {
        inst.classList.add('warning');
        addEcamMessage('info', 'Instrument test initiated...');
        setTimeout(() => {
            inst.classList.remove('warning');
            addEcamMessage('info', 'Instrument test PASSED');
        }, 2000);
    }
}


function shutdownEngine(engId) {
    closeInfoModal();
    const engNum = parseInt(engId.replace('engine-', ''));
    if (!flightState.engines[engNum].running) return;
    flightState.engines[engNum].running = false;
    updateEngineN1(engNum, 0); updateEngineEgt(engNum, 0); updateEngineFuelFlow(engNum, 0);
    document.getElementById(engId)?.classList.add('warning');
    const runningEngines = countRunningEngines();
    addEcamMessage('danger', `ENGINE ${engNum} SHUTDOWN - ${runningEngines} ENGINE(S) REMAINING`);
    if (runningEngines === 0 && !flightState.onGround) {
        addEcamMessage('danger', 'ALL ENGINES FLAMEOUT - EMERGENCY');
        triggerDanger();
    }
}

function startEngine(engId) {
    closeInfoModal();
    const engNum = parseInt(engId.replace('engine-', ''));
    if (flightState.engines[engNum].running || currentFuel <= 0) return;
    addEcamMessage('info', `ENGINE ${engNum} START SEQUENCE INITIATED`);
    document.getElementById(engId)?.classList.remove('warning', 'danger');
    let n1 = 0;
    const startInterval = setInterval(() => {
        n1 += 3; updateEngineN1(engNum, n1); updateEngineEgt(engNum, n1 * 15);
        if (n1 >= 25) {
            clearInterval(startInterval);
            flightState.engines[engNum].running = true;
            flightState.engines[engNum].n1 = 25;
            flightState.engines[engNum].egt = 400;
            flightState.engines[engNum].ff = 800;
            updateEngineN1(engNum, 25); updateEngineEgt(engNum, 400); updateEngineFuelFlow(engNum, 800);
            addEcamMessage('info', `ENGINE ${engNum} RUNNING - IDLE`);
            applyThrottleToEngine(engNum, flightState.throttle);
        }
    }, 300);
}

function countRunningEngines() { return Object.values(flightState.engines).filter(e => e.running).length; }

function applyThrottleToEngine(engNum, throttlePercent) {
    if (!flightState.engines[engNum].running) return;
    const n1 = 25 + (throttlePercent / 100) * 70;
    const egt = 400 + (throttlePercent / 100) * 500;
    const ff = 800 + (throttlePercent / 100) * 4500;
    flightState.engines[engNum].n1 = n1;
    flightState.engines[engNum].egt = egt;
    flightState.engines[engNum].ff = ff;
    updateEngineN1(engNum, n1); updateEngineEgt(engNum, egt); updateEngineFuelFlow(engNum, ff);
}

function showInfoModal(title, content) {
    document.getElementById('modal-title').textContent = title;
    document.getElementById('modal-body').innerHTML = content;

    // Style the modal for informational purposes (blue theme)
    const modalContent = document.querySelector('.modal-content');
    if (modalContent) modalContent.style.borderColor = 'var(--accent-cyan)';

    const modalHeader = document.querySelector('.modal-header');
    if (modalHeader) modalHeader.style.background = 'var(--accent-blue)';

    const acknowledgeButton = document.querySelector('.btn-acknowledge');
    if (acknowledgeButton) {
        acknowledgeButton.textContent = 'CLOSE';
        acknowledgeButton.style.background = 'var(--accent-blue)';
    }

    // Show the modal
    document.getElementById('alert-modal').classList.add('active');
}

function closeInfoModal() {
    // Hide the modal
    document.getElementById('alert-modal').classList.remove('active');

    // Reset modal styling back to the default "danger" state for the next alert
    const modalContent = document.querySelector('.modal-content');
    if (modalContent) modalContent.style.borderColor = 'var(--danger-color)';

    const modalHeader = document.querySelector('.modal-header');
    if (modalHeader) modalHeader.style.background = 'var(--danger-color)';

    const acknowledgeButton = document.querySelector('.btn-acknowledge');
    if (acknowledgeButton) {
        acknowledgeButton.textContent = 'ACKNOWLEDGE';
        acknowledgeButton.style.background = 'var(--danger-color)';
    }
}

function acknowledgeWarning(type) {
    if (type === 'warning') {
        // Acknowledges a MASTER WARNING (Danger)
        document.getElementById('master-warning').classList.remove('active');
        document.getElementById('alert-overlay').classList.add('hidden');
        document.getElementById('dashboard').classList.remove('danger-state');
        dangerCount = 0; // Reset the count for this level of alert
    } else {
        // Acknowledges a MASTER CAUTION (Warning)
        document.getElementById('master-caution').classList.remove('active');
        document.getElementById('dashboard').classList.remove('warning-state');
        warningCount = 0; // Reset the count for this level of alert
    }

    addEcamMessage('info', `${type.toUpperCase()} acknowledged by crew.`);
}


async function checkApiConnection() {
    try {
        const response = await fetch(`${API_BASE_URL}/health`);
        addEcamMessage(response.ok ? 'info' : 'warning', response.ok ? 'API connection established.' : 'API connection issue. Using DEMO mode.');
    } catch (error) {
        addEcamMessage('danger', 'Cannot connect to API. DEMO mode will be used.');
    }
}

async function createSimulation() {
    addEcamMessage('info', 'Creating new simulation...');
    stopPeriodicUpdates();
    try {
        const response = await fetch(`${API_BASE_URL}/simulations`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ sessionName: 'A380 Flight Simulation', description: 'Instrumentation verification session' })
        });
        if (!response.ok) throw new Error(`Server responded with status: ${response.status}`);
        const data = await response.json();
        currentSessionId = data.id;
        demoMode = false;
        addEcamMessage('info', `Simulation ${data.sessionCode} created successfully.`);
        updateSimulationDisplay(data);
        if (data.weatherCondition) updateWeatherDisplay(data.weatherCondition);
        initializeFlightStateForGround();
        enableControls();
    } catch (error) {
        addEcamMessage('warning', 'Backend unavailable - Starting DEMO mode');
        demoMode = true; currentSessionId = 'DEMO-123';
        updateSimulationDisplay({ aircraftCallsign: 'DEMO-A380', currentFlightPhase: 'PREFLIGHT', isActive: false, isPaused: false });
        initializeFlightStateForGround();
        enableControls();
    }
}

function initializeFlightStateForGround() {
    flightState = { ...flightState, airspeed: 0, altitude: 0, heading: 270, verticalSpeed: 0, onGround: true, gear: 'down', flaps: 0, spoilers: 'ret', parkingBrake: true, throttle: 0, pitch: 0, bank: 0, autopilot: { engaged: false, vs: 0 } };
    for (let i = 1; i <= 4; i++) { flightState.engines[i].running = false; updateEngineN1(i, 0); updateEngineEgt(i, 0); updateEngineFuelFlow(i, 0); }
    gearState = 'down'; document.getElementById('throttle-slider').value = 0; document.getElementById('throttle-value').textContent = '0%'; document.getElementById('hdg-select').textContent = '270°'; document.getElementById('flaps-select').value = '0';
    document.getElementById('gear-btn').innerHTML = '<i class="fas fa-circle"></i> DOWN'; document.getElementById('park-brake-btn').classList.add('active'); document.getElementById('park-brake-btn').textContent = 'SET';
    currentFuel = 180000; updateFuelDisplay(currentFuel, maxFuel); updateFlightDisplays();
    addEcamMessage('info', 'Aircraft on ground - All systems ready');
}

async function startSimulation() {
    // Handle demo mode first if it's active or if there's no session ID
    if (demoMode || !currentSessionId) {
        document.getElementById('simulation-status').textContent = 'ACTIVE';
        document.getElementById('simulation-status').className = 'status-badge status-active';
        document.getElementById('btn-start').disabled = true;
        document.getElementById('btn-pause').disabled = false;
        document.getElementById('btn-stop').disabled = false;
        addEcamMessage('info', 'Simulation started.');
        startPeriodicUpdates();
        return;
    }

    // API Mode: Send a request to the backend to start the simulation
    try {
        const response = await fetch(`${API_BASE_URL}/simulations/${currentSessionId}/start`, {
            method: 'PUT'
        });

        if (response.ok) {
            const data = await response.json();
            updateSimulationDisplay(data); // Update UI with state from the server
            addEcamMessage('info', 'Simulation started.');

            // Update button states to reflect the new simulation status
            document.getElementById('btn-start').disabled = true;
            document.getElementById('btn-pause').disabled = false;
            document.getElementById('btn-stop').disabled = false;

            // Start the periodic update loops for readings and random events
            startPeriodicUpdates();
        } else {
             addEcamMessage('danger', `Failed to start simulation. Server responded with status: ${response.status}`);
        }
    } catch (error) {
        addEcamMessage('danger', 'Failed to start simulation: ' + error.message);
    }
}

async function pauseSimulation() {
    // Handle demo mode first
    if (demoMode || !currentSessionId) {
        document.getElementById('simulation-status').textContent = 'PAUSED';
        document.getElementById('simulation-status').className = 'status-badge status-paused';
        document.getElementById('btn-start').disabled = false; // Allow resuming
        document.getElementById('btn-pause').disabled = true;
        addEcamMessage('warning', 'Simulation paused.');
        stopPeriodicUpdates(); // Stop the update loops
        return;
    }

    // API Mode: Send a request to the backend to pause
    try {
        const response = await fetch(`${API_BASE_URL}/simulations/${currentSessionId}/pause`, {
            method: 'PUT'
        });

        if (response.ok) {
            const data = await response.json();
            updateSimulationDisplay(data); // Update UI with state from the server
            addEcamMessage('warning', 'Simulation paused.');

            // Update button states
            document.getElementById('btn-start').disabled = false;
            document.getElementById('btn-pause').disabled = true;

            // Stop the periodic update loops
            stopPeriodicUpdates();
        } else {
             addEcamMessage('danger', `Failed to pause simulation. Server responded with status: ${response.status}`);
        }
    } catch (error) {
        addEcamMessage('danger', 'Failed to pause simulation: ' + error.message);
    }
}

async function stopSimulation() {
    // Stop any running update loops immediately
    stopPeriodicUpdates();

    // Handle demo mode first
    if (demoMode || !currentSessionId) {
        document.getElementById('simulation-status').textContent = 'STOPPED';
        document.getElementById('simulation-status').className = 'status-badge status-inactive';

        // Reset control buttons to their initial state
        document.getElementById('btn-start').disabled = true;
        document.getElementById('btn-pause').disabled = true;
        document.getElementById('btn-stop').disabled = true;
        document.getElementById('btn-new-sim').disabled = false;

        addEcamMessage('info', 'Simulation stopped.');
        demoMode = false; // Reset demo mode to allow a new API or demo session
        currentSessionId = null;
        return;
    }

    // API Mode: Send a request to the backend to stop
    try {
        const response = await fetch(`${API_BASE_URL}/simulations/${currentSessionId}/stop`, {
            method: 'PUT'
        });

        if (response.ok) {
            const data = await response.json();
            updateSimulationDisplay(data); // Update UI a final time
            addEcamMessage('info', 'Simulation stopped.');

            // Reset control buttons
            document.getElementById('btn-start').disabled = true;
            document.getElementById('btn-pause').disabled = true;
            document.getElementById('btn-stop').disabled = true;
            document.getElementById('btn-new-sim').disabled = false;

            currentSessionId = null; // Clear the session ID
        } else {
             addEcamMessage('danger', `Failed to stop simulation. Server responded with status: ${response.status}`);
        }
    } catch (error) {
        addEcamMessage('danger', 'Failed to stop simulation: ' + error.message);
    }
}


function startPeriodicUpdates() {
    if (updateInterval) clearInterval(updateInterval);
    updateInterval = setInterval(async () => {
        if (!demoMode) await generateReadings();
        if (randomEventEnabled && Date.now() - lastAlertTime > alertCooldown) maybeGenerateRandomEvent();
    }, 5000);
}

function stopPeriodicUpdates() {
    if (updateInterval) { clearInterval(updateInterval); updateInterval = null; }
}

function maybeGenerateRandomEvent() {
    if (Math.random() > 0.15) return;
    const eventTypes = [
        { weight: 30, handler: randomWeatherChange }, { weight: 25, handler: randomMinorAnomaly },
        { weight: 20, handler: randomSystemFluctuation }, { weight: 15, handler: randomTurbulence },
        { weight: 7, handler: randomMinorWarning }, { weight: 3, handler: randomMajorEvent }
    ];
    const totalWeight = eventTypes.reduce((sum, e) => sum + e.weight, 0);
    let random = Math.random() * totalWeight;
    for (const event of eventTypes) {
        if ((random -= event.weight) <= 0) { event.handler(); lastAlertTime = Date.now(); break; }
    }
}

function randomWeatherChange() { addEcamMessage('info', 'Weather conditions updated.'); }
function randomMinorAnomaly() { addEcamMessage('info', 'Instrument fluctuation detected.'); }
function randomSystemFluctuation() { addEcamMessage('info', 'Engine parameter fluctuation detected.'); }
function randomTurbulence() { addEcamMessage('info', 'Turbulence encountered.'); }
function randomMinorWarning() { addEcamMessage('warning', 'Minor system anomaly detected.'); triggerCaution(); }
function randomMajorEvent() { addEcamMessage('danger', 'Major system event detected.'); triggerDanger(); }

function triggerCaution() { warningCount++; document.getElementById('master-caution').classList.add('active'); document.getElementById('dashboard').classList.add('warning-state'); }
function triggerDanger() { dangerCount++; document.getElementById('master-warning').classList.add('active'); document.getElementById('alert-overlay').classList.remove('hidden'); document.getElementById('dashboard').classList.add('danger-state'); playAlertSound(); }

async function changeFlightPhase() {
    // Do nothing if there is no active simulation session
    if (!currentSessionId) {
        addEcamMessage('warning', 'Cannot change phase. No active simulation.');
        return;
    }

    // In demo mode, just update the display locally
    const phase = document.getElementById('flight-phase-select').value;
    if (demoMode) {
        document.getElementById('flight-phase').textContent = phase;
        addEcamMessage('info', `Flight phase changed to ${phase}`);
        return;
    }

    // API Mode: Send the new phase to the backend
    try {
        const response = await fetch(`${API_BASE_URL}/simulations/${currentSessionId}/phase?phase=${phase}`, {
            method: 'PUT'
        });

        if (response.ok) {
            const data = await response.json();
            updateSimulationDisplay(data); // Update UI with the latest state from the server
            addEcamMessage('info', `Flight phase changed to ${phase}`);

            // After changing the phase, it's good practice to get new readings immediately
            await generateReadings();
        } else {
            addEcamMessage('danger', `Failed to change flight phase. Server responded: ${response.status}`);
        }
    } catch (error) {
        addEcamMessage('danger', 'Failed to change flight phase: ' + error.message);
    }
}

async function generateReadings() {
    // Do nothing if there is no active simulation session
    if (!currentSessionId) {
        return;
    }

    // In demo mode, we don't fetch readings as the physics engine is handling it
    if (demoMode) {
        return;
    }

    const phase = document.getElementById('flight-phase-select').value;

    // API Mode: Fetch new instrument readings from the backend
    try {
        const response = await fetch(`${API_BASE_URL}/instruments/sessions/${currentSessionId}/generate?phase=${phase}`, {
            method: 'POST'
        });

        if (response.ok) {
            const instruments = await response.json();
            // Once we have the new data, update the instrument displays
            updateInstrumentDisplays(instruments);
        } else {
            console.error(`Failed to generate readings. Server responded: ${response.status}`);
        }
    } catch (error) {
        console.error('Failed to generate readings:', error);
    }
}

async function regenerateWeather() {
    addEcamMessage('info', 'Generating new weather conditions...');

    // If there's no active session, call the general weather endpoint for a demo report
    if (!currentSessionId || demoMode) {
        try {
            const response = await fetch(`${API_BASE_URL}/weather/generate`);
            if (response.ok) {
                const weather = await response.json();
                updateWeatherDisplay(weather);
                addEcamMessage('info', 'Weather conditions updated.');
            } else {
                addEcamMessage('danger', `Failed to generate weather. Server responded: ${response.status}`);
            }
        } catch (error) {
            addEcamMessage('danger', 'Failed to generate weather: ' + error.message);
        }
        return;
    }

    // API Mode: If a session is active, regenerate weather specifically for that session
    try {
        const response = await fetch(`${API_BASE_URL}/simulations/${currentSessionId}/weather/regenerate`, {
            method: 'POST'
        });

        if (response.ok) {
            const data = await response.json();
            if (data.weatherCondition) {
                updateWeatherDisplay(data.weatherCondition);
            }
            addEcamMessage('info', 'Simulation weather conditions regenerated.');
        } else {
            addEcamMessage('danger', `Failed to regenerate weather. Server responded: ${response.status}`);
        }
    } catch (error) {
        addEcamMessage('danger', 'Failed to regenerate weather: ' + error.message);
    }
}

async function runVerification() {
    if (!currentSessionId) {
        addEcamMessage('warning', 'No active simulation. Create a simulation first.');
        return;
    }

    if (demoMode) {
        addEcamMessage('info', 'Verification is not available in DEMO mode.');
        return;
    }

    addEcamMessage('info', 'Running instrument verification...');

    // API Mode: Call the verification endpoint
    try {
        const response = await fetch(`${API_BASE_URL}/instruments/sessions/${currentSessionId}/verify`);

        if (response.ok) {
            const result = await response.json();
            updateVerificationDisplay(result);

            // Trigger alerts based on the verification results
            if (result.failedCount > 0) {
                addEcamMessage('danger', `Verification FAILED: ${result.failedCount} critical instruments out of tolerance!`);
                triggerDanger();
            } else if (result.warningCount > 0) {
                addEcamMessage('warning', `Verification completed with ${result.warningCount} warnings.`);
                triggerCaution(); // Use triggerCaution for warnings
            } else {
                addEcamMessage('info', `Verification PASSED: All ${result.totalInstruments} instruments within tolerance.`);
                clearAlertState(); // Clear any previous alerts if everything passed
            }
        } else {
            addEcamMessage('danger', `Verification request failed. Server responded: ${response.status}`);
        }
    } catch (error) {
        addEcamMessage('danger', 'Failed to run verification: ' + error.message);
    }
}


function simulateFailure() {
    const failures = [{ message: 'ENGINE 2 FIRE' }, { message: 'GREEN HYD SYS FAIL' }, { message: 'CABIN PRESS FAIL' }];
    const failure = failures[Math.floor(Math.random() * failures.length)];
    addEcamMessage('danger', failure.message); triggerDanger(); showAlertModal('🚨 MASTER WARNING', failure.message);
}
function simulateWarning() {
    const warnings = [{ message: 'HYD YELLOW PRESS LOW' }, { message: 'ELEC AC BUS 2 FAULT' }, { message: 'APU AUTO SHUTDOWN' }];
    const warning = warnings[Math.floor(Math.random() * warnings.length)];
    addEcamMessage('warning', warning.message); triggerCaution();
}

function toggleRandomEvents() { randomEventEnabled = document.getElementById('random-events-toggle').checked; addEcamMessage('info', `Random events ${randomEventEnabled ? 'ENABLED' : 'DISABLED'}`); }
function refuelAircraft() {
    // Safety check: Ensure the aircraft is on the ground before refueling.
    if (!flightState.onGround) {
        addEcamMessage('warning', 'Cannot refuel while airborne.');
        return;
    }

    // Set a target fuel level (e.g., 90% of maximum capacity)
    const targetFuel = maxFuel * 0.90;

    // Check if refueling is even necessary
    if (currentFuel >= targetFuel) {
        addEcamMessage('info', 'Refueling not required. Fuel tanks are already sufficiently full.');
        return;
    }

    addEcamMessage('info', 'REFUELING IN PROGRESS...');

    // Use an interval to simulate the gradual refueling process
    const refuelInterval = setInterval(() => {
        // Stop the refueling process if the target is reached
        if (currentFuel >= targetFuel) {
            clearInterval(refuelInterval);
            currentFuel = targetFuel; // Clamp the value to the exact target
            updateFuelDisplay(currentFuel, maxFuel);
            addEcamMessage('info', 'REFUELING COMPLETE');

            // Clear any low-fuel warnings from the system display
            const fuelSystem = document.getElementById('sys-fuel');
            if (fuelSystem) {
                fuelSystem.classList.remove('warning', 'danger');
                fuelSystem.classList.add('ok');
                const statusEl = fuelSystem.querySelector('.system-status');
                if (statusEl) statusEl.textContent = 'OK';
            }
            return;
        }

        // Add a chunk of fuel. The value determines the refueling speed.
        // Let's add ~5000 kg per second for a realistic speed.
        currentFuel += 500; // 500 kg every 100ms = 5000 kg/sec

        // Ensure we don't overshoot the max capacity during the final step
        currentFuel = Math.min(currentFuel, maxFuel);

        // Update the fuel gauge and text display in real-time
        updateFuelDisplay(currentFuel, maxFuel);

    }, 100); // Run the update every 100 milliseconds for a smooth effect
}

function updateSimulationDisplay(data) {
    document.getElementById('callsign').textContent = data.aircraftCallsign || 'A380-SIM';
    document.getElementById('flight-phase').textContent = data.currentFlightPhase || 'PREFLIGHT';
    const statusEl = document.getElementById('simulation-status');
    if (data.isActive) { statusEl.textContent = data.isPaused ? 'PAUSED' : 'ACTIVE'; statusEl.className = `status-badge status-${data.isPaused ? 'paused' : 'active'}`; }
    else { statusEl.textContent = 'INACTIVE'; statusEl.className = 'status-badge status-inactive'; }
    if (shouldWritePfdFromBackend()) {
        if (data.currentAltitudeFeet !== undefined) flightState.altitude = data.currentAltitudeFeet;
        if (data.currentAirspeedKnots !== undefined) flightState.airspeed = data.currentAirspeedKnots;
    }
}

function updateInstrumentDisplays(instruments) {
    instruments.forEach(inst => {
        let elementId = '';
        switch (inst.instrumentType) {
            case 'AIRSPEED_INDICATOR': elementId = 'airspeed'; break;
            case 'ALTIMETER': elementId = 'altitude'; break;
            case 'VERTICAL_SPEED_INDICATOR': elementId = 'vsi'; break;
            case 'HEADING_INDICATOR': elementId = 'heading'; break;
            case 'MACH_INDICATOR': elementId = 'mach'; break;
            case 'GROUND_SPEED': elementId = 'gs'; break;
        }
        if (elementId) {
            if (shouldWritePfdFromBackend()) {
                flightState[elementId] = inst.currentValue || 0;
            }
            applyInstrumentVerificationState(elementId, inst.verificationStatus);
        }
    });
}

// NEWLY ADDED: This function was missing, causing a ReferenceError.
function applyInstrumentVerificationState(elementId, status) {
    const indicator = document.getElementById(`${elementId}-indicator`);
    if (!indicator) return;
    indicator.classList.remove('warning', 'danger', 'ok');
    switch (status) {
        case 'FAILED': indicator.classList.add('danger'); break;
        case 'WARNING': indicator.classList.add('warning'); break;
        case 'PASSED': indicator.classList.add('ok'); break;
    }
}

function updateEngineN1(engineNum, value) {
    const barEl = document.getElementById(`eng${engineNum}-n1-bar`);
    if(barEl) barEl.style.width = `${Math.min(value, 100)}%`;
    const valueEl = document.getElementById(`eng${engineNum}-n1`);
    if(valueEl) valueEl.textContent = `${value.toFixed(1)}%`;
}
function updateEngineEgt(engineNum, value) {
    const barEl = document.getElementById(`eng${engineNum}-egt-bar`);
    if(barEl) barEl.style.width = `${Math.min((value / 1000) * 100, 100)}%`;
    const valueEl = document.getElementById(`eng${engineNum}-egt`);
    if(valueEl) valueEl.textContent = `${Math.round(value)}°C`;
}
function updateEngineFuelFlow(engineNum, value) {
    const valueEl = document.getElementById(`eng${engineNum}-ff`);
    if(valueEl) valueEl.textContent = `${Math.round(value)} kg/h`;
}
function updateFuelDisplay(current, max) {
    const percentage = (current / max) * 100;
    const barEl = document.getElementById('fuel-bar');
    if(barEl) barEl.style.width = `${percentage}%`;
    const qtyEl = document.getElementById('fuel-qty');
    if(qtyEl) qtyEl.textContent = `${Math.round(current).toLocaleString()} kg`;
    const percentEl = document.getElementById('fuel-percent');
    if(percentEl) percentEl.textContent = `${percentage.toFixed(1)}%`;
}
function updateFuelConsumption() {
    // Only consume fuel if the simulation is actively running
    const statusEl = document.getElementById('simulation-status');
    if (!statusEl || statusEl.textContent !== 'ACTIVE') return;

    // Get the current flight phase to determine burn rate
    const phase = document.getElementById('flight-phase-select')?.value || 'PREFLIGHT';

    // Define fuel burn rates in kg per update cycle (every 5 seconds in this setup)
    const burnRates = {
        'PREFLIGHT': 0,
        'ENGINE_START': 50,
        'TAXI': 100,
        'TAKEOFF': 500,
        'CLIMB': 400,
        'CRUISE': 250,
        'DESCENT': 150,
        'APPROACH': 200,
        'LANDING': 150
    };

    fuelBurnRate = burnRates[phase] || 0;

    // Only proceed if there is fuel to burn and the engines are running
    if (fuelBurnRate > 0 && currentFuel > 0 && countRunningEngines() > 0) {
        currentFuel = Math.max(0, currentFuel - fuelBurnRate);
        updateFuelDisplay(currentFuel, maxFuel);

        const percentage = (currentFuel / maxFuel) * 100;

        // --- Logic to trigger alerts only ONCE when crossing a threshold ---

        // Critical Fuel Alert
        if (percentage <= 5 && lastFuelAlertLevel !== 'critical') {
            lastFuelAlertLevel = 'critical';
            addEcamMessage('danger', 'FUEL CRITICAL - LAND IMMEDIATELY');
            triggerDanger();
            updateSystemItem('sys-fuel', 'CRITICAL', 'danger');
        }
        // Low Fuel Warning
        else if (percentage <= 15 && lastFuelAlertLevel !== 'low' && lastFuelAlertLevel !== 'critical') {
            lastFuelAlertLevel = 'low';
            addEcamMessage('warning', 'LOW FUEL - Divert recommended');
            triggerCaution();
            updateSystemItem('sys-fuel', 'LOW', 'warning');
        }
        // Informational Message
        else if (percentage <= 25 && lastFuelAlertLevel === 'normal') {
            lastFuelAlertLevel = 'info';
            addEcamMessage('info', 'Fuel below 25% - Monitor consumption');
        }
    }
}

function updateWeatherDisplay(weather) {
    // Gracefully exit if no weather data is provided
    if (!weather) return;

    // --- Update Primary Values ---
    const windDir = weather.windDirectionDegrees || 0;
    const windSpd = weather.windSpeedKnots || 0;
    document.getElementById('wind-value').textContent = `${Math.round(windDir)}°/${Math.round(windSpd)} KTS`;
    document.getElementById('headwind').textContent = Math.round(weather.headWindComponent || 0);
    document.getElementById('crosswind').textContent = Math.round(weather.crosswindComponent || 0);

    const visibility = weather.visibilityStatuteMiles || 10;
    document.getElementById('visibility-value').textContent = `${visibility.toFixed(1)} SM`;

    const oat = weather.outsideAirTempCelsius || 15;
    document.getElementById('oat-value').textContent = `${oat.toFixed(1)}°C`;

    const qnh = weather.barometricPressureHPa || 1013;
    document.getElementById('qnh-value').textContent = `${qnh.toFixed(0)} hPa`;

    // --- Set Visual Warnings Based on Thresholds ---
    const windDisplay = document.getElementById('wind-display');
    if (windDisplay) {
        windDisplay.classList.remove('danger', 'warning');
        if (windSpd > 35) windDisplay.classList.add('danger');
        else if (windSpd > 25) windDisplay.classList.add('warning');
    }

    const visDisplay = document.getElementById('visibility-display');
    if (visDisplay) {
        visDisplay.classList.remove('danger', 'warning');
        if (visibility < 1) visDisplay.classList.add('danger');
        else if (visibility < 3) visDisplay.classList.add('warning');
    }

    // --- Handle Specific Weather Phenomena ---
    if (weather.windShearPresent) {
        addWeatherAlert('danger', 'WIND SHEAR DETECTED');
        triggerDanger();
    }
    if (weather.microburstDetected) {
        addWeatherAlert('danger', 'MICROBURST WARNING');
        triggerDanger();
        showAlertModal('WINDSHEAR WARNING', 'MICROBURST DETECTED - GO AROUND RECOMMENDED');
    }
    if (weather.thunderstormActivity) {
        addWeatherAlert('danger', 'THUNDERSTORM ACTIVITY');
        triggerCaution();
    }
    if (weather.icingConditions) {
        addWeatherAlert('warning', `Icing conditions: ${weather.icingType || 'Unknown'}`);
    }

    // Update the turbulence indicator display
    updateTurbulenceDisplay(weather.turbulenceLevel);

    function updateTurbulenceDisplay(level) {
        // 1. Reset all turbulence levels (remove 'active' class)
        document.querySelectorAll('.turbulence-level').forEach(el => {
            el.classList.remove('active');
        });

        // 2. Default to 'NONE' if no level is provided
        if (!level) level = 'NONE';

        // 3. Find the specific element for this level (e.g., data-level="light")
        const targetElement = document.querySelector(`.turbulence-level[data-level="${level.toLowerCase()}"]`);

        // 4. Highlight it
        if (targetElement) {
            targetElement.classList.add('active');
        }

        // 5. Trigger alerts for dangerous levels
        if (level === 'SEVERE' || level === 'EXTREME') {
            addEcamMessage('warning', `${level} TURBULENCE ENCOUNTERED`);
            if (typeof triggerCaution === 'function') triggerCaution();
        }
    }
}
function updateVerificationDisplay(result) {
    // Update the numerical counts for each status
    document.getElementById('verify-passed').textContent = result.passedCount || 0;
    document.getElementById('verify-warning').textContent = result.warningCount || 0;
    document.getElementById('verify-failed').textContent = result.failedCount || 0;

    // Calculate and display the pass rate percentage
    const passRate = result.passRate || 0;
    document.getElementById('verify-percent').textContent = `${passRate.toFixed(1)}%`;

    // Update the progress bar width
    const progressFill = document.getElementById('verify-progress');
    if (progressFill) {
        progressFill.style.width = `${passRate}%`;

        // Color the progress bar based on the severity of the results
        if (result.failedCount > 0) {
            // Red if there are any failures
            progressFill.style.background = 'var(--danger-color)';
        } else if (result.warningCount > 0) {
            // Yellow if there are warnings but no failures
            progressFill.style.background = 'var(--warning-color)';
        } else {
            // Green if everything passed
            progressFill.style.background = 'linear-gradient(90deg, var(--success-color), var(--accent-cyan))';
        }
    }
}

function updateSystemItem(id, status, state) {
    const el = document.getElementById(id); if (!el) return;
    const statusEl = el.querySelector('.system-status');
    if (statusEl) statusEl.textContent = status;
    el.classList.remove('ok', 'warning', 'danger');
    if (state) el.classList.add(state);
}

function clearAlerts() {
    warningCount = 0; dangerCount = 0; clearAlertState();
    document.querySelectorAll('.instrument, .engine, .system-item').forEach(el => el.classList.remove('danger', 'warning'));
    addEcamMessage('info', 'All alerts cleared.');
}

function clearAlertState() {
    document.getElementById('master-warning').classList.remove('active');
    document.getElementById('master-caution').classList.remove('active');
    document.getElementById('alert-overlay').classList.add('hidden');
    document.getElementById('dashboard').classList.remove('warning-state', 'danger-state');
}

function playAlertSound() {
    // Use a try...catch block to prevent errors in browsers that don't support the Web Audio API.
    try {
        // Create an AudioContext. 'webkitAudioContext' is for older Safari compatibility.
        const audioContext = new (window.AudioContext || window.webkitAudioContext)();

        // Create an OscillatorNode - this is what generates the sound wave.
        const oscillator = audioContext.createOscillator();

        // Create a GainNode - this controls the volume.
        const gainNode = audioContext.createGain();

        // Connect the nodes in a chain:
        // Oscillator -> Gain Node (volume) -> Destination (speakers)
        oscillator.connect(gainNode);
        gainNode.connect(audioContext.destination);

        // --- Configure the sound's properties ---

        // Set the frequency of the tone in Hertz (Hz). 800Hz is a noticeable, piercing tone.
        oscillator.frequency.value = 800;

        // Set the type of wave. 'square' produces a harsh, digital, beep-like sound,
        // which is perfect for an alert. Other options include 'sine', 'sawtooth', 'triangle'.
        oscillator.type = 'square';

        // Set the volume. The gain value is a multiplier. 1.0 is full volume, which is very loud.
        // A low value like 0.1 is usually sufficient for a clear alert.
        gainNode.gain.value = 0.1;

        // --- Play the sound for a short duration ---

        // Start the sound now.
        oscillator.start();

        // Stop the sound after 200 milliseconds (0.2 seconds) to create a short "beep".
        setTimeout(() => {
            oscillator.stop();
        }, 200);

    } catch (e) {
        // If any part of the Audio API fails (e.g., not supported), log a message
        // to the console instead of crashing the application.
        console.warn('Web Audio API is not supported in this browser. Sound alert was not played.');
    }
}

function showAlertModal(title, message) { document.getElementById('modal-title').textContent = title; document.getElementById('modal-body').innerHTML = `<p>${message}</p>`; document.getElementById('alert-modal').classList.add('active'); }
function closeModal() { document.getElementById('alert-modal').classList.remove('active'); }
function acknowledgeAlert() { closeModal(); clearAlertState(); addEcamMessage('info', 'Alert acknowledged by crew.'); }

function addEcamMessage(type, text) {
    const messagesEl = document.getElementById('ecam-messages');
    if (!messagesEl) return;

    const now = new Date();
    const timeStr = now.toTimeString().substring(0, 8);

    const messageEl = document.createElement('div');
    messageEl.className = `ecam-message ${type}`;
    messageEl.innerHTML = `
        <span class="ecam-time">${timeStr}</span>
        <span class="ecam-text">${text}</span>
    `;

    // Add the new message to the top of the list
    messagesEl.insertBefore(messageEl, messagesEl.firstChild);

    // Keep only the last 50 messages to prevent the list from getting too long
    while (messagesEl.children.length > 50) {
        messagesEl.removeChild(messagesEl.lastChild);
    }

    // Automatically scroll the panel to the top to see the newest message
    messagesEl.scrollTop = 0;
}
function updateFlightPhysics() {
    const isActive = document.getElementById('simulation-status')?.textContent === 'ACTIVE';
    if (!isActive) return;

    flightState.weight = PHYSICS.operatingEmptyWeight + currentFuel;
    let totalThrust = 0;
    Object.values(flightState.engines).forEach(eng => {
        if(eng.running) totalThrust += (eng.n1 / 100) * PHYSICS.thrustPerEngine;
    });

    const configDrag = getConfigurationDragPenalty(); // Move this out so it's available for both

    if (!flightState.onGround) {
        // --- 1. AIRSPEED CALCULATION (Missing in your snippet) ---
        const thrustFactor = totalThrust / PHYSICS.maxThrust;
        // Simple drag formula: Target speed reduces as drag increases
        const targetSpeed = (thrustFactor * 350) / (1.0 + configDrag);
        // Smoothly accelerate/decelerate towards target speed
        flightState.airspeed += (targetSpeed - flightState.airspeed) * 0.02;

        // --- 2. VERTICAL SPEED & ALTITUDE ---
        const thrustWeightRatio = totalThrust / (flightState.weight * PHYSICS.gravity);
        const excessRatio = thrustWeightRatio - (PHYSICS.climbBaselineRatio + configDrag);
        let vsFromThrust = excessRatio * (excessRatio > 0 ? PHYSICS.climbGain : PHYSICS.descentGain) * getLiftMultiplier();

        if (countRunningEngines() === 0) vsFromThrust = -2200;

        let vsFromPitch = flightState.pitch * PHYSICS.pitchToVsFactor;
        flightState.verticalSpeed = vsFromThrust + vsFromPitch;

        if (flightState.autopilot.engaged) {
            // Simple P-controller for Pitch to maintain target VS
            flightState.pitch += (flightState.autopilot.vs - flightState.verticalSpeed) * 0.0005;
            // Clamp pitch to realistic values
            flightState.pitch = Math.max(-15, Math.min(15, flightState.pitch));
        }

        flightState.altitude += flightState.verticalSpeed / 60;

        // Landing Logic
        if (flightState.altitude <= 0) {
            flightState.altitude = 0;
            flightState.onGround = true;
            flightState.verticalSpeed = 0;
            flightState.pitch = 0;
            addEcamMessage('info', 'TOUCHDOWN');
        }
    } else {
        // --- ON GROUND LOGIC ---
        if (flightState.throttle > 50 && !flightState.parkingBrake) {
            // Acceleration on ground
            flightState.airspeed = Math.min(flightState.airspeed + 2, 180); // +2 per tick acceleration

            // Takeoff Logic
            if (flightState.airspeed >= 150) {
                flightState.onGround = false;
                flightState.pitch = 10;
                flightState.altitude = 5; // <--- The Fix
                addEcamMessage('info', 'V1 - ROTATE');
            }
        } else {
            // Deceleration/Braking
            flightState.airspeed = Math.max(0, flightState.airspeed - 5);
        }
    }

    // Update derived physics values
    flightState.mach = flightState.airspeed / 661;
    flightState.groundSpeed = flightState.airspeed; // Simplified

    updateFlightDisplays();
}
function updateFlightDisplays() {
    document.getElementById('airspeed-value').textContent = Math.round(flightState.airspeed);
    document.getElementById('altitude-value').textContent = Math.round(flightState.altitude).toLocaleString();
    document.getElementById('vsi-value').textContent = Math.round(flightState.verticalSpeed);
    document.getElementById('heading-value').textContent = Math.round(flightState.heading);
    document.getElementById('mach-value').textContent = (flightState.airspeed / 661).toFixed(2);
    document.getElementById('gs-value').textContent = Math.round(flightState.airspeed);
    document.getElementById('pitch-value').textContent = `${flightState.pitch.toFixed(1)}°`;
    updatePfdAttitudeIndicator();
}

function enableControls() { document.getElementById('btn-start').disabled = false; document.getElementById('btn-pause').disabled = true; document.getElementById('btn-stop').disabled = true; }
function resetControls() { document.getElementById('btn-new-sim').disabled = false; document.getElementById('btn-start').disabled = true; document.getElementById('btn-pause').disabled = true; document.getElementById('btn-stop').disabled = true; }

function updateThrottle(value) {
    flightState.throttle = parseInt(value);
    document.getElementById('throttle-value').textContent = `${value}%`;
    for (let i = 1; i <= 4; i++) applyThrottleToEngine(i, flightState.throttle);
}
function updateFlaps(position) { flightState.flaps = parseInt(position); addEcamMessage('info', `FLAPS set to ${position}`); }
function toggleGear() {
    const gearBtn = document.getElementById('gear-btn');
    if (!gearBtn) return; // Exit if the button doesn't exist

    const maxGearRetractionSpeed = 280;
    const maxGearExtensionSpeed = 250;

    // --- Logic for RETRACTING the gear ---
    if (gearState === 'down') {
        // Safety Checks: Cannot retract on the ground or if flying too fast.
        if (flightState.onGround) {
            addEcamMessage('info', 'GEAR - Cannot retract on ground');
            return;
        }
        if (flightState.airspeed > maxGearRetractionSpeed) {
            addEcamMessage('danger', `OVERSPEED - Reduce speed below ${maxGearRetractionSpeed} KTS to retract gear`);
            triggerCaution();
            return;
        }

        // Start the retraction process
        gearState = 'transit';
        flightState.gear = 'transit';
        gearBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> TRANSIT';
        gearBtn.classList.add('transit');
        updateSystemItem('sys-gear', 'TRANSIT', 'warning');
        addEcamMessage('info', 'GEAR RETRACTING');

        // Simulate the 3-second transition time
        setTimeout(() => {
            gearState = 'up';
            flightState.gear = 'up';
            gearBtn.innerHTML = '<i class="fas fa-circle-notch"></i> UP';
            gearBtn.classList.remove('transit');
            updateSystemItem('sys-gear', 'UP', 'ok');
            addEcamMessage('info', 'GEAR UP AND LOCKED');
        }, 3000); // 3 seconds
    }
    // --- Logic for EXTENDING the gear ---
    else if (gearState === 'up') {
        // Safety Check: Cannot extend if flying too fast.
        if (flightState.airspeed > maxGearExtensionSpeed) {
            addEcamMessage('danger', `OVERSPEED - Reduce speed below ${maxGearExtensionSpeed} KTS to extend gear`);
            triggerCaution();
            return;
        }

        // Start the extension process
        gearState = 'transit';
        flightState.gear = 'transit';
        gearBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> TRANSIT';
        gearBtn.classList.add('transit');
        updateSystemItem('sys-gear', 'TRANSIT', 'warning');
        addEcamMessage('info', 'GEAR EXTENDING');

        // Simulate the 3-second transition time
        setTimeout(() => {
            gearState = 'down';
            flightState.gear = 'down';
            gearBtn.innerHTML = '<i class="fas fa-circle"></i> DOWN';
            gearBtn.classList.remove('transit');
            updateSystemItem('sys-gear', 'DOWN', 'ok');
            addEcamMessage('info', 'GEAR DOWN AND LOCKED');
            playGearSound(); // Play a sound effect on successful extension
        }, 3000); // 3 seconds
    }
    // Note: No action is taken if the gear is already in 'transit'.
}

function toggleSpoilers() {
    const spoilerBtn = document.getElementById('spoiler-btn');
    if (!spoilerBtn) return; // Exit if the button doesn't exist

    // Define the sequence of states
    const states = {
        ret: 'armed',
        armed: 'deployed',
        deployed: 'ret'
    };

    // Define the user-friendly text for each state
    const messages = {
        armed: 'ARMED',
        deployed: 'DEPLOYED',
        ret: 'RET'
    };

    // Cycle to the next state
    spoilerState = states[spoilerState];
    flightState.spoilers = spoilerState;

    // Update the button's text content
    spoilerBtn.textContent = messages[spoilerState];

    // Update the button's CSS classes for visual styling
    spoilerBtn.className = 'spoiler-btn'; // Reset classes first
    if (spoilerState !== 'ret') {
        spoilerBtn.classList.add(spoilerState); // Add 'armed' or 'deployed' class
    }

    // Add a corresponding message to the ECAM
    addEcamMessage('info', `SPEED BRAKE ${messages[spoilerState]}`);
}

function adjustHeading(delta) { flightState.heading = (flightState.heading + delta + 360) % 360; document.getElementById('hdg-select').textContent = `${Math.round(flightState.heading)}°`; }
function adjustPitch(delta) { if (!flightState.autopilot.engaged) flightState.pitch = Math.max(-15, Math.min(15, flightState.pitch + delta)); }
function toggleParkingBrake() { flightState.parkingBrake = !flightState.parkingBrake; document.getElementById('park-brake-btn').classList.toggle('active', flightState.parkingBrake); document.getElementById('park-brake-btn').textContent = flightState.parkingBrake ? 'SET' : 'OFF'; }
function toggleAutopilot() {
    flightState.autopilot.engaged = !flightState.autopilot.engaged;
    document.getElementById('ap-btn').classList.toggle('active', flightState.autopilot.engaged);
    document.getElementById('ap-btn').textContent = flightState.autopilot.engaged ? 'ON' : 'OFF';
    if(flightState.autopilot.engaged) { flightState.autopilot.vs = flightState.verticalSpeed; document.getElementById('ap-vs-value').textContent = Math.round(flightState.autopilot.vs); }
}
function adjustAutopilotVS(delta) { if (flightState.autopilot.engaged) {
flightState.autopilot.vs += delta;
document.getElementById('ap-vs-value').textContent = Math.round(flightState.autopilot.vs);
} }
function toggleAutothrottle() { flightState.autothrottle = !flightState.autothrottle; document.getElementById('athr-btn').classList.toggle('active', flightState.autothrottle); document.getElementById('athr-btn').textContent = flightState.autothrottle ? 'ON' : 'OFF'; }

function updatePfdAttitudeIndicator() {
    const horizon = document.getElementById('pfd-horizon');
    if (horizon) horizon.style.transform = `translateY(${flightState.pitch * 4}px) rotate(${flightState.bank}deg)`;
}

function initializeNavDisplay() {
    navDisplay.canvas = document.getElementById('nav-display-canvas');
    if (navDisplay.canvas) navDisplay.ctx = navDisplay.canvas.getContext('2d');
    generateNavData();
}

function setNavDisplayMode(mode) {
    navDisplay.mode = mode;
    document.querySelectorAll('.nd-mode-btn').forEach(btn => btn.classList.remove('active'));
    document.getElementById(`nd-mode-${mode.toLowerCase()}`).classList.add('active');
}

/**
 * Creates dummy data for the weather and terrain radar displays.
 * This is called once during initialization to have something to show.
 */
function generateNavData() {
    // Generate random weather cells (for WXR mode)
    navDisplay.weatherData = Array.from({ length: 15 }, () => ({
        x: (Math.random() - 0.5) * 250, // Horizontal position relative to aircraft
        y: Math.random() * -180 - 20,    // Vertical position (always ahead)
        radius: 15 + Math.random() * 25, // Size of the weather cell
        intensity: Math.random()         // Determines color (0-1)
    }));

    // Generate random terrain data points (for TERR mode)
    navDisplay.terrainData = Array.from({ length: 50 }, () => ({
        x: (Math.random() - 0.5) * 300,
        y: Math.random() * -200,
        // Elevation relative to current aircraft altitude (e.g., -1500 means 1500ft below)
        elevation: (Math.random() * -5000)
    }));
}

/**
 * Main rendering function for the Navigation Display canvas.
 * This is called repeatedly in the mainUpdateLoop.
 */
function drawNavDisplay() {
    // Exit if the canvas context isn't available
    if (!navDisplay.ctx) return;

    const { ctx, canvas } = navDisplay;
    const { width: w, height: h } = canvas;

    // Aircraft is centered horizontally and near the bottom vertically
    const centerX = w / 2;
    const centerY = h - 30;

    // 1. Clear the canvas with a black background
    ctx.fillStyle = '#000';
    ctx.fillRect(0, 0, w, h);

    // 2. Draw basic display elements (like range rings and aircraft symbol)
    ctx.strokeStyle = '#2a4'; // Green for rings
    ctx.lineWidth = 1;
    ctx.beginPath();
    ctx.arc(centerX, centerY, 80, Math.PI, 2 * Math.PI); // Inner ring
    ctx.stroke();
    ctx.beginPath();
    ctx.arc(centerX, centerY, 160, Math.PI, 2 * Math.PI); // Outer ring
    ctx.stroke();

    // Draw aircraft symbol (a simple white chevron)
    ctx.fillStyle = '#fff';
    ctx.beginPath();
    ctx.moveTo(centerX, centerY);
    ctx.lineTo(centerX - 10, centerY + 15);
    ctx.lineTo(centerX, centerY + 10);
    ctx.lineTo(centerX + 10, centerY + 15);
    ctx.closePath();
    ctx.fill();

    // 3. Call the appropriate drawing function based on the selected mode
    if (navDisplay.mode === 'TCAS') {
        drawTcasTargets(ctx, centerX, centerY);
    } else if (navDisplay.mode === 'WXR') {
        drawWeatherRadar(ctx, centerX, centerY);
    } else if (navDisplay.mode === 'TERR') {
        drawTerrainRadar(ctx, centerX, centerY);
    }
}

/**
 * Helper to draw TCAS targets on the ND.
 * @param {CanvasRenderingContext2D} ctx The canvas context.
 * @param {number} centerX The aircraft's X position on the canvas.
 * @param {number} centerY The aircraft's Y position on the canvas.
 */
function drawTcasTargets(ctx, centerX, centerY) {
    // --- FIX START ---
    // Convert the aircraft's current heading to radians for the math functions.
    // We use the NEGATIVE heading because if the aircraft turns right (clockwise),
    // the world on the display must appear to rotate left (counter-clockwise).
    const aircraftHeadingRad = -flightState.heading * (Math.PI / 180);
    const cosH = Math.cos(aircraftHeadingRad);
    const sinH = Math.sin(aircraftHeadingRad);
    // --- FIX END ---

    tcasState.targets.forEach(target => {
        // --- FIX START ---
        // Rotate the target's "North-up" coordinates to be relative to the aircraft's nose.
        const rotatedX = target.x * cosH - target.y * sinH;
        const rotatedY = target.x * sinH + target.y * cosH;

        // Calculate the final screen position using the NEW rotated coordinates.
        const x = centerX + rotatedX;
        const y = centerY + rotatedY;
        // --- FIX END ---

        const color = target.threat === 'TA' ? '#f9a825' : '#fff';

        // Draw the target symbol (a diamond shape)
        ctx.strokeStyle = color;
        ctx.lineWidth = 2;
        ctx.beginPath();
        ctx.moveTo(x, y - 6);
        ctx.lineTo(x + 6, y);
        ctx.lineTo(x, y + 6);
        ctx.lineTo(x - 6, y);
        ctx.closePath();
        ctx.stroke();

        // Draw the relative altitude text
        ctx.fillStyle = color;
        ctx.font = '11px "Courier New"';
        ctx.textAlign = 'center';
        const altText = `${target.relativeAltitude >= 0 ? '+' : ''}${Math.round(target.relativeAltitude / 100).toString().padStart(2, '0')}`;
        ctx.fillText(altText, x, y + 18);
    });
}

/**
 * Helper to draw weather radar returns on the ND.
 */
function drawWeatherRadar(ctx, centerX, centerY) {
    // --- FIX START ---
    const aircraftHeadingRad = -flightState.heading * (Math.PI / 180);
    const cosH = Math.cos(aircraftHeadingRad);
    const sinH = Math.sin(aircraftHeadingRad);
    // --- FIX END ---

    navDisplay.weatherData.forEach(cell => {
        // --- FIX START ---
        // Rotate the weather cell's coordinates
        const rotatedX = cell.x * cosH - cell.y * sinH;
        const rotatedY = cell.x * sinH + cell.y * cosH;

        const x = centerX + rotatedX;
        const y = centerY + rotatedY;
        // --- FIX END ---

        const gradient = ctx.createRadialGradient(x, y, 0, x, y, cell.radius);
        const color = cell.intensity > 0.75 ? '255, 0, 0' : cell.intensity > 0.4 ? '255, 255, 0' : '0, 255, 0';
        gradient.addColorStop(0, `rgba(${color}, 0.6)`);
        gradient.addColorStop(1, `rgba(${color}, 0)`);
        ctx.fillStyle = gradient;
        ctx.fillRect(x - cell.radius, y - cell.radius, cell.radius * 2, cell.radius * 2);
    });
}

/**
 * Helper to draw terrain data on the ND.
 */
function drawTerrainRadar(ctx, centerX, centerY) {
    // --- FIX START ---
    const aircraftHeadingRad = -flightState.heading * (Math.PI / 180);
    const cosH = Math.cos(aircraftHeadingRad);
    const sinH = Math.sin(aircraftHeadingRad);
    // --- FIX END ---

    navDisplay.terrainData.forEach(point => {
        // --- FIX START ---
        // Rotate the terrain point's coordinates
        const rotatedX = point.x * cosH - point.y * sinH;
        const rotatedY = point.x * sinH + point.y * cosH;

        const x = centerX + rotatedX;
        const y = centerY + rotatedY;
        // --- FIX END ---

        const altDifference = -point.elevation;
        let color = 'rgba(0, 255, 0, 0.7)'; // Green
        if (altDifference < 500) color = 'rgba(255, 0, 0, 0.7)'; // Red
        else if (altDifference < 2000) color = 'rgba(255, 255, 0, 0.7)'; // Yellow

        ctx.fillStyle = color;
        ctx.fillRect(x - 2, y - 2, 4, 4);
    });
}


/**
 * Updates the state of the TCAS system (spawning, moving, and assessing threats).
 * This is a logic function, not a drawing function.
 */
function updateTcasSystem() {
    const now = Date.now();

    // 1. Spawn new traffic periodically if we are below the max target count
    if (now - tcasState.lastSpawnTime > tcasState.spawnInterval && tcasState.targets.length < 5) {
        tcasState.targets.push({
            x: (Math.random() - 0.5) * 200, // Random horizontal position
            y: -180 - Math.random() * 50,   // Spawn ahead, off-screen
            relativeAltitude: (Math.random() - 0.5) * 4000, // Random altitude +/- 4000ft
            speed: 0.8 + Math.random() * 0.5, // How fast it moves on screen
            threat: 'OTHER' // Initial threat level
        });
        tcasState.lastSpawnTime = now;
    }

    // 2. Update existing targets (move them, assess threats, and remove old ones)
    tcasState.targets = tcasState.targets.filter(target => {
        // Move target closer to the aircraft
        target.y += target.speed;

        // Threat Assessment Logic
        const distance = Math.sqrt(target.x**2 + target.y**2);
        if (distance < 60 && Math.abs(target.relativeAltitude) < 800) {
            // If threat level is escalating from OTHER to TA, trigger an alert
            if (target.threat !== 'TA') {
                target.threat = 'TA'; // Escalate to Traffic Advisory
                addEcamMessage('warning', 'TCAS TRAFFIC, TRAFFIC');
                triggerCaution();
            }
        } else {
            // Downgrade threat if target is no longer a factor
            target.threat = 'OTHER';
        }

        // Keep the target only if it's still on the display (ahead of the aircraft)
        return target.y <= 20;
    });
}

// Make critical functions available to be called from HTML (e.g., onclick)
Object.assign(window, {
    createSimulation, startSimulation, pauseSimulation, stopSimulation, changeFlightPhase, generateReadings,
    regenerateWeather, runVerification, simulateFailure, simulateWarning, toggleRandomEvents, refuelAircraft,
    clearAlerts, updateThrottle, adjustHeading, updateFlaps, toggleGear, toggleSpoilers, toggleParkingBrake,
    toggleAutopilot, toggleAutothrottle, closeModal, acknowledgeAlert, testInstrument, shutdownEngine, startEngine,
    showInstrumentDetails, showEngineDetails, adjustPitch, setNavDisplayMode, adjustAutopilotVS
});