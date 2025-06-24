package org.study.couponsytem.service

import org.springframework.stereotype.Service
import org.study.couponsytem.entity.CouponGroup
import org.study.couponsytem.entity.CouponPolicyDetail
import org.study.couponsytem.repository.CouponPolicyDetailRepository
import org.study.couponsytem.request.DiscountPolicyCreate

@Service
class PolicyDetailService(
    private val couponPolicyDetailRepository: CouponPolicyDetailRepository,
) {
    fun savePolicies(
        group: CouponGroup,
        req: List<DiscountPolicyCreate>
    ): Map<Int, CouponPolicyDetail> {
        val entities = req.map {
            CouponPolicyDetail(
                couponGroup = group,
                discountType = it.type,
                discountValue = it.value,
                allocationPercentage = it.allocationPercentage
            )
        }
        return couponPolicyDetailRepository.saveAll(entities).associateBy { it.discountValue }
    }
}