package org.study.couponsytem.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "coupon_group")
data class CouponGroup (
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(unique = true)
    val groupKey: String,

    val title: String,
    val totalCount: Int,

    val startAt: LocalDateTime,
    val endAt: LocalDateTime,

    val createdAt: LocalDateTime = LocalDateTime.now()
)