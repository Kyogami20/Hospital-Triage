package com.meditriage.service;

import com.meditriage.database.PatientDAO;
import com.meditriage.model.Patient;
import com.meditriage.model.UndoAction;
import com.meditriage.structures.AVLTree;
import com.meditriage.structures.DoublyLinkedList;
import com.meditriage.structures.HashTable;
import com.meditriage.structures.MinHeap;
import com.meditriage.structures.Stack;

import java.time.LocalDateTime;

/**
 * Cerebro del sistema de triaje.
 *
 * Centraliza toda la lógica de negocio manteniendo coherencia entre:
 *   • MinHeap         — cola de prioridad
 *   • AVLTree (×2)    — índice por ID y por nombre
 *   • HashTable       — acceso O(1) a pacientes activos
 *   • DoublyLinkedList— historial en memoria
 *   • Stack           — pila de undo
 *   • PatientDAO      — persistencia en MySQL
 *
 * La UI nunca manipula estructuras directamente; siempre a través de este servicio.
 * Singleton thread-safe por sincronización simple (aplicación de escritorio).
 */
public class TriageService {

    // Singleton
    private static TriageService instance;

    public static synchronized TriageService getInstance() {
        if (instance == null) instance = new TriageService();
        return instance;
    }

    // Estructuras de datos
    private final MinHeap                      queue;          // cola de prioridad
    private final AVLTree<Integer, Patient>    avlById;        // índice por ID
    private final AVLTree<String,  Patient>    avlByName;      // índice por nombre
    private final HashTable<Integer, Patient>  activePatients; // acceso O(1) por ID
    private final DoublyLinkedList<Patient>    history;        // historial atendidos
    private final Stack<UndoAction>            undoStack;      // pila de deshacer

    // Persistencia
    private final PatientDAO dao;

    // Constructor privado
    private TriageService() {
        queue          = new MinHeap();
        avlById        = new AVLTree<>();
        avlByName      = new AVLTree<>();
        activePatients = new HashTable<>();
        history        = new DoublyLinkedList<>(200); // últimos 200 en memoria
        undoStack      = new Stack<>();
        dao            = new PatientDAO();

        // Cargar pacientes WAITING desde BD al iniciar (por si la app se reinicia)
        loadWaitingFromDb();
    }

    // Registro

    /**
     * Registra un nuevo paciente:
     *   1. Persiste en BD → obtiene ID generado.
     *   2. Inserta en heap, hash y ambos AVL.
     *   3. Empuja acción REGISTER al stack de undo.
     *
     * @return El paciente con el ID asignado por la BD.
     */
    public synchronized Patient registerPatient(String name, int age,
                                                 String symptoms, int level) {
        Patient p = new Patient();
        p.setName(name.trim());
        p.setAge(age);
        p.setSymptoms(symptoms.trim());
        p.setLevel(level);
        p.setStatus("WAITING");
        p.setArrivalAt(LocalDateTime.now());
        p.setCreatedAt(LocalDateTime.now());

        // Persistir y obtener ID
        int generatedId = dao.insertPatient(p);
        if (generatedId <= 0) throw new RuntimeException("Error al persistir paciente en BD");
        p.setId(generatedId);

        // Insertar en estructuras en memoria
        queue.push(p);
        activePatients.put(p.getId(), p);
        avlById.insert(p.getId(), p);
        avlByName.insert(p.getName(), p);

        // Guardar acción para posible undo
        undoStack.push(new UndoAction(UndoAction.ActionType.REGISTER, p));

        return p;
    }

    // Atender

    /**
     * Atiende al siguiente paciente más urgente:
     *   1. Pop del heap.
     *   2. Actualiza estructuras (hash, AVL) → paciente sale de activos.
     *   3. Agrega al historial en memoria.
     *   4. Actualiza BD (status ATTENDED + attended_at).
     *   5. Empuja acción ATTEND al stack de undo.
     *
     * @return El paciente atendido, o null si la cola está vacía.
     */
    public synchronized Patient attendNext() {
        if (queue.isEmpty()) return null;

        Patient p = queue.pop();
        p.setStatus("ATTENDED");
        p.setAttendedAt(LocalDateTime.now());

        // Sacar de activos
        activePatients.remove(p.getId());
        avlById.delete(p.getId());
        avlByName.delete(p.getName());

        // Agregar al historial en memoria
        history.addLast(p);

        // Persistir cambio
        dao.updateStatusToAttended(p.getId(), p.getAttendedAt());

        // Guardar acción para posible undo
        undoStack.push(new UndoAction(UndoAction.ActionType.ATTEND, p));

        return p;
    }

    // Deshacer

