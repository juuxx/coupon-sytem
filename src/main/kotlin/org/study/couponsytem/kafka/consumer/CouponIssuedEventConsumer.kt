package org.study.couponsytem.kafka.consumer

import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.LoggerFactory
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component
import org.study.couponsytem.kafka.event.CouponIssuedEvent
import org.study.couponsytem.repository.CouponPolicyDetailRepository
import org.study.couponsytem.repository.CouponRepository
import org.study.couponsytem.repository.UserRepository
@Component
class CouponIssuedEventConsumer(
    private val objectMapper: ObjectMapper,
    private val couponRepository: CouponRepository,
    private val redisTemplate: StringRedisTemplate,
    private val userRepository: UserRepository
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @KafkaListener(topics = ["coupon-issued"], groupId = "coupon-group")
    fun consume(record: ConsumerRecord<String, String>) {
        val event = try {
            objectMapper.readValue(record.value(), CouponIssuedEvent::class.java)
        } catch (e: Exception) {
            log.error("Kafka 이벤트 파싱 실패", e)
            return
        }

        val issuedKey = "couponIssued:${event.eventId}"

        try {
            val coupon = couponRepository.findByCouponKey(event.couponKey)
                ?: throw IllegalArgumentException("존재하지 않는 쿠폰: ${event.couponKey}")

            val user = userRepository.findByUserId(event.userId)
                ?: throw IllegalArgumentException("존재하지 않는 userId: ${event.userId}")

            coupon.issued = true
            coupon.userId = user.id
            coupon.discountType = event.discountType
            coupon.discountAmount = event.discountValue

            couponRepository.save(coupon)

            // 🔄 Redis 상태: PENDING → COMPLETED
            val field = event.userId.toString()
            val existing = redisTemplate.opsForHash<String, String>().get(issuedKey, field)
            if (existing != null) {
                val updated = existing.replace("\"status\":\"PENDING\"", "\"status\":\"COMPLETED\"")
                redisTemplate.opsForHash<String, String>().put(issuedKey, event.userId, updated)
            }

            log.info("쿠폰 발급 DB 처리 완료: $event")

        } catch (e: Exception) {
            log.error("쿠폰 발급 이벤트 처리 실패", e)

            // 🔄 Redis 상태: PENDING → CANCEL
            val existing = redisTemplate.opsForHash<String, String>().get(issuedKey, event.userId)
            if (existing != null) {
                val updated = existing.replace("\"status\":\"PENDING\"", "\"status\":\"CANCEL\"")
                redisTemplate.opsForHash<String, String>().put(issuedKey, event.userId, updated)
                log.warn("발급 실패로 Redis 상태를 CANCEL 로 갱신: userId=${event.userId}")
            }
        }
    }
}
