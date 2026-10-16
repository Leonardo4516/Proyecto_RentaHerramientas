import * as THREE from 'three';
import { OrbitControls } from 'three/addons/controls/OrbitControls.js';

/**
 * RentaTools PRO - Escenario 3D Interactivo de Retroexcavadora (Three.js)
 * Modelo paramétrico de alta fidelidad, con articulación cinemática completa,
 * materiales PBR corporativos Sunbelt Rentals (Amarillo #FFC72C, Verde #00843D, Gris #1E293B)
 * y controles orbitales ergonómicos.
 */

let scene, camera, renderer, controls;
let container = null;
let animFrameId = null;
let clock = null;
let isPaused = false;
let userInteracting = false;
let userInteractionTimeout = null;

// Grupos jerárquicos para articulación cinemática
let excavatorRoot = null;
let upperstructure = null;
let boomGroup = null;
let stickGroup = null;
let bucketGroup = null;

// Materiales compartidos
let matYellow, matYellowDark, matDarkSteel, matChrome, matGlass, matTracks, matGreenAccent, matDetails;

function initHero3D() {
  container = document.getElementById('hero-3d-canvas');
  if (!container) return;

  // Comprobar soporte WebGL
  try {
    const canvasTest = document.createElement('canvas');
    const gl = canvasTest.getContext('webgl2') || canvasTest.getContext('webgl');
    if (!gl) throw new Error('WebGL no disponible');
  } catch (e) {
    console.warn('WebGL no soportado, mostrando respaldo visual:', e);
    showFallback();
    return;
  }

  clock = new THREE.Clock();

  // 1. Escena
  scene = new THREE.Scene();
  scene.background = null;

  // 2. Cámara
  const w = container.clientWidth || 600;
  const h = container.clientHeight || 500;
  const aspect = w / h;
  camera = new THREE.PerspectiveCamera(36, aspect, 0.1, 100);
  updateCameraDistance();

  // 3. Renderer
  renderer = new THREE.WebGLRenderer({
    alpha: true,
    antialias: true,
    powerPreference: 'high-performance'
  });
  renderer.setPixelRatio(Math.min(window.devicePixelRatio || 1, 2));
  renderer.setSize(w, h);
  renderer.shadowMap.enabled = true;
  renderer.shadowMap.type = THREE.PCFSoftShadowMap;
  renderer.toneMapping = THREE.ACESFilmicToneMapping;
  renderer.toneMappingExposure = 1.15;
  renderer.outputColorSpace = THREE.SRGBColorSpace;

  const canvas = renderer.domElement;
  canvas.style.width = '100%';
  canvas.style.height = '100%';
  canvas.style.display = 'block';
  canvas.style.touchAction = 'pan-y'; // Permite scroll táctil natural en móviles
  container.innerHTML = '';
  container.appendChild(canvas);

  // 4. Controles Orbitales
  controls = new OrbitControls(camera, canvas);
  controls.enableDamping = true;
  controls.dampingFactor = 0.05;
  controls.enableZoom = false; // Sin zoom con rueda para no interferir con scroll
  controls.enablePan = false;
  controls.minPolarAngle = Math.PI * 0.15;
  controls.maxPolarAngle = Math.PI * 0.48; // Nunca mirar desde debajo del piso
  controls.autoRotate = true;
  controls.autoRotateSpeed = 0.75;
  controls.target.set(0, 1.1, 0.3);

  controls.addEventListener('start', () => {
    userInteracting = true;
    controls.autoRotate = false;
    if (userInteractionTimeout) clearTimeout(userInteractionTimeout);
  });

  controls.addEventListener('end', () => {
    userInteracting = false;
    userInteractionTimeout = setTimeout(() => {
      if (!userInteracting) controls.autoRotate = true;
    }, 2500);
  });

  // 5. Iluminación de Estudio Industrial
  setupLighting();

  // 6. Materiales PBR
  setupMaterials();

  // 7. Construcción de la Retroexcavadora
  buildExcavator();

  // 8. Sombra de Contacto y Plataforma en el Piso
  setupGroundShadow();

  // 9. Observador de Redimensionamiento
  const resizeObserver = new ResizeObserver(() => {
    handleResize();
  });
  resizeObserver.observe(container);
  window.addEventListener('resize', handleResize);

  // 10. Observador de Visibilidad (Ahorro de batería/GPU)
  if ('IntersectionObserver' in window) {
    const visibilityObserver = new IntersectionObserver((entries) => {
      entries.forEach(entry => {
        if (entry.isIntersecting) {
          resumeLoop();
        } else {
          pauseLoop();
        }
      });
    }, { threshold: 0.05 });
    visibilityObserver.observe(container);
  }

  document.addEventListener('visibilitychange', () => {
    if (document.hidden) pauseLoop();
    else resumeLoop();
  });

  // Marcar como listo en el DOM
  container.setAttribute('data-ready', 'true');

  // Iniciar bucle de animación
  animate();
}

