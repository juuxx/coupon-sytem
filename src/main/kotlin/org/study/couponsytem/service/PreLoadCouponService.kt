package org.study.couponsytem.service

import jakarta.transaction.Transactional
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Service
import org.study.couponsytem.entity.*
import org.study.couponsytem.repository.CouponGroupRepository
import org.study.couponsytem.repository.CouponPolicyDetailRepository
import org.study.couponsytem.repository.CouponRepository
import org.study.couponsytem.request.CouponPolicyCreateRequest
import org.study.couponsytem.response.CouponLoadMessage
import org.study.couponsytem.response.CouponLoadResponse
import java.time.LocalDateTime

@Service
class PreLoadCouponService(
    private val couponKeyGenerator: CouponKeyGenerator,
    private val couponGroupRepository: CouponGroupRepository,
    private val couponRepository: CouponRepository,
    private val couponPolicyDetailRepository: CouponPolicyDetailRepository,
    private val redisTemplate: StringRedisTemplate
) {

    @Transactional
    fun preload(request: CouponPolicyCreateRequest): CouponLoadResponse {
        val now = LocalDateTime.now()

        // Group key 중복 확인
        if (couponGroupRepository.existsByGroupKey(request.eventId)) {
            return CouponLoadResponse(success = false, message = CouponLoadMessage.EXIST_GROUP_KEY)
        }

        // 1. 쿠폰 그룹 생성 (기존 로직과 유사)
        val couponGroup = couponGroupRepository.save(
            CouponGroup(
                groupKey = request.eventId,
                title = request.couponPolicyName, // 요청의 policyName을 제목으로 사용
                totalCount = request.totalCouponCount,
                startAt = now,
                endAt = now.plusDays(7) // 추후 request에서 받을 수 있도록 수정 필요
            )
        )

        // 2. 할인 정책 세부 정보 저장 및 Map으로 구성
        // 각 DiscountPolicyDetail을 DB에 저장하고 ID를 부여받음
        // <할인율/금액: DiscountPolicyDetail 엔티티> 맵으로 관리하여 나중에 매핑
        val policyDetailsMap = mutableMapOf<Int, CouponPolicyDetail>() // value -> CouponPolicyDetail 엔티티
        val amountPoliciesMap = mutableMapOf<Int, CouponPolicyDetail>() // 금액 할인용 (만약 금액 할인이 있다면)

        val couponPolicyDetailsToSave = request.discountPolicies.map { detailRequest ->
            val policyDetail = CouponPolicyDetail(
                couponGroup = couponGroup,
                discountType = detailRequest.type,
                discountValue = detailRequest.value,
                allocationPercentage = detailRequest.allocationPercentage
            )
            // 임시로 id를 생성하여 맵에 저장 (실제 저장은 후에 Batch Insert 고려)
            if (detailRequest.type == DiscountType.PERCENT) {
                policyDetailsMap[detailRequest.value] = policyDetail
            } else if (detailRequest.type == DiscountType.AMOUNT) {
                amountPoliciesMap[detailRequest.value] = policyDetail
            }
            policyDetail
        }
        // DB에 CouponPolicyDetail 일괄 저장 (ID가 부여됨)
        val savedPolicyDetails = couponPolicyDetailRepository.saveAll(couponPolicyDetailsToSave)
        // 실제 저장된 엔티티로 맵 업데이트
        val finalPolicyDetailsMap = savedPolicyDetails.associateBy { it.discountValue } // 단순화를 위해 discountValue로 매핑

        // 3. 쿠폰 entity 생성 및 Redis 적재 데이터 준비
        val couponsToSave = mutableListOf<Coupon>()
        val redisCouponData = mutableSetOf<String>() // Redis Set에 저장할 "couponKey:policyDetailId" 형식의 데이터

        // 각 할인 정책별로 할당될 쿠폰 수를 계산하고, 해당 정책 ID와 매핑하여 리스트에 추가
        val tempCouponListForShuffle = mutableListOf<Pair<String, Long>>() // Pair<couponKey, policyDetailId>

        for (policyRequest in request.discountPolicies) {
            val countForPolicy = (request.totalCouponCount * (policyRequest.allocationPercentage / 100.0)).toInt()
            val policyDetail = finalPolicyDetailsMap[policyRequest.value] // 저장된 정책 엔티티 가져오기

            if (policyDetail == null) {
                // 정책 세부 정보를 찾지 못한 경우 오류 처리 또는 스킵
                return CouponLoadResponse(success = false, message = "할인 정책 세부 정보가 유효하지 않습니다.")
            }

            repeat(countForPolicy) {
                val couponKey = couponKeyGenerator.generate(request.eventId.uppercase()) // 고유 쿠폰 키 생성
                tempCouponListForShuffle.add(Pair(couponKey, policyDetail.id!!)) // 저장된 policyDetail의 ID 사용
            }
        }

        // 4. 쿠폰을 랜덤으로 섞기
        // 이렇게 하면 Redis Set에 추가될 때 이미 랜덤한 순서가 됨
        tempCouponListForShuffle.shuffle()

        // 5. DB에 저장할 Coupon 엔티티 생성 및 Redis 데이터 구성
        for ((couponKey, policyDetailId) in tempCouponListForShuffle) {
            val policyDetail = savedPolicyDetails.first { it.id == policyDetailId } // 다시 policyDetail 엔티티를 찾음
            couponsToSave.add(
                Coupon(
                    couponKey = couponKey,
                    group = couponGroup,
                    discountType = policyDetail.discountType, // 타입 저장
                    discountAmount = policyDetail.discountValue // 할인 금액/퍼센트 저장
                )
            )
            redisCouponData.add("$couponKey:${policyDetailId}") // Redis에 저장할 "쿠폰키:정책ID" 형식
        }

        // 6. DB 저장 (Coupon 엔티티)
        couponRepository.saveAll(couponsToSave)

//        val redisKey = "couponQueue:${request.eventId}"
//        // 7. Redis Set에 적재 (랜덤으로 가져갈 수 있도록 Set 사용)
//        redisTemplate.opsForSet().add(redisKey, *redisCouponData.toTypedArray())

        val redisKey = "couponQueue:${request.eventId}"
        val couponsToPush = mutableListOf<String>()

        request.discountPolicies.forEach { policy ->
            val count = (request.totalCouponCount * (policy.allocationPercentage / 100.0)).toInt()
            val policyDetail = savedPolicyDetails.first { it.discountValue == policy.value }

            repeat(count) {
                val couponKey = couponKeyGenerator.generate(request.eventId.uppercase())

                // ✅ Redis 저장용: 쿠폰키:할인율 (예: ABC123:50)
                couponsToPush += "$couponKey:${policyDetail.discountValue}"

                // ✅ DB 저장용: 할인율 정보 따로
                couponsToSave += Coupon(
                    couponKey = couponKey,
                    group = couponGroup,
                    discountType = policyDetail.discountType,
                    discountAmount = policyDetail.discountValue
                )
            }
        }

        couponsToPush.shuffle()

        // ✅ Redis에 rightPushAll
        redisTemplate.opsForList().rightPushAll(redisKey, *couponsToPush.toTypedArray())

        val message = String.format(CouponLoadMessage.SUCCESS, request.totalCouponCount)
        return CouponLoadResponse(success = true, message = message)
    }
}
