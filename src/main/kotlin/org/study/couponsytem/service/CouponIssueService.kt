package org.study.couponsytem.service

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.core.io.ClassPathResource
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.data.redis.core.script.DefaultRedisScript
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.scripting.support.ResourceScriptSource
import org.springframework.stereotype.Service
import org.study.couponsytem.entity.DiscountType
import org.study.couponsytem.kafka.event.CouponIssuedEvent
import org.study.couponsytem.request.CouponIssueRequest
import org.study.couponsytem.request.CouponIssueResponse

@Service
class CouponIssueService(
    private val redisTemplate: StringRedisTemplate,
    private val kafkaTemplate: KafkaTemplate<String, CouponIssuedEvent>
) {

    private val objectMapper = ObjectMapper()

    private val issueScript = DefaultRedisScript<String>().apply {
        setScriptSource(ResourceScriptSource(ClassPathResource("redis/lua/issue_coupon.lua")))
        resultType = String::class.java
    }

    fun issue(request: CouponIssueRequest): CouponIssueResponse {
        val eventId = request.eventId
        val userId = request.userId
        val redisQueueKey = "couponQueue:$eventId"
        val redisIssuedKey = "coupon:issued:$eventId"

        val result = redisTemplate.execute(
            issueScript,
            listOf(redisQueueKey, redisIssuedKey),
            userId
        )

        return when (result) {
            "DUPLICATE" -> CouponIssueResponse(false, "DUPLICATE", null, "이미 쿠폰을 발급받았습니다.")
            "SOLD_OUT" -> CouponIssueResponse(false, "SOLD_OUT", null, "쿠폰이 모두 소진되었습니다.")
            null -> CouponIssueResponse(false, "ERROR", null, "쿠폰 발급 중 오류가 발생했습니다.")
            else -> {

                val (couponKey, typeStr, discountStr) = result.split(":")
                val discountValue = discountStr.toInt()
                val discountType = DiscountType.valueOf(typeStr.uppercase())  // enum DiscountType.PERCENT, AMOUNT 등

                // Kafka 이벤트 발행
                val event = CouponIssuedEvent(eventId, userId, couponKey, discountValue, discountType)
                kafkaTemplate.send("coupon-issued", eventId, event)

                // 사용자 메시지 구성
                val message = when (discountType) {
                    DiscountType.PERCENT -> "${discountValue}% 할인 쿠폰이 발급되었습니다."
                    DiscountType.AMOUNT -> "${discountValue}원 할인 쿠폰이 발급되었습니다."
                }

                CouponIssueResponse(true, null, couponKey, message)
            }
        }

    }
}