# 📁 Guía de Imágenes y Assets de RentaTools PRO

Esta carpeta contiene todos los recursos visuales del proyecto. Al colocar tus imágenes aquí, Spring Boot las sirve automáticamente bajo la ruta web `/assets/img/...`.

---

## 📂 Estructura de Subcarpetas

```text
frontend/assets/img/
├── logo/          -> Logos corporativos, isotipos y favicon
├── machinery/     -> Fotos de maquinaria pesada, herramientas y equipos
├── banners/       -> Imágenes de fondo, headers y banners de promociones
└── icons/         -> Íconos vectoriales o badges personalizados
```

---

## 🏷️ 1. `logo/` (Logotipos e Identidad de Marca)
- **Archivos incluidos por defecto:**
  - `logo.svg`: Logotipo completo horizontal para la barra de navegación superior.
  - `logo-icon.svg`: Isotipo compacto (ícono solo) para dispositivos móviles y favicon.
- **Formatos recomendados:**
  - **SVG (Vectorial):** Ideal para logotipos, no pierde calidad al cambiar de resolución y pesa muy poco.
  - **PNG:** Con fondo transparente (`alpha channel`), resolución recomendada: `512x512px` o superior.
- **Cómo usarlo en el código:**
  - En HTML: `<img src="assets/img/logo/logo.svg" alt="RentaTools PRO">`

---

## 🚜 2. `machinery/` (Maquinaria Pesada y Herramientas)
- **Propósito:** Aquí se deben guardar las fotos reales de retroexcavadoras, andamios, mezcladoras, taladros, etc.
- **Formatos recomendados:**
  - **WebP:** Máxima compresión sin pérdida visual (ideal para rendimiento y carga instantánea).
  - **JPG / JPEG:** Con calidad al 85% para balance entre peso y nitidez.
- **Dimensiones sugeridas:**
  - **Resolución:** `800 x 500 px` o relación de aspecto `16:10`.
  - **Peso máximo recomendado:** Entre `80 KB` y `250 KB` por imagen.
- **Convención de nombres sugerida (minúsculas y guiones):**
  - `retroexcavadora-cat-420f2.webp`
  - `torre-iluminacion-generac.webp`
  - `mezcladora-diesel-380l.webp`
  - `andamio-multidireccional-20m2.webp`
  - `taladro-demoledor-bosch.webp`
  - `compactadora-canguro-weber.webp`
- **Cómo usarlo en el código o base de datos:**
  - En la base de datos o en `ToolCatalogFallbackController.java`:
    `"imageUrl": "/assets/img/machinery/retroexcavadora-cat-420f2.webp"`
  - En HTML/JS: Se cargará automáticamente en las tarjetas del catálogo y en los modales.

---

## 🖼️ 3. `banners/` (Banners y Fondos de Sección)
- **Propósito:** Banners del Hero del marketplace, fondos para reportes o promociones especiales.
- **Dimensiones sugeridas:** `1920 x 600 px` o `1280 x 480 px`.

---

## ⚡ 4. Sincronización Automática con Spring Boot
Recuerda que los archivos del frontend se encuentran en `frontend/` y al compilar o preparar la aplicación se sincronizan con `infrastructure/src/main/resources/static/`.

Para copiar tus imágenes de una sola vez al backend, ejecuta:
```bash
cp -r frontend/assets/ infrastructure/src/main/resources/static/
```
Así estarán disponibles de inmediato al correr el servidor.
