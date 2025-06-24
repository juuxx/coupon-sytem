package org.study.couponsytem.service

import org.springframework.stereotype.Component
import org.study.couponsytem.entity.Coupon
import org.study.couponsytem.entity.CouponGroup
import org.study.couponsytem.entity.CouponKeyGenerator
import org.study.couponsytem.entity.CouponPolicyDetail

@Component
class CouponBatchGenerator(
    private val keyGen: CouponKeyGenerator
) {
    fun generate(
        group: CouponGroup,
        policyMap: Map<Int, CouponPolicyDetail>,
        total: Int
    ): List<Coupon> {
        val temp = mutableListOf<Pair<String, CouponPolicyDetail>>()

        policyMap.values.forEach { pd ->
            val count = (total * (pd.allocationPercentage / 100.0)).toInt()
            repeat(count) {
                val key = keyGen.generate(group.groupKey.uppercase())
                temp += key to pd
            }
        }

        temp.shuffle()

        return temp.map { (key, pd) ->
            Coupon(
                couponKey = key,
                group = group,
                discountType = pd.discountType,
                discountAmount = pd.discountValue
            )
        }
    }
}