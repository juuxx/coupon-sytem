package org.study.couponsytem.service

import org.study.couponsytem.entity.Coupon

interface CouponQueuePort {
    fun push(eventId: String, coupons: List<Coupon>)
}