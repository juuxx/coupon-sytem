package org.study.couponsytem.service

import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component
import org.study.couponsytem.entity.Coupon

@Component
class RedisCouponQueueAdapter(
    private val redis: StringRedisTemplate
) : CouponQueuePort {
    override fun push(eventId: String, coupons: List<Coupon>) {
        val key = "couponQueue:$eventId"
        redis.executePipelined {
            val ops = redis.opsForList()
            coupons.forEach { c -> ops.rightPush(key, "${c.couponKey}:${c.discountAmount}") }
            null
        }
    }
}