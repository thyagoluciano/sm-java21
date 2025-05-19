package br.com.zup.realwave.sales.manager.api.request

import org.hibernate.validator.constraints.NotBlank

data class SegmentationRequest(@field:NotBlank val queryString: String?)
