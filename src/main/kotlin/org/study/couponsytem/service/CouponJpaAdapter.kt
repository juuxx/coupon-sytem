package org.study.couponsytem.service

import org.springframework.stereotype.Component
import org.study.couponsytem.entity.Coupon
import org.study.couponsytem.repository.CouponRepository


@Component
class CouponJpaAdapter(
    private val couponRepository: CouponRepository
) : CouponPersistencePort {
    override fun saveAll(coupons: List<Coupon>): List<Coupon> = couponRepository.saveAll(coupons)
}