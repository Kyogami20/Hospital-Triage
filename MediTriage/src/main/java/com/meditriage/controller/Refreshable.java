package com.meditriage.controller;

/**
 * Interfaz que implementan los controladores que necesitan refrescar
 * su estado cuando el usuario navega hacia ellos.
 *
 * MainController llama a refresh() después de cargar cada vista.
 */
public interface Refreshable {
    void refresh();
}
