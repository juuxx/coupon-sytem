package org.study.couponsytem.request

import org.study.couponsytem.entity.DiscountType

data class CouponLoadCommand(
    val eventId: String,
    val couponPolicyName: String,
    val totalCouponCount: Int,
    val discountPolicies: List<DiscountPolicyCreate>
)

data class DiscountPolicyCreate(
    val type: DiscountType,
    val value: Int,
    val allocationPercentage: Int
)
