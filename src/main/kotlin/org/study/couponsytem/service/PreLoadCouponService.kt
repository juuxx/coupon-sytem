package org.study.couponsytem.service

import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.study.couponsytem.entity.Coupon
import org.study.couponsytem.entity.CouponGroup
import org.study.couponsytem.entity.CouponKeyGenerator
import org.study.couponsytem.repository.CouponGroupRepository
import org.study.couponsytem.repository.CouponRepository
import org.study.couponsytem.request.CouponLoadRequest
import org.study.couponsytem.response.CouponLoadMessage
import org.study.couponsytem.response.CouponLoadResponse
import java.time.LocalDateTime

@Service
class PreLoadCouponService(
    private val couponKeyGenerator: CouponKeyGenerator,
    private val couponGroupRepository: CouponGroupRepository,
    private val couponRepository: CouponRepository,
    private val redisTemplate: StringRedisTemplate
) {
    fun preload(request: CouponLoadRequest): CouponLoadResponse {

        val now = LocalDateTime.now()

        //Group key 중복 확인
        if (couponGroupRepository.existsByGroupKey(request.eventId)) {
            return CouponLoadResponse(success = false, message = CouponLoadMessage.EXIST_GROUP_KEY)
        }

        // 1. 쿠폰 그룹 생성
        val group = couponGroupRepository.save(
            CouponGroup(
                groupKey = request.eventId,
                title = "${request.eventId.uppercase()} 쿠폰 이벤트",
                totalCount = request.count,
                startAt = now,  // 일단 시작일 now.. 나중엔 시간을 받자
                endAt = now.plusDays(7)
            )
        )

        // 2. 쿠폰 키 생성 (중복 제거)
        val generatedKeys = mutableSetOf<String>()
        while (generatedKeys.size < request.count) {
            generatedKeys += couponKeyGenerator.generate(request.eventId.uppercase())
        }
        
        // 3. 쿠폰 entity 생성
        val coupons = generatedKeys.map { key ->
            Coupon(
                couponKey = key,
                group = group,
                discountAmount = 1000
            )
        }

        // 4. DB 저장
        couponRepository.saveAll(coupons)

        // 5. Redis 적재
        val redisKey = "couponQueue:${request.eventId}"
        redisTemplate.opsForList().leftPushAll(redisKey, *generatedKeys.toTypedArray())


        val message = String.format(CouponLoadMessage.SUCCESS, request.count)
        return CouponLoadResponse(success = true, message = message)

    }

}
