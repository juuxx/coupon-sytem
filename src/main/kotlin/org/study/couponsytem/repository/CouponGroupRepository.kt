package org.study.couponsytem.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import org.study.couponsytem.entity.Coupon
import org.study.couponsytem.entity.CouponGroup

@Repository
interface CouponGroupRepository : JpaRepository<CouponGroup, Long> {
    fun existsByGroupKey(groupKey: String): Boolean
}
