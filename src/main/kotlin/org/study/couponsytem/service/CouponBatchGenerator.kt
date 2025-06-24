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
    /**
     * @param group        쿠폰이 속한 그룹 (이벤트 단위 등)
     * @param policyMap    <할인금액, 상세정책> Map (할당 비율 포함)
     * @param total        전체 생성할 쿠폰 수
     * @return             생성된 쿠폰 리스트
     */
    fun generate(
        group: CouponGroup,
        policyMap: Map<Int, CouponPolicyDetail>,
        total: Int
    ): List<Coupon> {
        val temp = mutableListOf<Pair<String, CouponPolicyDetail>>() // 쿠폰키와 정책 매핑을 임시 저장할 리스트

        // 1️⃣ 각 할인 정책에 따라 비율만큼 쿠폰 생성
        policyMap.values.forEach { pd ->
            val count = (total * (pd.allocationPercentage / 100.0)).toInt()  // 예: 30%면 100개 중 30개
            repeat(count) {
                val key = keyGen.generate(group.groupKey.uppercase())  // 유니크한 쿠폰 키 생성
                temp += key to pd  // 쿠폰 키와 정책을 쌍으로 저장
            }
        }

        // 2️⃣ 전체 쿠폰을 셔플하여 무작위 순서로 발급되도록 준비
        temp.shuffle()

        // 3️⃣ 최종 쿠폰 엔티티로 변환하여 반환
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