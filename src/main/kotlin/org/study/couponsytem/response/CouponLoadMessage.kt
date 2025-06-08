package org.study.couponsytem.response

object CouponLoadMessage {
    const val SUCCESS = "%d장의 쿠폰이 큐에 적재되었습니다."
    const val EXIST_GROUP_KEY = "이미 존재하는 쿠폰 그룹 키입니다."
    const val GROUP_NOT_FOUND = "쿠폰 그룹이 존재하지 않습니다."
    const val NO_COUPONS = "적재할 쿠폰이 없습니다."
    const val REDIS_FAILURE = "쿠폰 적재 중 오류가 발생했습니다."
}
