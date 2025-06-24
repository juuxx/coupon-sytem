package org.study.couponsytem.entity

import jakarta.persistence.*

@Entity
@Table(name = "coupon_policy_detail")
class CouponPolicyDetail(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    // CouponGroup과의 관계 설정: N:1 (여러 CouponPolicyDetail이 하나의 CouponGroup에 속함)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false) // 외래 키 컬럼 이름 지정
    var couponGroup: CouponGroup,

    // 할인 타입: "PERCENT" (비율 할인) 또는 "AMOUNT" (금액 할인)
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    var discountType: DiscountType,

    // 할인 값:
    // discountType이 "PERCENT"일 경우 10, 20, 50 등 퍼센트 값
    // discountType이 "AMOUNT"일 경우 1000, 5000, 10000 등 금액 값
    @Column(nullable = false)
    var discountValue: Int,

    // 총 쿠폰 발행 수량 중 이 특정 정책에 할당될 비율 (예: 20 -> 20%)
    // 모든 CouponPolicyDetail의 allocationPercentage 합계는 100이 되어야 함
    @Column(nullable = false)
    var allocationPercentage: Int
) {

}