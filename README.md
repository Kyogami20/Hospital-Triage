<div align="center">

# 🏥 MediTriage System
### Sistema de Triaje Hospitalario con Estructuras de Datos

![C++](https://img.shields.io/badge/C++-17-00599C?style=for-the-badge&logo=c%2B%2B&logoColor=white)
![Qt](https://img.shields.io/badge/Qt-6.x-41CD52?style=for-the-badge&logo=qt&logoColor=white)
![SQL](https://img.shields.io/badge/SQLite-3-003B57?style=for-the-badge&logo=sqlite&logoColor=white)
![Status](https://img.shields.io/badge/Estado-En%20Desarrollo-orange?style=for-the-badge)

> **Proyecto Final — Curso de Estructura de Datos**  
> Aplicación de escritorio que simula un sistema de triaje médico hospitalario,  
> implementando estructuras de datos fundamentales desde cero en C++17 con interfaz gráfica en Qt 6.

</div>

---

## 🧠 Descripción del Proyecto

**MediTriage System** es una aplicación de escritorio que simula el flujo de atención de urgencias en un hospital, basada en el **Estándar de Triaje de Manchester (MTS)**. El sistema permite registrar pacientes, asignarles un nivel de urgencia y gestionar su atención de forma priorizada usando estructuras de datos implementadas manualmente.

El proyecto fue desarrollado como aplicación práctica del **Curso de Estructura de Datos**, demostrando el uso real y combinado de colas de prioridad, listas enlazadas, pilas, árboles AVL y tablas hash en un sistema con interfaz gráfica funcional.

---

## 🧱 Estructuras de Datos Implementadas

Todas las estructuras están implementadas **desde cero en C++**, sin usar `std::priority_queue`, `std::list`, `std::stack`, `std::map` ni `std::unordered_map`.

| Estructura | Implementación | Uso en el sistema |
|---|---|---|
| ⚙️ **Min-Heap (Cola de Prioridad)** | Árbol binario sobre array | Cola principal de espera de pacientes |
| 🔗 **Lista Doblemente Enlazada** | Nodos con punteros `prev` y `next` | Historial de pacientes atendidos |
| 📚 **Pila (Stack)** | Lista enlazada invertida | Deshacer último registro de paciente |
| 🌳 **Árbol AVL** | Auto-balanceo con rotaciones | Búsqueda de pacientes por nombre o ID |
| #️⃣ **Tabla Hash** | Hash con encadenamiento separado | Acceso O(1) a datos de pacientes activos |

### Complejidades Algorítmicas

| Operación | Min-Heap | Lista Enlazada | Stack | AVL | Hash Table |
|---|---|---|---|---|---|
| Insertar | O(log n) | O(1) | O(1) | O(log n) | O(1) |
| Eliminar | O(log n) | O(n) | O(1) | O(log n) | O(1) |
| Buscar | O(n) | O(n) | — | O(log n) | O(1) |

---

## 🚦 Niveles de Triaje

El sistema implementa el protocolo **Manchester Triage System (MTS)** con 5 niveles de urgencia:

```
🔴  NIVEL 1 — INMEDIATO       Atención al instante      Paro cardíaco, inconsciencia
🟠  NIVEL 2 — MUY URGENTE     Máx. 10 minutos           Dolor torácico, convulsiones
🟡  NIVEL 3 — URGENTE         Máx. 60 minutos           Fractura sin complicaciones
🟢  NIVEL 4 — POCO URGENTE    Máx. 120 minutos          Fiebre leve, herida pequeña
⚪  NIVEL 5 — NO URGENTE      Máx. 240 minutos          Chequeo rutinario, gripe leve
```

El Min-Heap garantiza que siempre se atienda primero al paciente con mayor urgencia. En caso de empate de nivel, se prioriza al que llegó antes (timestamp de llegada).

---

## ✨ Características

- ✅ Registro de pacientes con datos completos (nombre, edad, síntomas, nivel)
- ✅ Cola de espera en tiempo real ordenada por prioridad con colores por nivel
- ✅ Botón "Atender siguiente" que extrae el paciente más urgente del heap
- ✅ Historial completo de pacientes atendidos (lista doblemente enlazada)
- ✅ Deshacer último registro con un clic (stack de operaciones)
- ✅ Buscador de pacientes por nombre o ID (árbol AVL)
- ✅ Acceso instantáneo a fichas de pacientes activos (tabla hash)
- ✅ Panel de estadísticas: tiempo promedio de espera, pacientes por nivel
- ✅ Persistencia de datos con SQLite (sesiones guardadas)
- ✅ Interfaz gráfica con Qt 6, colores intuitivos y diseño limpio

---

## 📁 Estructura del Proyecto

```
MediTriage/
│
├── 📄 main.cpp                        ← Punto de entrada de la aplicación
├── 📄 MediTriage.pro                  ← Archivo de proyecto Qt
│
├── 📂 structures/                     ← Estructuras de datos implementadas desde cero
│   ├── PriorityQueue.h / .cpp         ← Min-Heap para cola de pacientes
│   ├── LinkedList.h / .cpp            ← Lista doblemente enlazada para historial
│   ├── Stack.h / .cpp                 ← Pila para operación deshacer
│   ├── AVLTree.h / .cpp               ← Árbol AVL para búsqueda eficiente
│   └── HashTable.h / .cpp             ← Tabla hash para acceso rápido
│
├── 📂 models/                         ← Modelos de dominio
│   └── Patient.h / .cpp               ← Clase paciente con todos sus atributos
│
├── 📂 ui/                             ← Interfaz gráfica (Qt Widgets)
│   ├── MainWindow.h / .cpp            ← Ventana principal con layout general
│   ├── RegisterForm.h / .cpp          ← Formulario de registro de paciente
│   ├── WaitingQueuePanel.h / .cpp     ← Panel de cola de espera en vivo
│   ├── HistoryPanel.h / .cpp          ← Panel de historial de atendidos
│   ├── SearchPanel.h / .cpp           ← Panel de búsqueda de pacientes
│   └── StatsPanel.h / .cpp            ← Panel de estadísticas del sistema
│
├── 📂 database/                       ← Capa de persistencia
│   └── DatabaseManager.h / .cpp       ← Manejo de SQLite con Qt SQL
│
├── 📂 assets/                         ← Recursos visuales
│   ├── icons/                         ← Íconos de la interfaz
│   └── styles/                        ← Archivos QSS (estilos de la GUI)
│
├── 📂 tests/                          ← Pruebas unitarias por estructura
│   ├── test_priority_queue.cpp
│   ├── test_linked_list.cpp
│   ├── test_avl_tree.cpp
│   └── test_hash_table.cpp
│
└── 📄 README.md
```

---

## ⚙️ Requisitos Previos

Antes de compilar el proyecto, asegúrate de tener instalado:

| Herramienta | Versión mínima | Descarga |
|---|---|---|
| Qt Framework | 6.x | [qt.io/download](https://www.qt.io/download) |
| Qt Creator (IDE) | 11.x | Incluido con Qt |
| Compilador C++ | C++17 compatible (GCC 9+, MSVC 2019+, Clang 10+) | Según SO |
| SQLite | 3.x | Incluido con Qt SQL |

---

## 🚀 Instalación y Ejecución

### Clonar el repositorio

```bash
git clone https://github.com/Kyogami20/Hospital-Triage.git
cd meditriage
```

### Opción A — Qt Creator (Recomendado)

```
1. Abrir Qt Creator
2. File → Open Project → seleccionar MediTriage.pro
3. Configurar el kit de compilación (Qt 6 + compilador)
4. Presionar ▶ Run (Ctrl + R)
```

### Opción B — Línea de comandos

```bash
# En Linux / macOS
mkdir build && cd build
qmake ../MediTriage.pro
make -j4
./MediTriage

# En Windows (con MinGW)
mkdir build && cd build
qmake ..\MediTriage.pro
mingw32-make
MediTriage.exe
```

---

## 🖥️ Uso del Sistema

```
┌─────────────────────────────────────────────────────┐
│                   MEDITRIAGE SYSTEM                  │
├──────────────────┬──────────────────────────────────┤
│  [+ Nuevo        │  Cola de Espera (Min-Heap)        │
│    Paciente]     │  ─────────────────────────────    │
│                  │  🔴 #001 - Juan Pérez   [ATENDER] │
│  [↩ Deshacer]   │  🟠 #002 - Ana López              │
│                  │  🟡 #003 - Pedro Ríos             │
│  [🔍 Buscar]    │  🟢 #004 - María Torres           │
│                  │                                   │
│  [📊 Stats]     ├──────────────────────────────────┤
│                  │  Historial de Atendidos           │
└──────────────────┴──────────────────────────────────┘
```

1. **Registrar paciente** → Completar formulario con nombre, edad, síntomas y asignar nivel de triaje (1–5)
2. **Atender siguiente** → El sistema extrae automáticamente al paciente con mayor urgencia
3. **Deshacer** → Revierte el último registro usando la pila de operaciones
4. **Buscar** → Localiza cualquier paciente activo o del historial por nombre o ID
5. **Estadísticas** → Visualiza métricas del servicio de urgencias en tiempo real

---

## 👥 Integrantes

| Nombre |  Rol |
|---|---|
| [Compañero 1] | [Rol] |
| Quispe Mejia, Ricardo Antonio | [Rol] |
| [Compañero 3] | [Rol] |
| [Compañero 4] | [Rol] |
| [Compañero 5] | [Rol] |
| [Compañero 6] | [Rol] |
| [Compañero 7] | [Rol] |
| [Compañero 8] | [Rol] |

> **Curso:** Estructura de Datos  
> **Docente:** Javier Elmer Cabrera  
> **Universidad:** Universidad Nacional Mayor de San Marcos  
> **Ciclo:** 2026-0

---

<div align="center">

**⭐ Si este proyecto te fue útil, dale una estrella al repositorio ⭐**

*Desarrollado con ❤️ y muchas horas de depuración de punteros*

</div>
