package org.study.couponsytem.entity

import org.springframework.stereotype.Component

@Component
class CouponKeyGenerator {
    private val allowedChars = ('A'..'Z') + ('2'..'9')

    fun generate(prefix: String = "", length: Int = 6): String {
        val body = (1..length)
            .map { allowedChars.random() }
            .joinToString("")
        return if (prefix.isNotBlank()) "$prefix-$body" else body
    }
}