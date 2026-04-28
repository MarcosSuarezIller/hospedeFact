# HospedeFact

**Aplicación de Gestión Unificada para Negocios de Hostelería Mixtos (Hotel + Restaurante)**

Una solución móvil nativa Android que integra la gestión de hospedaje y restaurante en una única plataforma, automatizando la facturación consolidada y reduciendo la carga administrativa mediante sincronización de datos en tiempo real.

---

## Tabla de Contenidos

- [Descripción General](#descripción-general)
- [Características](#características)
- [Requisitos](#requisitos)
- [Instalación](#instalación)
- [Configuración](#configuración)
- [Uso](#uso)
- [Estructura del Proyecto](#estructura-del-proyecto)
- [Tecnología](#tecnología)
- [Roles y Permisos](#roles-y-permisos)
- [Desarrollo](#desarrollo)
- [Testing](#testing)
- [Despliegue](#despliegue)
- [Licencia](#licencia)
- [Autor](#autor)

---

## Descripción General

HospedeFact resuelve un problema real en pequeños y medianos negocios de hostelería: la fragmentación en la gestión de servicios. Actualmente, estos establecimientos operan con sistemas separados para alojamiento y restaurante, lo que genera:

- Duplicación de datos
- Errores manuales en facturación
- Pérdida de tiempo administrativo
- Falta de visión unificada del cliente

**La solución:** Una aplicación que centraliza todo en una única plataforma con:
- ✅ Gestión integrada de huéspedes y habitaciones
- ✅ Toma de pedidos de restaurante vinculados a habitaciones
- ✅ Facturación consolidada automática
- ✅ Control de almacén y órdenes de compra
- ✅ Sincronización en tiempo real entre dispositivos
- ✅ Sistema de mesas para restaurante

---

## Características

### Módulo de Hospedaje
- **Gestión de Huéspedes:** Crear, editar, listar huéspedes activos
- **Gestión de Habitaciones:** Tipos de habitación, tarifas configurables, control de estado (disponible, ocupada, limpieza)
- **Check-in/Check-out:** Registro automático de fechas de entrada y salida
- **Histórico:** Acceso a datos históricos de estancias

### Módulo de Restaurante
- **Toma de Pedidos:** Por huésped o por mesa
- **Menú Digital:** Productos con precios, categorías (cargados desde almacén)
- **Carrito de Compra:** Cantidad, precios unitarios, totales actualizados
- **Gestión de Mesas:** Estados (disponible, ocupada, reservada, mantenimiento)
- **Vinculación a Huésped:** Los consumos de restaurante se asignan automáticamente a la habitación

### Facturación
- **Factura Simple:** Consolidación de pedidos de restaurante + IVA (21%)
- **Factura con Estancia:** Cálculo automático (días × precio/noche) + consumos de restaurante + IVA
- **Factura de Mesa:** Consolidación de todos los pedidos de una mesa
- **Detalles Completos:** Línea por línea, con cantidades, precios y subtotales

### Gestión de Almacén
- **Control de Stock:** Gestión de productos con stock actual y mínimo
- **Precios Duales:** Precio de compra y precio de venta (menú)
- **Órdenes de Compra:** Crear, cambiar estado, recibir mercancía
- **Movimientos de Stock:** Registro de entrada, salida, ajuste, pérdida
- **Gestión de Proveedores:** Directorio de proveedores con datos de contacto
- **Alertas de Stock:** Notificación cuando llega a mínimo

### Sincronización
- **Tiempo Real:** Todos los dispositivos ven los cambios inmediatamente
- **Offline-First:** Funciona sin conexión y sincroniza cuando vuelve online
- **Multi-usuario:** Múltiples camareros, recepcionistas y gerentes trabajando simultáneamente

---

## Requisitos

### Hardware
- Tablet o Smartphone con Android 9 (API 28) o superior
- Mínimo 2GB de RAM
- Conexión a Internet (WiFi o datos móviles)

### Software
- Android Studio 2023.1 o superior (para compilación)
- JDK 11 o superior
- Gradle 7.4 o superior

### Dependencias Principales
- Kotlin 1.8+
- Firebase Cloud Firestore
- Firebase Authentication
- AndroidX (Navigation, Lifecycle, ViewModel)
- Material Design 3

---

## Instalación

### Opción 1: Clonar el Repositorio

```bash
git clone https://github.com/MarcosSuarezIller/hospedeFact.git
cd hospedeFact
```

### Opción 2: Descargar APK

Descarga el APK compilado directamente desde `/releases`:
1. Navega a la carpeta de releases
2. Descarga `hospedeFact-release.apk`
3. Transfiere a tu dispositivo Android
4. Instala (requiere habilitar "Instalación de fuentes desconocidas")

---

## Configuración

### 1. Configurar Firebase

1. Ve a [Firebase Console](https://console.firebase.google.com)
2. Crea un nuevo proyecto (o usa uno existente)
3. Agrega una app Android:
   - Package Name: `com.example.hospedeFact`
   - SHA-1: Obtén desde: `./gradlew signingReport`
4. Descarga `google-services.json`
5. Coloca en: `app/google-services.json`

### 2. Configurar Firestore

En Firebase Console:

**Crear Colecciones:**
- `usuarios`
- `huespedes`
- `habitaciones`
- `pedidos`
- `facturas`
- `mesas`
- `productos_almacen`
- `proveedores`
- `ordenes_compra`
- `movimientos_stock`

**Agregar Reglas de Seguridad:**

```firestore
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // Huéspedes: Recepcionista y Gerente
    match /huespedes/{document=**} {
      allow read, write: if request.auth != null 
        && get(/databases/$(database)/documents/usuarios/$(request.auth.uid)).data.rol in ['recepcionista', 'gerente'];
    }
    
    // Pedidos: Camarero y Gerente
    match /pedidos/{document=**} {
      allow read: if request.auth != null;
      allow write: if request.auth != null 
        && get(/databases/$(database)/documents/usuarios/$(request.auth.uid)).data.rol in ['camarero', 'gerente'];
    }
    
    // Facturas: Todos autenticados
    match /facturas/{document=**} {
      allow read: if request.auth != null;
      allow write: if request.auth != null 
        && get(/databases/$(database)/documents/usuarios/$(request.auth.uid)).data.rol in ['recepcionista', 'camarero', 'gerente'];
    }
    
    // Habitaciones, Mesas, Almacén: Solo Gerente
    match /habitaciones/{document=**} {
      allow read, write: if request.auth != null 
        && get(/databases/$(database)/documents/usuarios/$(request.auth.uid)).data.rol == 'gerente';
    }
    
    match /mesas/{document=**} {
      allow read, write: if request.auth != null 
        && get(/databases/$(database)/documents/usuarios/$(request.auth.uid)).data.rol == 'gerente';
    }
    
    match /productos_almacen/{document=**} {
      allow read: if request.auth != null;
      allow write: if request.auth != null 
        && get(/databases/$(database)/documents/usuarios/$(request.auth.uid)).data.rol == 'gerente';
    }
  }
}
```

### 3. Crear Usuarios de Prueba

En Firebase Authentication, crea estos usuarios:

| Email | Contraseña | Rol |
|-------|-----------|-----|
| gerente@hospedeFact.com | 123456 | gerente |
| camarero@hospedeFact.com | 123456 | camarero |
| recepcionista@hospedeFact.com | 123456 | recepcionista |

Luego agrega documentos en `usuarios/{uid}` con:
```json
{
  "nombre": "Nombre del Usuario",
  "email": "email@example.com",
  "rol": "gerente|camarero|recepcionista"
}
```

---

## Uso

### Primer Inicio

1. Abre la app
2. Login con `gerente@hospedeFact.com` / `123456`
3. Se abre el Dashboard con opciones según tu rol

### Flujo Típico de Uso

#### Como Gerente

1. **Configurar Habitaciones:** Dashboard → Habitaciones → Crear habitación (número, tipo, precio/noche, capacidad)
2. **Agregar Productos al Menú:** Dashboard → Almacén → Crear producto (nombre, precio compra, precio venta, stock)
3. **Crear Órdenes de Compra:** Dashboard → Almacén → Órdenes de Compra → Crear orden
4. **Gestionar Mesas:** Dashboard → Mesas → Crear mesa (número, capacidad, ubicación)

#### Como Recepcionista

1. **Crear Huésped:** Dashboard → Huéspedes → Nuevo → Ingresar datos → Seleccionar habitación
2. **Check-in:** Seleccionar huésped → Confirmar
3. **Check-out y Factura:** Dashboard → Facturas → Huésped con Estancia → Seleccionar huésped → Ver detalles automáticos → Generar Factura

#### Como Camarero

1. **Tomar Pedido de Mesa:** Dashboard → Pedidos → Nuevo → "Tipo: Mesa" → Seleccionar mesa → Añadir productos → Crear
2. **Tomar Pedido de Huésped:** Dashboard → Pedidos → Nuevo → "Tipo: Huésped" → Seleccionar huésped → Añadir productos → Crear
3. **Generar Factura de Mesa:** Dashboard → Facturas → Mesa → Seleccionar mesa → Generar

---

## Estructura del Proyecto

```
HospedeFact/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/hospedeFact/
│   │   │   │   ├── data/
│   │   │   │   │   ├── models/              # Data classes
│   │   │   │   │   └── repository/          # Acceso a Firestore
│   │   │   │   ├── ui/
│   │   │   │   │   ├── auth/               # Login
│   │   │   │   │   ├── dashboard/          # Pantalla principal
│   │   │   │   │   ├── huespedes/          # CRUD de huéspedes
│   │   │   │   │   ├── habitaciones/       # CRUD de habitaciones
│   │   │   │   │   ├── pedidos/            # Toma de pedidos
│   │   │   │   │   ├── factura/            # Facturación
│   │   │   │   │   ├── restauracion/       # Mesas
│   │   │   │   │   └── almacen/            # Almacén
│   │   │   │   └── navigation/             # Navegación
│   │   │   ├── res/
│   │   │   │   ├── layout/                 # XML de pantallas
│   │   │   │   ├── values/                 # Strings, colores, dimensiones
│   │   │   │   └── navigation/             # nav_graph.xml
│   │   │   └── AndroidManifest.xml
│   │   └── test/                           # Tests unitarios
│   ├── build.gradle                        # Dependencias
│   └── google-services.json                # Configuración Firebase
├── build.gradle
├── settings.gradle
└── README.md
```

---

## Tecnología

### Backend
- **Firebase Cloud Firestore:** Base de datos NoSQL en tiempo real
- **Firebase Authentication:** Autenticación y gestión de usuarios
- **Firebase Security Rules:** Control de acceso basado en roles

### Frontend
- **Kotlin:** Lenguaje de programación moderno
- **Android Native:** Aplicación nativa no híbrida
- **MVVM Architecture:** Separación de capas (Model, View, ViewModel)
- **Material Design 3:** Diseño moderno y responsivo
- **Navigation Component:** Navegación entre pantallas
- **LiveData & ViewModel:** Gestión de estado reactiva
- **Coroutines:** Programación asincrónica

### Herramientas
- **Android Studio:** IDE de desarrollo
- **Gradle:** Sistema de compilación
- **Git:** Control de versiones

---

## Roles y Permisos

### Gerente
- Ver todas las funcionalidades
- Crear/editar/eliminar habitaciones
- Crear/editar/eliminar mesas
- Crear/editar/eliminar productos del almacén
- Crear órdenes de compra
- Ver histórico de todas las transacciones

### Camarero
- Ver huéspedes (solo lectura)
- Ver mesas
- Crear pedidos (para mesa o huésped)
- Generar facturas de mesa
- Ver estado de mesas

### Recepcionista
- Crear/editar/eliminar huéspedes
- Ver habitaciones
- Hacer check-in/check-out
- Registrar servicios adicionales
- Generar facturas de estancia

---

## 💻 Desarrollo

### Configurar Entorno Local

```bash
# Clonar repositorio
git clone https://github.com/MarcosSuarezIller/hospedeFact.git
cd hospedeFact

# Abrir en Android Studio
# File → Open → Seleccionar carpeta hospedeFact

# Compilar
./gradlew build

# Ejecutar en emulador
./gradlew emulateDebug
```

### Estructura MVVM

Cada módulo sigue:
1. **Model:** Data classes en `data/models/`
2. **Repository:** Acceso a Firestore en `data/repository/`
3. **ViewModel:** Lógica en `ui/{modulo}/{NombreViewModel}.kt`
4. **View:** UI en `ui/{modulo}/{NombreFragment}.kt` y `res/layout/fragment_{nombre}.xml`

### Ejemplo: Crear un Nuevo Módulo

1. Crear data class en `data/models/MiEntidad.kt`
2. Crear repository en `data/repository/MiEntidadRepository.kt`
3. Crear ViewModel en `ui/miomodulo/MioViewModel.kt`
4. Crear Fragment en `ui/miomodulo/MioFragment.kt`
5. Crear layout en `res/layout/fragment_mio.xml`
6. Agregar a `nav_graph.xml`

---

## 🧪 Testing

### Ejecutar Pruebas

```bash
# Tests unitarios
./gradlew test

# Tests instrumentados (en dispositivo/emulador)
./gradlew connectedAndroidTest
```

### Checklist de Pruebas Manual

- [ ] Login con cada rol
- [ ] CRUD de huéspedes
- [ ] CRUD de habitaciones
- [ ] Crear pedido de huésped
- [ ] Crear pedido de mesa
- [ ] Generar factura simple
- [ ] Generar factura con estancia
- [ ] Sincronización entre dispositivos
- [ ] Cambio de estado de mesa
- [ ] Crear orden de compra
- [ ] Cambiar estado de orden

---

## Despliegue

### Compilar APK de Producción

```bash
./gradlew assembleRelease
```

El APK estará en: `app/build/outputs/apk/release/app-release.apk`

### Firmar APK

```bash
jarsigner -verbose -sigalg SHA1withRSA -digestalg SHA1 \
  -keystore ~/hospedeFact.keystore app-release-unsigned.apk hospedeFact-key
```

### Distribuir a Clientes

**Opción 1: Sideloading (Manual)**
1. Conectar dispositivo por USB
2. Copiar APK al dispositivo
3. Instalar manualmente

**Opción 2: MDM (Mobile Device Management)**
1. Si el cliente tiene MDM, distribuir a través de ese sistema

**Opción 3: Google Play (Futuro)**
1. Publicar en Google Play Store privada

### Monitoreo en Producción

```bash
# Ver logs de Firebase
firebase logging tail

# Monitorear Firestore
firebase firestore:get {collection}/{document}
```

---

## Reportar Bugs

Si encuentras un bug:

1. Abre un issue en GitHub describiendo:
   - Qué intentabas hacer
   - Qué pasó (comportamiento real)
   - Qué debería haber pasado (comportamiento esperado)
   - Pasos para reproducir
   - Dispositivo y versión de Android

2. Etiqueta según severidad:
   - `bug-crítico:` bloquea funcionalidad
   - `bug-moderado:` afecta usabilidad
   - `bug-menor:` cosméticos o raros

---

## 📈 Roadmap Futuro

### Corto Plazo (1-3 meses)
- [ ] Integración con pasarelas de pago (Stripe, Redsys)
- [ ] Exportación de facturas a PDF
- [ ] Historial de precios en productos

### Mediano Plazo (3-6 meses)
- [ ] Dashboard de estadísticas (ingresos, ocupación)
- [ ] Previsiones de demanda basadas en histórico
- [ ] Integración con software contable (Odoo, SageOne)
- [ ] Aplicación iOS (React Native o Swift)

### Largo Plazo (6+ meses)
- [ ] Channel Manager integrado (Booking.com, Airbnb)
- [ ] Sistema de reservas online en web
- [ ] Machine Learning para optimización de precios
- [ ] Gamificación (objetivos, incentivos)

---

## Licencia

Este proyecto está bajo licencia MIT. Ver [LICENSE](LICENSE) para más detalles.

---

## Autor

**Marcos Suárez Illerías**
- Email: marcos.saja@gmail.com
- GitHub: [@MarcosSuarezIller](https://github.com/MarcosSuarezIller)
- Proyecto Final de DAM - IES Augusto González de Linares (2024-2025)

---

## Soporte

Para preguntas o soporte:
1. Abre un issue en GitHub
2. Contacta al autor

---

## Agradecimientos

- Firebase por la infraestructura serverless
- Google por Android Studio y Kotlin
- Material Design por los principios de UI/UX
- La comunidad de Android por las mejores prácticas

---

**Última actualización:** Abril de 2026