    /**
     * Deshace la última acción (REGISTER o ATTEND) de forma coherente.
     *
     * Undo de REGISTER:
     *   → Elimina el paciente del heap, hash, AVL y BD.
     *
     * Undo de ATTEND:
     *   → Revierte el paciente: reinsertar en heap/hash/AVL,
     *     quitar del historial y actualizar BD (status WAITING, attended_at NULL).
     *
     * @return Mensaje descriptivo de la operación realizada.
     */
    public synchronized String undoLastAction() {
        if (undoStack.isEmpty()) return "No hay acciones para deshacer.";

        UndoAction action  = undoStack.pop();
        Patient    patient = action.getPatient();

        if (action.getActionType() == UndoAction.ActionType.REGISTER) {
            // Deshacer REGISTRO
            queue.removeById(patient.getId());
            activePatients.remove(patient.getId());
            avlById.delete(patient.getId());
            avlByName.delete(patient.getName());
            dao.deletePatient(patient.getId());
            return "✓ Registro de «" + patient.getName() + "» (ID " + patient.getId() + ") deshecho.";

        } else {
            // Deshacer ATENCIÓN
            patient.setStatus("WAITING");
            patient.setAttendedAt(null);

            // Reinsertar en estructuras
            queue.push(patient);
            activePatients.put(patient.getId(), patient);
            avlById.insert(patient.getId(), patient);
            avlByName.insert(patient.getName(), patient);

            // Quitar del historial en memoria
            history.removeElement(patient);

            // Revertir en BD
            dao.revertToWaiting(patient.getId());
            return "✓ Atención de «" + patient.getName() + "» (ID " + patient.getId() + ") deshecha.";
        }
    }

    // Búsquedas

    /**
     * Busca paciente activo por ID usando el AVL (O(log n)).
     * Si no está en estructuras en memoria, consulta la BD.
     */
    public Patient searchById(int id) {
        Patient p = avlById.search(id);
        if (p == null) p = dao.findById(id);
        return p;
    }

    /**
     * Busca paciente activo por nombre exacto usando el AVL (O(log n)).
     */
    public Patient searchByNameExact(String name) {
        return avlByName.search(name.trim());
    }

    /**
     * Busca pacientes activos cuyo nombre contenga el texto dado.
     * Realiza traversal in-order del AVL de nombres y filtra.
     * Para búsqueda en BD (incluyendo historial) usa findByNameLike de DAO.
     */
    public Patient[] searchByNameContains(String text) {
        // Recolectar todos los pacientes activos en una lista propia
        DoublyLinkedList<Patient> all = new DoublyLinkedList<>();
        avlByName.collectAllInto(all);

        String lowerText = text.trim().toLowerCase();

        // Filtrar sin java.util: doble pasada sobre DoublyLinkedList
        Patient[] allArr   = all.toArray();
        int count = 0;
        for (Object obj : allArr) {
            Patient p = (Patient) obj;
            if (p.getName().toLowerCase().contains(lowerText)) count++;
        }
        Patient[] result = new Patient[count];
        int idx = 0;
        for (Object obj : allArr) {
            Patient p = (Patient) obj;
            if (p.getName().toLowerCase().contains(lowerText)) result[idx++] = p;
        }

        // Si no encontramos nada en memoria, buscar en BD
        if (count == 0) return dao.findByNameLike(text);
        return result;
    }

    // Snapshots para UI

    /**
     * Retorna los pacientes de la cola ordenados por prioridad,
     * sin modificar el heap original.
     */
    public Patient[] getQueueSnapshotSorted() {
        return queue.getSortedSnapshot();
    }

    /**
     * Retorna todos los pacientes del historial en memoria (más reciente al final).
     */
    public Patient[] getHistorySnapshot() {
        Object[] raw    = history.toArray();
        Patient[] result = new Patient[raw.length];
        for (int i = 0; i < raw.length; i++) result[i] = (Patient) raw[i];
        return result;
    }

    /** Retorna el paciente más urgente sin extraerlo del heap. */
    public Patient peekNext() {
        return queue.peek();
    }

    // Estadísticas

    /**
     * Construye y retorna el DTO de métricas del sistema.
     * Las métricas de BD (attendedToday, avgWait) se consultan con una sola
     * query adicional por llamada; no son costosas con índices correctos.
     */
    public TriageStats getStats() {
        return new TriageStats(
            queue.size(),
            dao.countAttendedToday(),
            dao.avgWaitMinutesToday(),
            undoStack.size(),
            activePatients.size(),
            activePatients.loadFactor(),
            activePatients.collisionsCount(),
            avlById.height(),
            avlByName.height(),
            history.size()
        );
    }

    /** Expone el PatientDAO para consultas extendidas del HistoryController. */
    public PatientDAO getDao() { return dao; }

    // Carga inicial

    /**
     * Al iniciar la aplicación, carga en memoria los pacientes WAITING de la BD
     * (por si la app se cerró y se reinicia).
     */
    private void loadWaitingFromDb() {
        Patient[] waiting = dao.listWaiting();
        for (Patient p : waiting) {
            queue.push(p);
            activePatients.put(p.getId(), p);
            avlById.insert(p.getId(), p);
            avlByName.insert(p.getName(), p);
        }
        System.out.println("[TriageService] Cargados " + waiting.length + " pacientes WAITING desde BD.");
    }
}
