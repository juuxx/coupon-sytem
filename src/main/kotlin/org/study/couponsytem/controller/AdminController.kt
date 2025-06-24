package org.study.couponsytem.controller

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.study.couponsytem.request.CouponLoadCommand
import org.study.couponsytem.request.CouponLoadRequest
import org.study.couponsytem.request.CouponPolicyCreateRequest
import org.study.couponsytem.response.CouponLoadResponse
import org.study.couponsytem.service.CouponLoadFacadeService
import org.study.couponsytem.service.PreLoadCouponService

@RequestMapping("/admin")
@RestController
class AdminController(
    private val preloadCouponService: PreLoadCouponService,
    private val couponLoadFacadeService: CouponLoadFacadeService
) {

    @PostMapping("/coupons/load")
    fun loadCoupons(@RequestBody request: CouponPolicyCreateRequest) : ResponseEntity<CouponLoadResponse> {
        val preload = preloadCouponService.preload(request)
        return ResponseEntity.ok(preload)
    }

    @PostMapping("/coupons/load2")
    fun loadCoupons(@RequestBody request: CouponLoadCommand) : ResponseEntity<CouponLoadResponse> {
        val preload = couponLoadFacadeService.preload(request)
        return ResponseEntity.ok(preload)
    }
}