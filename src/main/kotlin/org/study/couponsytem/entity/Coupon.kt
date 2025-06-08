package org.study.couponsytem.entity

import jakarta.persistence.*

@Entity
@Table(name = "coupon")
data class Coupon(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "coupon_id")
    val id: Long = 0,

    @Column(name = "coupon_key", nullable = false, unique = true)
    val couponKey: String,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id")
    val group: CouponGroup,

    @Column(name = "user_id")
    var userId: Long? = null,

    @Column(name = "discount_amount", nullable = false)
    val discountAmount: Int,

    @Column(name = "issued_flag", nullable = false)
    var issued: Boolean = false
)
