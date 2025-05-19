package br.com.zup.realwave.sales.manager.integration.feign.apis

import br.com.zup.rw.coupon.api.v1.representation.CouponRepresentation
import feign.Headers
import feign.Param
import feign.RequestLine

@Headers("Accept: application/json", "Content-Type: application/json")
interface CouponApiService {

    @RequestLine("GET /v1/coupons/code/{code}/customer/{customerId}/validation")
    fun validationCoupon(
        @Param("code") code: String,
        @Param("customerId") customerId: String
    ): CouponRepresentation

}
