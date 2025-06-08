package org.study.couponsytem.request

class CouponIssueResponse (
    val success: Boolean,
    val reason: String?,
    val couponKey: String?,
    val message: String
){
}