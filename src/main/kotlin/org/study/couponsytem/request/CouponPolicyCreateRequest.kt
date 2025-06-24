package org.study.couponsytem.request

import org.study.couponsytem.entity.DiscountType

data class CouponPolicyCreateRequest(
    val eventId: String, // 이벤트 식별자 (예: "3000EVENT")
    val couponPolicyName: String, // 해당 쿠폰 발급 정책의 이름 (관리용)
    val totalCouponCount: Int, // 총 발급할 쿠폰 수량
    val discountPolicies: List<DiscountPolicyDetail> // 다양한 할인 정책들을 정의
)

data class DiscountPolicyDetail(
    val type: DiscountType, // 할인 타입 (예: PERCENT, AMOUNT)
    val value: Int, // 할인 값 (예: 50 for 50%, 5000 for 5000원)
    val allocationPercentage: Int // totalCouponCount 대비 해당 할인 정책이 할당될 비율 (%)
)

