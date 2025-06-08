package org.study.couponsytem.controller

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.study.couponsytem.request.CouponLoadRequest
import org.study.couponsytem.response.CouponLoadResponse
import org.study.couponsytem.service.PreLoadCouponService

@RequestMapping("/admin")
@RestController
class AdminController(
    private val preloadCouponService: PreLoadCouponService
) {

    @PostMapping("/coupons/load")
    fun loadCoupons(@RequestBody request: CouponLoadRequest) : ResponseEntity<CouponLoadResponse> {
        val preload = preloadCouponService.preload(request)
        return ResponseEntity.ok(preload)
    }
}