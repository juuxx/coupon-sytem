package org.study.couponsytem.service

import org.springframework.stereotype.Service
import org.study.couponsytem.entity.CouponGroup
import org.study.couponsytem.repository.CouponGroupRepository
import org.study.couponsytem.request.CouponLoadCommand
import java.time.LocalDateTime

@Service
class CouponGroupService(
    private val couponGroupRepository: CouponGroupRepository,
) {
    fun exists(groupKey: String) = couponGroupRepository.existsByGroupKey(groupKey)

    fun createGroup(cmd: CouponLoadCommand): CouponGroup {
        val now = LocalDateTime.now()
        return couponGroupRepository.save(
            CouponGroup(
                groupKey = cmd.eventId,
                title = cmd.couponPolicyName,
                totalCount = cmd.totalCouponCount,
                startAt = now,
                endAt = now.plusDays(7)
            )
        )
    }
}