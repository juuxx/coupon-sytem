package org.study.couponsytem.kafka.consumer

import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.LoggerFactory
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
    private val couponPolicyDetailRepository: CouponPolicyDetailRepository,
    private val userRepository: UserRepository

) {
    private val log = LoggerFactory.getLogger(javaClass)

    @KafkaListener(topics = ["coupon-issued"], groupId = "coupon-group")
    fun consume(record: ConsumerRecord<String, String>) {
        try {
            val event = objectMapper.readValue(record.value(), CouponIssuedEvent::class.java)

            val coupon = couponRepository.findByCouponKey(event.couponKey)
                ?: throw IllegalArgumentException("존재하지 않는 쿠폰: ${event.couponKey}")

            val couponPolicyDetail = couponPolicyDetailRepository.findByCouponGroup_IdAndDiscountValue(coupon.group.id, event.discountRate)
                ?: throw IllegalArgumentException("존재하지 않는 쿠폰 정책: ${event.couponKey}")

            val user = userRepository.findByUserId(event.userId)
                ?: run {
                    log.warn("존재하지 않는 userId: ${event.userId}")
                    return
                }

            val userId = user.id

            coupon.issued = true
            coupon.userId = userId  // userId가 Long 이면
            coupon.discountType = couponPolicyDetail.discountType
            coupon.discountAmount = event.discountRate

            couponRepository.save(coupon)

            log.info("쿠폰 발급 DB 처리 완료: $event")

        } catch (e: Exception) {
            log.error("쿠폰 발급 이벤트 처리 실패", e)
        }
    }
}