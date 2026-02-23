# MediTriage — Sistema Profesional de Triaje Hospitalario

Aplicación de escritorio Java 17 + JavaFX que implementa un sistema de triaje
hospitalario real, con **todas las estructuras de datos implementadas desde cero**
(sin `java.util` collections) y persistencia en **MySQL**.

---

## Requisitos

| Herramienta | Versión mínima |
|---|---|
| Java JDK | 17 |
| Apache Maven | 3.8+ |
| MySQL Server | 8.0+ |
| (Opcional) MySQL Workbench | cualquier versión |

---

## 1. Crear la base de datos

Ejecuta el script SQL incluido en el proyecto:

```sql
-- Opción A: desde terminal MySQL
mysql -u root -p < sql/schema.sql

-- Opción B: pegar en MySQL Workbench / DBeaver
-- Abre sql/schema.sql y ejecútalo
```

El script crea:
- `meditriage_db` (base de datos, charset utf8mb4)
- `patients` (tabla con índices optimizados)

---

## 2. Configurar credenciales

Edita el archivo:

```
src/main/resources/app.properties
```

```properties
db.url=jdbc:mysql://localhost:3306/meditriage_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
db.user=root
db.password=TU_CONTRASEÑA
```

Cambia `db.user` y `db.password` según tu instalación de MySQL.

---

## 3. Compilar y ejecutar

```bash
# Desde la carpeta raíz del proyecto (donde está pom.xml)
mvn clean javafx:run
```

O en dos pasos:

```bash
mvn clean package
mvn javafx:run
```

---

## 4. Arquitectura del proyecto

```
src/main/java/com/meditriage/
├── App.java                          ← Punto de entrada (JavaFX Application)
│
├── model/
│   ├── Patient.java                  ← Entidad de dominio
│   └── UndoAction.java               ← Acción deshaciable (REGISTER / ATTEND)
│
├── structures/                       ← ★ Estructuras implementadas desde cero
│   ├── MinHeap.java                  ← Cola de prioridad (arreglo nativo)
│   ├── AVLTree.java                  ← Árbol AVL genérico (K extends Comparable)
│   ├── HashTable.java                ← Hash con encadenamiento separado
│   ├── DoublyLinkedList.java         ← Lista doblemente enlazada genérica
│   └── Stack.java                    ← Pila (arreglo nativo)
│
├── database/
│   ├── DatabaseConnection.java       ← Conexión JDBC (lee app.properties)
│   └── PatientDAO.java               ← CRUD completo (PreparedStatement)
│
├── service/
│   ├── TriageService.java            ← Singleton: cerebro del sistema
│   └── TriageStats.java              ← DTO de métricas
│
└── controller/
    ├── Refreshable.java              ← Interfaz para refresh de vistas
    ├── MainController.java           ← Navegación lateral + carga dinámica FXML
    ├── DashboardController.java      ← KPIs + simulador de pacientes
    ├── RegisterController.java       ← Formulario de registro
    ├── QueueController.java          ← Cola prioritaria + atender + deshacer
    ├── SearchController.java         ← Búsqueda por ID (AVL) / nombre (AVL)
    └── HistoryController.java        ← Historial con filtros + exportar CSV
```

---

## 5. Estructuras de datos implementadas

### MinHeap — Cola de Prioridad
- **Criterio**: nivel más bajo = más urgente; desempate por `arrivalAt` más antiguo.
- `push(Patient)` → O(log n)
- `pop()` → O(log n) — extrae el más urgente
- `peek()` → O(1)
- `removeById(int)` → O(n) búsqueda + O(log n) heapify (para undo)
- `getSortedSnapshot()` → copia + heap-sort propio, no modifica el original

### AVLTree<K extends Comparable<K>, V>
- Dos instancias: `AVLTree<Integer, Patient>` (por ID) y `AVLTree<String, Patient>` (por nombre).
- Rotaciones: LL, RR, LR (doble), RL (doble).
- `insert` / `search` / `delete` → O(log n)
- `collectAllInto(DoublyLinkedList)` → traversal in-order para búsquedas tipo "contiene"

### HashTable<K, V> — Encadenamiento Separado
- Buckets: arreglo de `Node<K,V>` (lista enlazada simple interna).
- Hash con mezcla de bits (Knuth multiplicative).
- `put` / `get` / `remove` / `containsKey` → O(1) promedio
- Redimensionamiento automático (threshold 0.75)
- Métricas: `loadFactor()`, `collisionsCount()`

### DoublyLinkedList<T>
- Nodos dobles con `prev` / `next`.
- Capacidad máxima configurable (historial últimos N atendidos).
- `addLast` / `addFirst` / `removeFirst` / `removeLast` → O(1)
- `removeElement(T)` → O(n) — para undo de ATTEND
- `filter(SimpleFilter<T>)` — interfaz funcional propia (sin `Predicate` de `java.util`)

### Stack<T>
- Arreglo nativo con redimensionamiento dinámico.
- `push` / `pop` / `peek` / `isEmpty` / `size`
- Almacena `UndoAction` (REGISTER o ATTEND) para deshacer coherentemente.

---

## 6. Lógica de Undo

| Acción | Undo |
|---|---|
| **REGISTER** | Elimina del heap, hash, ambos AVL y BD (`DELETE`) |
| **ATTEND** | Reinsertar en heap/hash/AVL, quitar de historial, revertir BD (`status=WAITING`, `attended_at=NULL`) |

El stack de undo mantiene orden LIFO; solo se puede deshacer la última acción.

---

## 7. Simulador de pacientes (Dashboard)

- **ON/OFF** mediante `ToggleButton` + `javafx.animation.Timeline` (sin `java.util.Timer`).
- **Intervalo**: slider 1-15 segundos (reconfigurable en caliente).
- **Distribución REALISTA**: N1=5%, N2=15%, N3=30%, N4=30%, N5=20%.
- **Distribución UNIFORME**: 20% cada nivel.
- **Caso crítico manual**: genera siempre un paciente de Nivel 1.

---

## 8. Vistas de la aplicación

| Vista | Descripción |
|---|---|
| Dashboard | KPIs, métricas de estructuras, simulador |
| Registrar | Formulario validado, toast de confirmación |
| Cola Prioritaria | Tabla ordenada por heap, Atender, Deshacer |
| Buscar | Búsqueda exacta (AVL), parcial (traversal) |
| Historial | Filtros nivel/nombre/tiempo, exportar CSV |

---

## Notas académicas

- **Regla cero `java.util`**: ninguna colección (`ArrayList`, `HashMap`, `PriorityQueue`, `Stack`, `LinkedList`, `TreeMap`, `Comparator`) fue utilizada en estructuras de datos o lógica de negocio.
- Se permiten: `String` / `LocalDateTime` (java.lang / java.time), `Math.random()`, `Integer.compare()`, `String.compareTo()`, y utilidades JDBC (`java.sql.*`).
- La lectura de `app.properties` usa `java.util.Properties` únicamente para configuración (no como estructura de datos del sistema).
