package br.com.zup.realwave.sales.manager.infrastructure

import javax.validation.ConstraintViolationException
import javax.validation.Validation
import javax.validation.Validator

/**
 * Created by luizs on 04/07/2017
 */
object Validator {
    val validator: Validator by lazy {
        Validation.buildDefaultValidatorFactory().validator
    }

    fun <T> validate(instance: T) {
        val validate = validator.validate(instance)

        if (!validate.isEmpty()) {
            throw ConstraintViolationException(validate)
        }
    }
}
