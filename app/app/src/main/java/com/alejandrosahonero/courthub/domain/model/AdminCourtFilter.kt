package com.alejandrosahonero.courthub.domain.model

enum class AdminCourtFilter(val label: String) {
    ALL("Todas"),
    ENABLED("Habilitadas"),
    DISABLED("En mantenimiento"),
    PRICE_ASC("Precio ↑"),
    PRICE_DESC("Precio ↓"),
    PADEL("Pádel"),
    FUTBOL("Fútbol"),
    TENIS("Tenis"),
    CRISTAL("Cristal")
}