function updateCameraDistance() {
  const width = container ? container.clientWidth : window.innerWidth;
  if (width < 576) {
    camera.position.set(6.8, 3.8, 6.8);
  } else if (width < 992) {
    camera.position.set(5.8, 3.3, 5.8);
  } else {
    camera.position.set(5.1, 2.9, 5.1);
  }
}

function handleResize() {
  if (!container || !renderer || !camera) return;
  const w = container.clientWidth;
  const h = container.clientHeight;
  if (w === 0 || h === 0) return;
  camera.aspect = w / h;
  camera.updateProjectionMatrix();
  renderer.setSize(w, h, false);
}

function setupLighting() {
  const hemiLight = new THREE.HemisphereLight(0xffffff, 0xdcfce7, 0.9);
  hemiLight.position.set(0, 20, 0);
  scene.add(hemiLight);

  const sunLight = new THREE.DirectionalLight(0xffffff, 1.7);
  sunLight.position.set(8, 14, 8);
  sunLight.castShadow = true;
  sunLight.shadow.mapSize.width = 1024;
  sunLight.shadow.mapSize.height = 1024;
  sunLight.shadow.camera.near = 1;
  sunLight.shadow.camera.far = 30;
  sunLight.shadow.camera.left = -4;
  sunLight.shadow.camera.right = 4;
  sunLight.shadow.camera.top = 4;
  sunLight.shadow.camera.bottom = -4;
  sunLight.shadow.bias = -0.001;
  scene.add(sunLight);

  const fillLight = new THREE.DirectionalLight(0xfffbeb, 0.7);
  fillLight.position.set(-8, 6, -6);
  scene.add(fillLight);

  const rimLight = new THREE.DirectionalLight(0x86efac, 0.5);
  rimLight.position.set(-6, 8, 8);
  scene.add(rimLight);
}

function setupMaterials() {
  matYellow = new THREE.MeshStandardMaterial({
    color: 0xFFC72C,
    roughness: 0.35,
    metalness: 0.15,
    name: 'SunbeltYellow'
  });

  matYellowDark = new THREE.MeshStandardMaterial({
    color: 0xE6AE00,
    roughness: 0.45,
    metalness: 0.2
  });

  matDarkSteel = new THREE.MeshStandardMaterial({
    color: 0x1E293B,
    roughness: 0.55,
    metalness: 0.5
  });

  matTracks = new THREE.MeshStandardMaterial({
    color: 0x111827,
    roughness: 0.75,
    metalness: 0.35
  });

  matChrome = new THREE.MeshStandardMaterial({
    color: 0xE2E8F0,
    roughness: 0.1,
    metalness: 0.95
  });

  matGlass = new THREE.MeshPhysicalMaterial({
    color: 0x38BDF8,
    transparent: true,
    opacity: 0.42,
    roughness: 0.08,
    metalness: 0.1,
    transmission: 0.7,
    ior: 1.5,
    reflectivity: 0.8
  });

  matGreenAccent = new THREE.MeshStandardMaterial({
    color: 0x00843D,
    roughness: 0.3,
    metalness: 0.2
  });

  matDetails = new THREE.MeshStandardMaterial({
    color: 0x475569,
    roughness: 0.4,
    metalness: 0.6
  });
}

