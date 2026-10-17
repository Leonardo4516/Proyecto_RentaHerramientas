import * as THREE from 'three';
import { OrbitControls } from 'three/addons/controls/OrbitControls.js';
import { GLTFLoader } from 'three/addons/loaders/GLTFLoader.js';

/**
 * RentaTools PRO - Escenario 3D Interactivo del Hero
 * Modelo Oficial: Retroexcavadora XCMG Wheeled Excavator (Meshy 3D PBR)
 * Integrado con iluminación de estudio industrial, sombras suaves de contacto,
 * rotación continua sutil con amortiguación inercial y control táctil/ratón ergonómico.
 */

let scene, camera, renderer, controls;
let container = null;
let animFrameId = null;
let clock = null;
let isPaused = false;
let userInteracting = false;
let userInteractionTimeout = null;

// Raíz del modelo 3D cargado
let excavatorRoot = null;
let excavatorModel = null;
let baseModelY = 0;
let isModelLoaded = false;

function initHero3D() {
  container = document.getElementById('hero-3d-canvas');
  if (!container) return;

  // 1. Comprobar compatibilidad WebGL
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

  // 2. Escena
  scene = new THREE.Scene();
  scene.background = null; // Fondo transparente sobre el hero minimalista

  // 3. Cámara
  const w = container.clientWidth || 600;
  const h = container.clientHeight || 500;
  const aspect = w / (h || 1);
  camera = new THREE.PerspectiveCamera(36, aspect, 0.1, 100);
  updateCameraDistance();

  // 4. Renderer de Alto Rendimiento
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
  renderer.toneMappingExposure = 1.18;
  renderer.outputColorSpace = THREE.SRGBColorSpace;

  const canvas = renderer.domElement;
  canvas.style.width = '100%';
  canvas.style.height = '100%';
  canvas.style.display = 'block';
  canvas.style.touchAction = 'pan-y'; // Permite scroll táctil natural en móviles
  canvas.setAttribute('aria-label', 'Modelo 3D Interactivo de Retroexcavadora XCMG');
  
  // Limpiar contenedor previo e insertar canvas
  container.innerHTML = '';
  container.appendChild(canvas);

  // 5. Spinner sutil de inicialización
  showLoadingIndicator();

  // 6. Controles Orbitales Ergonómicos
  controls = new OrbitControls(camera, canvas);
  controls.enableDamping = true;
  controls.dampingFactor = 0.05;
  controls.enableZoom = false; // Sin interferir con el scroll de la página
  controls.enablePan = false;  // Mantiene el modelo perfectamente centrado
  controls.minPolarAngle = Math.PI * 0.15;
  controls.maxPolarAngle = Math.PI * 0.48; // Nunca mirar desde debajo del piso
  controls.autoRotate = true;
  controls.autoRotateSpeed = 0.55;
  controls.target.set(0, 1.05, 0);

  controls.addEventListener('start', () => {
    userInteracting = true;
    controls.autoRotate = false;
    if (userInteractionTimeout) clearTimeout(userInteractionTimeout);
  });

  controls.addEventListener('end', () => {
    userInteracting = false;
    userInteractionTimeout = setTimeout(() => {
      if (!userInteracting) controls.autoRotate = true;
    }, 1000);
  });

  // 7. Iluminación de Estudio Industrial
  setupLighting();

  // 8. Sombra de Contacto y Plataforma en el Piso
  setupGroundShadow();

  // 9. Cargar Modelo 3D Oficial Meshy XCMG Wheeled Excavator (.glb)
  loadExcavatorModel();

  // 10. Observador de Redimensionamiento (Responsive)
  const resizeObserver = new ResizeObserver(() => {
    handleResize();
  });
  resizeObserver.observe(container);
  window.addEventListener('resize', handleResize);

  // 11. Observador de Visibilidad (Ahorro de batería y recursos)
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

  // Iniciar bucle de renderizado
  animate();
}

