package org.study.couponsytem.service

import org.study.couponsytem.entity.Coupon

interface CouponPersistencePort {
    fun saveAll(coupons: List<Coupon>): List<Coupon>
}