function buildExcavator() {
  excavatorRoot = new THREE.Group();
  scene.add(excavatorRoot);

  // -------------------------------------------------------------
  // A. TREN DE RODAJE INFERIOR (Orugas y bastidor en X)
  // -------------------------------------------------------------
  const undercarriage = new THREE.Group();
  excavatorRoot.add(undercarriage);

  // Bastidor central en X
  const centerFrameGeo = new THREE.BoxGeometry(1.3, 0.32, 1.6);
  const centerFrame = new THREE.Mesh(centerFrameGeo, matDarkSteel);
  centerFrame.position.y = 0.42;
  centerFrame.castShadow = true;
  centerFrame.receiveShadow = true;
  undercarriage.add(centerFrame);

  // Anillo de giro (Slew ring)
  const slewRingGeo = new THREE.CylinderGeometry(0.55, 0.55, 0.15, 24);
  const slewRing = new THREE.Mesh(slewRingGeo, matDetails);
  slewRing.position.y = 0.62;
  slewRing.castShadow = true;
  undercarriage.add(slewRing);

  // Orugas Izquierda y Derecha
  buildTrackAssembly(undercarriage, -0.92);
  buildTrackAssembly(undercarriage, 0.92);

  // -------------------------------------------------------------
  // B. TORRETA SUPERIOR GIRATORIA (Upperstructure)
  // -------------------------------------------------------------
  upperstructure = new THREE.Group();
  upperstructure.position.y = 0.7;
  excavatorRoot.add(upperstructure);

  // Plataforma principal
  const deckGeo = new THREE.BoxGeometry(1.9, 0.25, 2.7);
  const deck = new THREE.Mesh(deckGeo, matDarkSteel);
  deck.position.set(0, 0.12, 0);
  deck.castShadow = true;
  deck.receiveShadow = true;
  upperstructure.add(deck);

  // Carrocería de motor y compartimentos
  const bodyGeo = new THREE.BoxGeometry(1.85, 0.75, 1.6);
  const body = new THREE.Mesh(bodyGeo, matYellow);
  body.position.set(0, 0.62, -0.45);
  body.castShadow = true;
  body.receiveShadow = true;
  upperstructure.add(body);

  // Rejillas de ventilación de motor laterales
  for (let side of [-1, 1]) {
    const ventGeo = new THREE.BoxGeometry(0.04, 0.45, 0.9);
    const vent = new THREE.Mesh(ventGeo, matDarkSteel);
    vent.position.set(side * 0.94, 0.62, -0.45);
    upperstructure.add(vent);
  }

  // Contrapeso trasero curvado
  const counterweightGeo = new THREE.CylinderGeometry(0.92, 0.92, 0.8, 16, 1, false, 0, Math.PI);
  const counterweight = new THREE.Mesh(counterweightGeo, matYellowDark);
  counterweight.rotation.x = Math.PI / 2;
  counterweight.rotation.y = Math.PI;
  counterweight.position.set(0, 0.62, -1.25);
  counterweight.scale.set(1, 0.45, 1);
  counterweight.castShadow = true;
  upperstructure.add(counterweight);

  // Franja verde corporativa RentaTools en el contrapeso
  const stripeGeo = new THREE.BoxGeometry(1.6, 0.14, 0.05);
  const stripe = new THREE.Mesh(stripeGeo, matGreenAccent);
  stripe.position.set(0, 0.55, -1.38);
  upperstructure.add(stripe);

  // Tubo de escape vertical
  const exhaustGeo = new THREE.CylinderGeometry(0.05, 0.05, 0.6, 12);
  const exhaust = new THREE.Mesh(exhaustGeo, matDarkSteel);
  exhaust.position.set(0.65, 1.25, -0.9);
  exhaust.castShadow = true;
  upperstructure.add(exhaust);

  // Cabina del Operador (Lado Izquierdo)
  buildCabin(upperstructure);

  // Soporte de anclaje de la pluma (Boom Lugs) en el frontal
  const boomLugGeo = new THREE.BoxGeometry(0.12, 0.5, 0.45);
  for (let s of [-0.22, 0.22]) {
    const lug = new THREE.Mesh(boomLugGeo, matDarkSteel);
    lug.position.set(s, 0.45, 0.85);
    lug.castShadow = true;
    upperstructure.add(lug);
  }

  // -------------------------------------------------------------
  // C. BRAZO ARTICULADO (Boom -> Stick -> Bucket)
  // -------------------------------------------------------------
  buildExcavatorArm(upperstructure);
}

