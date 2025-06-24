package org.study.couponsytem.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.study.couponsytem.entity.CouponPolicyDetail

interface CouponPolicyDetailRepository : JpaRepository<CouponPolicyDetail, Long> {
    fun findByCouponGroup_IdAndDiscountValue(id: Long, discountRate: Int): CouponPolicyDetail?
}