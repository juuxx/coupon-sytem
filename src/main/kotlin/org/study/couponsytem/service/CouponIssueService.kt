package org.study.couponsytem.service

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.core.io.ClassPathResource
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.data.redis.core.script.DefaultRedisScript
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.scripting.support.ResourceScriptSource
import org.springframework.stereotype.Service
import org.study.couponsytem.kafka.event.CouponIssuedEvent
import org.study.couponsytem.request.CouponIssueRequest
import org.study.couponsytem.request.CouponIssueResponse
import java.time.LocalDateTime

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
                val event = CouponIssuedEvent(
                    eventId = eventId,
                    userId = userId,
                    couponKey = result
                )
                kafkaTemplate.send("coupon-issued", eventId, event)

                CouponIssueResponse(true, null, result, "쿠폰 발급에 성공하였습니다.")
            }
        }

    }
}