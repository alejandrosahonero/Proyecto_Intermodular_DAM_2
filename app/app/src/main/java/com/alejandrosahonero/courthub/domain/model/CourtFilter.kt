package com.alejandrosahonero.courthub.domain.model

enum class CourtFilter(val label: String) {
    ALL("Todas"),
    FAVORITES("Favoritos"),
    PRICE_ASC("Precio ↑"),
    PRICE_DESC("Precio ↓"),
    AVAILABLE("Disponibles"),
    PADEL("Pádel"),
    FUTBOL("Fútbol"),
    TENIS("Tenis"),
    CRISTAL("Cristal")
}
