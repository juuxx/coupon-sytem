package org.study.couponsytem.kafka.event

import org.study.couponsytem.entity.DiscountType
import java.time.LocalDateTime

data class CouponIssuedEvent(
    val eventId: String,
    val userId: String,
    val couponKey: String,
    val discountValue: Int,
    val discountType: DiscountType,
    val issuedAt: String = LocalDateTime.now().toString()
)