function buildTrackAssembly(parent, xPos) {
  const trackGroup = new THREE.Group();
  trackGroup.position.set(xPos, 0, 0);
  parent.add(trackGroup);

  // Viga principal de la oruga
  const beamGeo = new THREE.BoxGeometry(0.48, 0.45, 2.8);
  const beam = new THREE.Mesh(beamGeo, matDarkSteel);
  beam.position.y = 0.38;
  beam.castShadow = true;
  trackGroup.add(beam);

  // Ruedas guía en los extremos (Idler & Sprocket)
  const wheelGeo = new THREE.CylinderGeometry(0.32, 0.32, 0.46, 20);
  wheelGeo.rotateZ(Math.PI / 2);

  const frontWheel = new THREE.Mesh(wheelGeo, matDarkSteel);
  frontWheel.position.set(0, 0.36, 1.25);
  frontWheel.castShadow = true;
  trackGroup.add(frontWheel);

  const rearWheel = new THREE.Mesh(wheelGeo, matDarkSteel);
  rearWheel.position.set(0, 0.36, -1.25);
  rearWheel.castShadow = true;
  trackGroup.add(rearWheel);

  // Rodillos inferiores
  const rollerGeo = new THREE.CylinderGeometry(0.12, 0.12, 0.44, 14);
  rollerGeo.rotateZ(Math.PI / 2);
  for (let i = -3; i <= 3; i++) {
    const roller = new THREE.Mesh(rollerGeo, matDetails);
    roller.position.set(0, 0.16, i * 0.32);
    trackGroup.add(roller);
  }

  // Banda de oruga continua
  const topBeltGeo = new THREE.BoxGeometry(0.52, 0.08, 2.5);
  const topBelt = new THREE.Mesh(topBeltGeo, matTracks);
  topBelt.position.set(0, 0.69, 0);
  topBelt.castShadow = true;
  trackGroup.add(topBelt);

  const bottomBeltGeo = new THREE.BoxGeometry(0.52, 0.08, 2.5);
  const bottomBelt = new THREE.Mesh(bottomBeltGeo, matTracks);
  bottomBelt.position.set(0, 0.05, 0);
  bottomBelt.receiveShadow = true;
  trackGroup.add(bottomBelt);

  // Curvas de oruga delantera y trasera
  const curveGeo = new THREE.CylinderGeometry(0.38, 0.38, 0.52, 16, 1, true, 0, Math.PI);
  curveGeo.rotateZ(Math.PI / 2);

  const frontCurve = new THREE.Mesh(curveGeo, matTracks);
  frontCurve.position.set(0, 0.37, 1.25);
  frontCurve.rotation.y = -Math.PI / 2;
  trackGroup.add(frontCurve);

  const rearCurve = new THREE.Mesh(curveGeo, matTracks);
  rearCurve.position.set(0, 0.37, -1.25);
  rearCurve.rotation.y = Math.PI / 2;
  trackGroup.add(rearCurve);

  // Tejas metálicas decorativas en la parte superior
  const padGeo = new THREE.BoxGeometry(0.54, 0.03, 0.08);
  for (let z = -1.1; z <= 1.1; z += 0.22) {
    const pad = new THREE.Mesh(padGeo, matDarkSteel);
    pad.position.set(0, 0.74, z);
    trackGroup.add(pad);
  }
}

function buildCabin(parent) {
  const cabinGroup = new THREE.Group();
  cabinGroup.position.set(-0.55, 0.25, 0.25);
  parent.add(cabinGroup);

  const cabFrameGeo = new THREE.BoxGeometry(0.72, 1.15, 1.1);
  const cabFrame = new THREE.Mesh(cabFrameGeo, matDarkSteel);
  cabFrame.position.set(0, 0.6, 0);
  cabFrame.castShadow = true;
  cabinGroup.add(cabFrame);

  const windshieldGeo = new THREE.BoxGeometry(0.66, 0.95, 0.06);
  const windshield = new THREE.Mesh(windshieldGeo, matGlass);
  windshield.position.set(0, 0.62, 0.53);
  cabinGroup.add(windshield);

  const sideWindowGeo = new THREE.BoxGeometry(0.06, 0.95, 0.95);
  const sideWindow = new THREE.Mesh(sideWindowGeo, matGlass);
  sideWindow.position.set(-0.34, 0.62, 0);
  cabinGroup.add(sideWindow);

  const seatGeo = new THREE.BoxGeometry(0.35, 0.45, 0.35);
  const seat = new THREE.Mesh(seatGeo, matGreenAccent);
  seat.position.set(0, 0.4, -0.1);
  cabinGroup.add(seat);

  const consoleGeo = new THREE.BoxGeometry(0.3, 0.3, 0.2);
  const consoleBox = new THREE.Mesh(consoleGeo, matDetails);
  consoleBox.position.set(0, 0.3, 0.25);
  cabinGroup.add(consoleBox);

  const lightGeo = new THREE.BoxGeometry(0.2, 0.08, 0.08);
  const lightMat = new THREE.MeshStandardMaterial({
    color: 0xffffff,
    emissive: 0xfffbeb,
    emissiveIntensity: 0.8
  });
  const cabLight = new THREE.Mesh(lightGeo, lightMat);
  cabLight.position.set(0, 1.2, 0.52);
  cabinGroup.add(cabLight);
}

