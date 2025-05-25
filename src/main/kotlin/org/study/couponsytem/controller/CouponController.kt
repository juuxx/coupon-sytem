package org.study.couponsytem.controller

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.study.couponsytem.request.CouponIssueReq
import org.study.couponsytem.request.CouponIssueRes
import org.study.couponsytem.service.CouponIssueService

@RequestMapping
@RestController
class CouponController(
    private val couponIssueService: CouponIssueService
) {
    @PostMapping
    fun create(@RequestBody request: CouponIssueReq): ResponseEntity<CouponIssueRes> =
        ResponseEntity.ok(couponIssueService.issue(request))
}