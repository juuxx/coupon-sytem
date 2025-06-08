package org.study.couponsytem.kafka.event

import java.time.LocalDateTime

data class CouponIssuedEvent(
    val eventId: String,
    val userId: String,
    val couponKey: String,
    val issuedAt: String = LocalDateTime.now().toString()
)