function buildExcavatorArm(parent) {
  boomGroup = new THREE.Group();
  boomGroup.position.set(0.18, 0.45, 0.85);
  parent.add(boomGroup);

  const boomLowerGeo = new THREE.BoxGeometry(0.26, 0.35, 1.6);
  const boomLower = new THREE.Mesh(boomLowerGeo, matYellow);
  boomLower.position.set(0, 0.65, 0.6);
  boomLower.rotation.x = -Math.PI / 4;
  boomLower.castShadow = true;
  boomLower.receiveShadow = true;
  boomGroup.add(boomLower);

  const boomUpperGeo = new THREE.BoxGeometry(0.24, 0.32, 1.6);
  const boomUpper = new THREE.Mesh(boomUpperGeo, matYellow);
  boomUpper.position.set(0, 1.25, 1.7);
  boomUpper.rotation.x = -Math.PI / 10;
  boomUpper.castShadow = true;
  boomUpper.receiveShadow = true;
  boomGroup.add(boomUpper);

  const boomStripeGeo = new THREE.BoxGeometry(0.27, 0.1, 0.8);
  const boomStripe = new THREE.Mesh(boomStripeGeo, matGreenAccent);
  boomStripe.position.set(0, 1.25, 1.7);
  boomStripe.rotation.x = -Math.PI / 10;
  boomGroup.add(boomStripe);

  for (let s of [-0.22, 0.22]) {
    const cylGroup = new THREE.Group();
    cylGroup.position.set(s, 0.2, 0.2);

    const cylBodyGeo = new THREE.CylinderGeometry(0.06, 0.06, 1.1, 12);
    cylBodyGeo.rotateX(Math.PI / 2);
    const cylBody = new THREE.Mesh(cylBodyGeo, matDarkSteel);
    cylBody.position.set(0, 0.3, 0.3);
    cylBody.rotation.x = -Math.PI / 4.5;
    cylGroup.add(cylBody);

    const rodGeo = new THREE.CylinderGeometry(0.035, 0.035, 0.9, 12);
    rodGeo.rotateX(Math.PI / 2);
    const rod = new THREE.Mesh(rodGeo, matChrome);
    rod.position.set(0, 0.6, 0.6);
    rod.rotation.x = -Math.PI / 4.5;
    cylGroup.add(rod);

    boomGroup.add(cylGroup);
  }

  stickGroup = new THREE.Group();
  stickGroup.position.set(0, 1.48, 2.45);
  boomGroup.add(stickGroup);

  const stickBodyGeo = new THREE.BoxGeometry(0.2, 0.28, 1.8);
  const stickBody = new THREE.Mesh(stickBodyGeo, matYellow);
  stickBody.position.set(0, -0.65, 0.4);
  stickBody.rotation.x = Math.PI / 3.2;
  stickBody.castShadow = true;
  stickBody.receiveShadow = true;
  stickGroup.add(stickBody);

  const stickCylGroup = new THREE.Group();
  const stickCylBodyGeo = new THREE.CylinderGeometry(0.05, 0.05, 0.8, 12);
  stickCylBodyGeo.rotateX(Math.PI / 2);
  const stickCylBody = new THREE.Mesh(stickCylBodyGeo, matDarkSteel);
  stickCylBody.position.set(0, 0.1, -0.3);
  stickCylBody.rotation.x = Math.PI / 4;
  stickCylGroup.add(stickCylBody);

  const stickRodGeo = new THREE.CylinderGeometry(0.03, 0.03, 0.7, 12);
  stickRodGeo.rotateX(Math.PI / 2);
  const stickRod = new THREE.Mesh(stickRodGeo, matChrome);
  stickRod.position.set(0, -0.15, 0.05);
  stickRod.rotation.x = Math.PI / 4;
  stickCylGroup.add(stickRod);
  stickGroup.add(stickCylGroup);

  bucketGroup = new THREE.Group();
  bucketGroup.position.set(0, -1.35, 1.05);
  stickGroup.add(bucketGroup);

  buildBucket(bucketGroup);
}

