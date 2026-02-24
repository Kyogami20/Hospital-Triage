<div align="center">

# 🏥 MediTriage System
### Sistema de Triaje Hospitalario con Estructuras de Datos

![Java](https://img.shields.io/badge/Java-17-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![JavaFX](https://img.shields.io/badge/GUI-JavaFX-0078D7?style=for-the-badge&logo=java&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-8.0-4479A1?style=for-the-badge&logo=mysql&logoColor=white)
![Maven](https://img.shields.io/badge/Maven-3.x-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white)
![Status](https://img.shields.io/badge/Estado-En%20Desarrollo-orange?style=for-the-badge)

> **Proyecto Final — Curso de Estructura de Datos**  
> Aplicación de escritorio que simula un sistema de triaje médico hospitalario,  
> implementando estructuras de datos fundamentales desde cero en Java 17 con interfaz gráfica en JavaFX y persistencia en MySQL.

</div>

---

## 🧠 Descripción del Proyecto

**MediTriage System** es una aplicación de escritorio que simula el flujo de atención de urgencias en un hospital, basada en el **Estándar de Triaje de Manchester (MTS)**. El sistema permite registrar pacientes, asignarles un nivel de urgencia y gestionar su atención de forma priorizada usando estructuras de datos implementadas manualmente.

El proyecto fue desarrollado como aplicación práctica del **Curso de Estructura de Datos**, demostrando el uso real y combinado de colas de prioridad, listas enlazadas, pilas, árboles AVL y tablas hash en un sistema con interfaz gráfica funcional.

> ⚠️ **Importante:** Todas las estructuras de datos están implementadas **desde cero en Java**, sin usar `PriorityQueue`, `LinkedList`, `Stack`, `TreeMap` ni `HashMap` de la librería estándar `java.util`.

---

## 🧱 Estructuras de Datos Implementadas

| Estructura | Implementación | Uso en el sistema |
|---|---|---|
| ⚙️ **Min-Heap (Cola de Prioridad)** | Árbol binario sobre array | Cola principal de espera de pacientes |
| 🔗 **Lista Doblemente Enlazada** | Nodos con referencias `prev` y `next` | Historial de pacientes atendidos |
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
- ✅ Persistencia de datos con MySQL (sesiones guardadas)
- ✅ Interfaz gráfica con JavaFX, colores intuitivos y diseño moderno

---

## 📁 Estructura del Proyecto

```
MediTriage/
│
├── 📄 pom.xml                              ← Dependencias Maven (MySQL Connector, etc.)
│
└── 📂 src/
    └── 📂 main/
        └── 📂 java/
            └── 📂 com/meditriage/
                │
                ├── 📄 Main.java                        ← Punto de entrada de la aplicación
                │
                ├── 📂 structures/                      ← Estructuras de datos desde cero
                │   ├── MinHeap.java                    ← Min-Heap para cola de pacientes
                │   ├── DoublyLinkedList.java            ← Lista doblemente enlazada
                │   ├── Stack.java                      ← Pila para operación deshacer
                │   ├── AVLTree.java                    ← Árbol AVL para búsqueda eficiente
                │   └── HashTable.java                  ← Tabla hash para acceso rápido
                │
                ├── 📂 models/                          ← Modelos de dominio
                │   └── Patient.java                    ← Clase paciente con sus atributos
                │
                ├── 📂 ui/                              ← Interfaz gráfica (JavaFX)
                │   ├── MainWindow.java                 ← Ventana principal (Stage/Scene)
                │   ├── RegisterForm.java               ← Formulario de registro (FXML)
                │   ├── WaitingQueuePanel.java           ← Panel cola en vivo (VBox/TableView)
                │   ├── HistoryPanel.java               ← Panel de historial (VBox)
                │   ├── SearchPanel.java                ← Panel de búsqueda (HBox)
                │   └── StatsPanel.java                 ← Panel de estadísticas (Charts)
                │
                ├── 📂 resources/                       ← Recursos de JavaFX
                │   ├── fxml/                           ← Archivos de layout FXML
                │   └── styles/                         ← Archivos CSS para la GUI
                │
                └── 📂 database/                        ← Capa de persistencia
                    ├── DatabaseConnection.java         ← Singleton de conexión MySQL
                    └── PatientDAO.java                 ← Operaciones CRUD de pacientes
```

---

## ⚙️ Requisitos Previos

Antes de ejecutar el proyecto, asegúrate de tener instalado:

| Herramienta | Versión mínima | Descarga |
|---|---|---|
| Java JDK | 17 | [adoptium.net](https://adoptium.net/) |
| JavaFX SDK | 21 | [gluonhq.com/products/javafx](https://gluonhq.com/products/javafx/) |
| Maven | 3.x | [maven.apache.org](https://maven.apache.org/download.cgi) |
| MySQL Server | 8.0 | [mysql.com/downloads](https://dev.mysql.com/downloads/mysql/) |
| MySQL Workbench | 8.x (opcional) | Para visualizar y administrar la BD |

---

## 🗄️ Configuración de la Base de Datos

### 1. Crear la base de datos en MySQL

```sql
CREATE DATABASE meditriage_db;
USE meditriage_db;

CREATE TABLE patients (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    age         INT NOT NULL,
    symptoms    TEXT,
    level       INT NOT NULL CHECK (level BETWEEN 1 AND 5),
    status      ENUM('waiting', 'attended') DEFAULT 'waiting',
    arrival_at  DATETIME DEFAULT CURRENT_TIMESTAMP,
    attended_at DATETIME
);
```

### 2. Configurar la conexión en el proyecto

Edita el archivo `DatabaseConnection.java` con tus credenciales locales:

```java
private static final String URL  = "jdbc:mysql://localhost:3306/meditriage_db";
private static final String USER = "root";            // tu usuario MySQL
private static final String PASS = "tu_contraseña";  // tu contraseña MySQL
```

### 3. Dependencia MySQL en `pom.xml`

```xml
<dependencies>
    <!-- MySQL Connector -->
    <dependency>
        <groupId>com.mysql</groupId>
        <artifactId>mysql-connector-j</artifactId>
        <version>8.3.0</version>
    </dependency>
    <!-- JavaFX Controls -->
    <dependency>
        <groupId>org.openjfx</groupId>
        <artifactId>javafx-controls</artifactId>
        <version>21</version>
    </dependency>
    <!-- JavaFX FXML -->
    <dependency>
        <groupId>org.openjfx</groupId>
        <artifactId>javafx-fxml</artifactId>
        <version>21</version>
    </dependency>
</dependencies>
```

---

## 🚀 Instalación y Ejecución

### Clonar el repositorio

```bash
git clone https://github.com/Kyogami20/Hospital-Triage.git
cd Hospital-Triage
```

### Opción A — IntelliJ IDEA (Recomendado)

```
1. File → Open → seleccionar la carpeta del proyecto
2. IntelliJ detecta el pom.xml automáticamente
3. Esperar a que Maven descargue las dependencias
4. Configurar credenciales MySQL en DatabaseConnection.java
5. Ejecutar Main.java con ▶ Run
```

### Opción B — Línea de comandos con Maven

```bash
# Descargar dependencias y compilar
mvn compile

# Ejecutar la aplicación
mvn exec:java -Dexec.mainClass="com.meditriage.Main"

# O generar JAR ejecutable
mvn package
java -jar target/meditriage-1.0.jar
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

1. **Registrar paciente** → Completar formulario con nombre, edad, síntomas y nivel de triaje (1–5)
2. **Atender siguiente** → Extrae al paciente más urgente del heap y lo registra en MySQL
3. **Deshacer** → Revierte el último registro usando la pila de operaciones
4. **Buscar** → Localiza cualquier paciente por nombre o ID mediante el árbol AVL
5. **Estadísticas** → Visualiza métricas del servicio de urgencias en tiempo real

---

## 👥 Integrantes

| Nombre | Rol |
|---|---|
| [Compañero 1] | [Rol] |
| Quispe Mejia, Ricardo Antonio | [Rol] |
| Palomino Antón, Leonardo David | [Rol] |
| Arias Mandarachi, Bastian | [Rol] |
| De la Cruz Antay, Adrian Avelino | [Rol] |
| Paredes Galvez, Piero Alfonso | [Rol] |
| Quispe Arango, Juan Pablo | [Rol] |
| [Compañero 8] | [Rol] |

> **Curso:** Estructura de Datos  
> **Docente:** Javier Elmer Cabrera  
> **Universidad:** Universidad Nacional Mayor de San Marcos  
> **Ciclo:** 2026-0

---

<div align="center">

**⭐ Si este proyecto te fue útil, dale una estrella al repositorio ⭐**

*Desarrollado con ☕ Java y muchas horas debuggeando NullPointerExceptions*

</div>
