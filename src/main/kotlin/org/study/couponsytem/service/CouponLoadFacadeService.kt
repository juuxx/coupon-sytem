package org.study.couponsytem.service

import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import org.study.couponsytem.request.CouponLoadCommand
import org.study.couponsytem.response.CouponLoadMessage
import org.study.couponsytem.response.CouponLoadResponse

@Service
class CouponLoadFacadeService(
    private val couponGroupService: CouponGroupService,
    private val policyDetailService: PolicyDetailService,
    private val generator: CouponBatchGenerator,
    private val persistence: CouponPersistencePort,
    private val queuePort: CouponQueuePort,
) {

    @Transactional
    fun preload(cmd: CouponLoadCommand): CouponLoadResponse {
        if (couponGroupService.exists(cmd.eventId))
            return CouponLoadResponse(false, CouponLoadMessage.EXIST_GROUP_KEY)

        val group = couponGroupService.createGroup(cmd)
        val policyMap = policyDetailService.savePolicies(group, cmd.discountPolicies)

        val coupons = generator.generate(group, policyMap, cmd.totalCouponCount)
        persistence.saveAll(coupons)
        queuePort.push(group.groupKey, coupons)

        val msg = String.format(CouponLoadMessage.SUCCESS, coupons.size)
        return CouponLoadResponse(true, msg)
    }
}