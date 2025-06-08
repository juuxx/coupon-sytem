package org.study.couponsytem.controller

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.study.couponsytem.request.CouponIssueRequest
import org.study.couponsytem.request.CouponIssueResponse
import org.study.couponsytem.service.CouponIssueService

@RequestMapping("/api/coupons")
@RestController
class CouponController(
    private val couponIssueService: CouponIssueService
) {
    @PostMapping("/issue")
    fun issueCoupon(@RequestBody request: CouponIssueRequest): ResponseEntity<CouponIssueResponse> =
        ResponseEntity.ok(couponIssueService.issue(request))
}