function showLoadingIndicator() {
  const spinner = document.createElement('div');
  spinner.id = 'hero-3d-spinner';
  spinner.className = 'd-flex flex-column align-items-center justify-content-center';
  spinner.style.position = 'absolute';
  spinner.style.top = '50%';
  spinner.style.left = '50%';
  spinner.style.transform = 'translate(-50%, -50%)';
  spinner.style.pointerEvents = 'none';
  spinner.style.zIndex = '2';
  spinner.style.transition = 'opacity 0.4s ease';
  spinner.innerHTML = `
    <div class="spinner-border text-warning mb-2" role="status" style="width: 2.2rem; height: 2.2rem; border-width: 0.22em;">
      <span class="visually-hidden">Cargando 3D...</span>
    </div>
    <span style="font-size: 0.78rem; font-weight: 600; letter-spacing: 0.5px; color: #475569;">Cargando Maquinaria 3D...</span>
  `;
  container.appendChild(spinner);
}

function hideLoadingIndicator() {
  const spinner = document.getElementById('hero-3d-spinner');
  if (spinner) {
    spinner.style.opacity = '0';
    setTimeout(() => {
      if (spinner.parentNode) spinner.parentNode.removeChild(spinner);
    }, 450);
  }
}

function updateCameraDistance() {
  if (!container || !camera) return;
  const width = container.clientWidth || window.innerWidth;
  const height = container.clientHeight || window.innerHeight;
  const aspect = width / (height || 1);

  if (width < 576) {
    camera.position.set(6.4, 3.4, 6.4);
  } else if (width < 992) {
    camera.position.set(5.5, 2.9, 5.5);
  } else {
    const d = aspect > 1.3 ? 5.0 : 5.5;
    camera.position.set(d, d * 0.48, d);
  }
  if (controls) {
    controls.target.set(0, 1.05, 0);
  }
}

function handleResize() {
  if (!container || !renderer || !camera) return;
  const w = container.clientWidth;
  const h = container.clientHeight;
  if (w === 0 || h === 0) return;
  camera.aspect = w / h;
  updateCameraDistance();
  camera.updateProjectionMatrix();
  renderer.setSize(w, h, false);
}

function setupLighting() {
  // Luz ambiental semiesférica (Cielo blanco / rebote suelo verde sutil)
  const hemiLight = new THREE.HemisphereLight(0xffffff, 0xdcfce7, 1.1);
  hemiLight.position.set(0, 20, 0);
  scene.add(hemiLight);

  // Luz solar principal con sombras suaves
  const sunLight = new THREE.DirectionalLight(0xffffff, 2.0);
  sunLight.position.set(8, 14, 8);
  sunLight.castShadow = true;
  sunLight.shadow.mapSize.width = 1024;
  sunLight.shadow.mapSize.height = 1024;
  sunLight.shadow.camera.near = 0.5;
  sunLight.shadow.camera.far = 30;
  sunLight.shadow.camera.left = -4;
  sunLight.shadow.camera.right = 4;
  sunLight.shadow.camera.top = 4;
  sunLight.shadow.camera.bottom = -4;
  sunLight.shadow.bias = -0.0008;
  scene.add(sunLight);

  // Luz de relleno lateral cálida
  const fillLight = new THREE.DirectionalLight(0xfffbeb, 0.9);
  fillLight.position.set(-8, 6, -6);
  scene.add(fillLight);

  // Luz de contorno (Rim light) en tono verde corporativo
  const rimLight = new THREE.DirectionalLight(0x86efac, 0.65);
  rimLight.position.set(-6, 8, 8);
  scene.add(rimLight);
}

function setupGroundShadow() {
  // Plano invisible receptor de sombras de contacto bajo las ruedas
  const shadowGeo = new THREE.PlaneGeometry(8, 8);
  const shadowMat = new THREE.ShadowMaterial({
    opacity: 0.24
  });
  const ground = new THREE.Mesh(shadowGeo, shadowMat);
  ground.rotation.x = -Math.PI / 2;
  ground.position.y = 0.005;
  ground.receiveShadow = true;
  scene.add(ground);

  // Disco estético suave para asentar visualmente la máquina en el hero
  const discGeo = new THREE.CircleGeometry(3.0, 48);
  const discMat = new THREE.MeshBasicMaterial({
    color: 0x00843D,
    transparent: true,
    opacity: 0.05
  });
  const disc = new THREE.Mesh(discGeo, discMat);
  disc.rotation.x = -Math.PI / 2;
  disc.position.y = 0.002;
  scene.add(disc);
}

