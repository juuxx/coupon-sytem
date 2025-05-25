package org.study.couponsytem.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import org.study.couponsytem.entity.Coupon


@Repository
interface CouponIssueRepository : JpaRepository<Coupon, Long> {

    // 🔍 쿠폰 키로 조회
    fun findByCouponKey(couponKey: String): Coupon?

    // 🔍 발급 안 된 쿠폰 찾기
    fun findFirstByIssuedFalse(): Coupon?

    // 🔍 특정 유저가 받은 쿠폰들
    fun findAllByUserId(userId: Long): List<Coupon>

    // 🔍 미발급 쿠폰만 목록
    fun findAllByIssuedFalse(): List<Coupon>

    // 🔍 쿠폰 키로 존재 여부 확인
    fun existsByCouponKey(couponKey: String): Boolean
}