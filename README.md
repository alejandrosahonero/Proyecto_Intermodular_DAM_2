# Gestión Integral de Reservas

[cite_start]Este proyecto es una aplicación móvil nativa para Android desarrollada en **Kotlin**[cite: 32, 77]. [cite_start]Su propósito es automatizar la gestión de reservas de pistas, la administración de usuarios y el registro de pagos, garantizando la integridad de los datos y la prevención de conflictos de horarios[cite: 32].

---

## Stack Tecnológico

* [cite_start]**Lenguaje:** Kotlin[cite: 77].
* [cite_start]**UI:** Jetpack Compose (Material Design 3)[cite: 37, 79].
* [cite_start]**Arquitectura:** MVVM con Clean Architecture[cite: 68, 78].
* [cite_start]**Backend:** Firebase (Real-time DB & Auth)[cite: 38, 40, 60, 81].
* [cite_start]**Pagos:** Stripe Sandbox[cite: 42, 51, 57].
* [cite_start]**Notificaciones:** Firebase Cloud Messaging[cite: 41, 52, 67, 82].

---

## Arquitectura (MVVM)

* [cite_start]**Model:** Gestión de datos y lógica de negocio con Firebase y persistencia local[cite: 69].
* [cite_start]**View:** Interfaz de usuario reactiva en Jetpack Compose[cite: 70].
* [cite_start]**ViewModel:** Puente entre datos y UI, permitiendo pruebas unitarias aisladas[cite: 73, 75].

---

## Funcionalidades

### Módulo de Usuario
* [cite_start]**Autonomía:** Visualización de disponibilidad en tiempo real[cite: 18, 22, 46].
* [cite_start]**Pagos:** Transacciones seguras y simuladas mediante Stripe[cite: 42, 51].
* [cite_start]**Notificaciones:** Confirmaciones y recordatorios vía Push[cite: 52].

### Módulo de Administración Móvil
* [cite_start]**Gestión "a pie de campo":** Diseñado para administradores que operan fuera de una oficina[cite: 33, 34, 50].
* [cite_start]**Control de Pistas:** Bloqueo por mantenimiento y validación de entradas[cite: 35, 55].

---

## Planificación (50 Horas)

| Semana | Actividad Principal | Hito Técnico |
| :--- | :--- | :--- |
| 1 | Análisis y Diseño UI | [cite_start]Mockups Figma [cite: 93] |
| 2 | Setup MVVM y Auth | [cite_start]Login/Registro [cite: 93] |
| 3 | CRUD y Persistencia | [cite_start]DB Híbrida [cite: 93] |
| 4 | Lógica de Conflictos | [cite_start]Algoritmo de validación [cite: 93] |
| 5 | Stripe y Push | [cite_start]Flujo de pago completo [cite: 93] |
| 6 | Testing y Cierre | [cite_start]APK estable y Memoria [cite: 93] |