function loadExcavatorModel() {
  const loader = new GLTFLoader();
  const modelUrl = 'models/xcmg_excavator.glb';

  loader.load(
    modelUrl,
    (gltf) => {
      excavatorModel = gltf.scene;

      // Calcular dimensiones exactas de la caja delimitadora del modelo
      const box = new THREE.Box3().setFromObject(excavatorModel);
      const size = box.getSize(new THREE.Vector3());
      const center = box.getCenter(new THREE.Vector3());

      // Normalizar la escala para que encaje armónicamente en el escenario hero (aprox 3.5 unidades)
      const maxDim = Math.max(size.x, size.y, size.z);
      const targetSize = 3.5;
      const scale = targetSize / (maxDim || 1);

      excavatorRoot = new THREE.Group();
      excavatorRoot.name = 'excavatorRoot';
      scene.add(excavatorRoot);

      excavatorModel.scale.setScalar(scale);

      // Centrar el modelo horizontalmente y colocar la base de ruedas sobre el piso (y = 0)
      excavatorModel.position.x = -center.x * scale;
      baseModelY = -box.min.y * scale;
      excavatorModel.position.y = baseModelY;
      excavatorModel.position.z = -center.z * scale;

      // Orientar en ángulo 3/4 atractivo hacia el usuario
      excavatorRoot.rotation.y = -0.42;

      // Habilitar sombras y asegurar renderizado PBR de doble cara
      excavatorModel.traverse((child) => {
        if (child.isMesh) {
          child.castShadow = true;
          child.receiveShadow = true;
          if (child.material) {
            child.material.side = THREE.DoubleSide;
            child.material.needsUpdate = true;
          }
        }
      });

      excavatorRoot.add(excavatorModel);
      isModelLoaded = true;

      // Ocultar indicador de carga con transición suave
      hideLoadingIndicator();
    },
    (xhr) => {
      // Progreso opcional
      if (xhr.lengthComputable) {
        const percent = Math.round((xhr.loaded / xhr.total) * 100);
        const spinnerText = container.querySelector('#hero-3d-spinner span');
        if (spinnerText) {
          spinnerText.textContent = `Cargando 3D (${percent}%)...`;
        }
      }
    },
    (error) => {
      console.error('Error cargando modelo 3D XCMG:', error);
      hideLoadingIndicator();
      showFallback();
    }
  );
}

function showFallback() {
  if (!container) return;
  container.innerHTML = `
    <div class="sb-hero-3d-fallback d-flex flex-column align-items-center justify-content-center h-100 text-center p-4">
      <i class="fa fa-truck-monster" style="font-size: 5.5rem; color: #FFC72C; opacity: 0.88; margin-bottom: 1rem;"></i>
      <h5 class="fw-bold text-dark mb-1">Maquinaria Pesada Lista para Operar</h5>
      <p class="text-muted small mb-0">Catálogo certificado con disponibilidad inmediata en Bogotá y Sabana.</p>
    </div>
  `;
}

function animate() {
  animFrameId = requestAnimationFrame(animate);

  if (isPaused) return;

  const elapsedTime = clock ? clock.getElapsedTime() : 0;
  const prefersReduced = window.matchMedia('(prefers-reduced-motion: reduce)').matches;

  // Movimiento sutil continuo de la retroexcavadora (respiración / ralentí del motor industrial)
  if (!prefersReduced && excavatorRoot && isModelLoaded) {
    excavatorRoot.position.y = Math.sin(elapsedTime * 0.75) * 0.022;
  }

  // Actualización de controles orbitales con inercia y autorotación sutil
  if (controls) {
    controls.update();
  }

  // Renderizar fotograma
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

// API pública para depuración y control en pruebas E2E
window.Hero3D = {
  init: initHero3D,
  pause: pauseLoop,
  resume: resumeLoop,
  dispose: disposeHero3D,
  isReady: () => !!(container && container.getAttribute('data-ready') === 'true'),
  isModelLoaded: () => isModelLoaded,
  getModel: () => excavatorModel,
  getRoot: () => excavatorRoot,
  getControls: () => controls
};