function buildBucket(parent) {
  const bucketGeo = new THREE.BoxGeometry(0.48, 0.45, 0.55);
  const bucket = new THREE.Mesh(bucketGeo, matDarkSteel);
  bucket.position.set(0, -0.15, -0.15);
  bucket.rotation.x = -Math.PI / 6;
  bucket.castShadow = true;
  bucket.receiveShadow = true;
  parent.add(bucket);

  const sidePlateGeo = new THREE.BoxGeometry(0.52, 0.35, 0.25);
  const sidePlate = new THREE.Mesh(sidePlateGeo, matYellow);
  sidePlate.position.set(0, -0.15, -0.15);
  sidePlate.rotation.x = -Math.PI / 6;
  parent.add(sidePlate);

  const toothGeo = new THREE.ConeGeometry(0.04, 0.18, 5);
  toothGeo.rotateX(Math.PI / 2);
  for (let t = -0.18; t <= 0.18; t += 0.12) {
    const tooth = new THREE.Mesh(toothGeo, matChrome);
    tooth.position.set(t, -0.38, 0.12);
    tooth.rotation.x = -Math.PI / 4;
    tooth.castShadow = true;
    parent.add(tooth);
  }
}

function setupGroundShadow() {
  const shadowGeo = new THREE.PlaneGeometry(6.5, 6.5);
  const shadowMat = new THREE.ShadowMaterial({
    opacity: 0.22
  });
  const ground = new THREE.Mesh(shadowGeo, shadowMat);
  ground.rotation.x = -Math.PI / 2;
  ground.position.y = 0.01;
  ground.receiveShadow = true;
  scene.add(ground);

  const discGeo = new THREE.CircleGeometry(2.6, 48);
  const discMat = new THREE.MeshBasicMaterial({
    color: 0x00843D,
    transparent: true,
    opacity: 0.05
  });
  const disc = new THREE.Mesh(discGeo, discMat);
  disc.rotation.x = -Math.PI / 2;
  disc.position.y = 0.005;
  scene.add(disc);
}

function showFallback() {
  if (!container) return;
  container.innerHTML = `
    <div class="sb-hero-3d-fallback">
      <i class="fa fa-truck-monster" style="font-size: 6rem; color: #FFC72C; opacity: 0.85;"></i>
    </div>
  `;
}

function animate() {
  animFrameId = requestAnimationFrame(animate);

  if (isPaused) return;

  const elapsedTime = clock ? clock.getElapsedTime() : 0;
  const prefersReduced = window.matchMedia('(prefers-reduced-motion: reduce)').matches;

  if (!prefersReduced && excavatorRoot) {
    if (upperstructure) {
      upperstructure.rotation.y = Math.sin(elapsedTime * 0.45) * 0.18;
    }
    if (boomGroup) {
      boomGroup.rotation.x = Math.sin(elapsedTime * 0.6) * 0.1 - 0.05;
    }
    if (stickGroup) {
      stickGroup.rotation.x = Math.cos(elapsedTime * 0.6 + 0.4) * 0.14 + 0.05;
    }
    if (bucketGroup) {
      bucketGroup.rotation.x = Math.sin(elapsedTime * 0.6 + 0.8) * 0.22 - 0.1;
    }
  }

  if (controls) {
    controls.update();
  }

  if (renderer && scene && camera) {
    renderer.render(scene, camera);
  }
}

function pauseLoop() {
  isPaused = true;
}

function resumeLoop() {
  if (isPaused) {
    isPaused = false;
    if (clock) clock.getDelta();
  }
}

function disposeHero3D() {
  if (animFrameId) cancelAnimationFrame(animFrameId);
  window.removeEventListener('resize', handleResize);
  if (renderer && renderer.domElement && renderer.domElement.parentNode) {
    renderer.domElement.parentNode.removeChild(renderer.domElement.parentNode);
    renderer.dispose();
  }
}

if (document.readyState === 'loading') {
  document.addEventListener('DOMContentLoaded', initHero3D);
} else {
  setTimeout(initHero3D, 50);
}

window.Hero3D = {
  init: initHero3D,
  pause: pauseLoop,
  resume: resumeLoop,
  dispose: disposeHero3D,
  isReady: () => !!(container && container.getAttribute('data-ready') === 'true')
};
