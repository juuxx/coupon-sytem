package org.study.couponsytem.service

import org.springframework.stereotype.Service
import org.study.couponsytem.repository.CouponIssueRepository
import org.study.couponsytem.request.CouponIssueReq
import org.study.couponsytem.request.CouponIssueRes

@Service
class CouponIssueService(
    private val couponIssueRepository: CouponIssueRepository
) {
    fun issue(request: CouponIssueReq): CouponIssueRes? {
        //couponInssueRepository
        return null
    }
}