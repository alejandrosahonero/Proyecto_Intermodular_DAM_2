package com.alejandrosahonero.courthub.domain.usecase.access

import kotlin.random.Random

class GenerateAccessCodeUseCase {
    operator fun invoke(): String =
        Random.nextInt(100000, 999999).toString()
}