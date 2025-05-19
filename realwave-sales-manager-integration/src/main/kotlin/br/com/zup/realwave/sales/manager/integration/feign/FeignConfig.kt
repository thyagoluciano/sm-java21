package br.com.zup.realwave.sales.manager.integration.feign

import br.com.zup.realwave.cms.client.offer.CmsOfferClient
import br.com.zup.realwave.common.graylog.feign.RwSlf4jLogger
import br.com.zup.realwave.feign.commons.FeignConfigurer
import br.com.zup.realwave.pcm.client.domain.PcmCompositionClient
import br.com.zup.realwave.sales.manager.infrastructure.JacksonExtension
import br.com.zup.realwave.sales.manager.integration.feign.apis.*
import br.com.zup.realwave.sales.manager.integration.feign.decoder.CatalogSearchErrorDecoder
import br.com.zup.realwave.sales.manager.integration.feign.decoder.CouponErrorDecoder
import br.com.zup.realwave.sales.manager.integration.feign.decoder.CustomerInfoErrorDecoder
import br.com.zup.realwave.sales.manager.integration.feign.decoder.CustomerOrderManagerErrorDecoder
import br.com.zup.realwave.sales.manager.integration.feign.decoder.EventStoreErrorDecoder
import br.com.zup.realwave.sales.manager.integration.feign.decoder.ProductCatalogManagerErrorDecoder
import br.com.zup.realwave.sales.manager.integration.feign.interceptor.EventStoreBasicAuth
import br.com.zup.realwave.sales.manager.integration.feign.interceptor.SalesManagerFeignInterceptor
import feign.Feign
import feign.Logger
import feign.Request
import feign.Retryer
import feign.jackson.JacksonDecoder
import feign.jackson.JacksonEncoder
import feign.okhttp.OkHttpClient
import feign.slf4j.Slf4jLogger
import org.apache.logging.log4j.LogManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * Created by Danilo Paiva on 06/06/17
 */
@Configuration
open class FeignConfig {

    companion object {
        private const val TEN_SECONDS = 10000
        private const val ONE_MINUTE = 60000
    }

    private val log = LogManager.getLogger(this.javaClass)

    @Value("\${realwave.com.command.url}")
    private lateinit var customerOrderManagerUrl: String

    @Value("\${event.store.url}")
    private lateinit var eventStoreUrl: String

    @Value("\${realwave.mgm.query.url}")
    private lateinit var memberGetMemberUrl: String

    @Value("\${realwave.catalog.search.url}")
    private lateinit var catalogSearchUrl: String

    @Value("\${realwave.pcm.manager.url}")
    private lateinit var productCatalogManagerUrl: String

    @Value("\${realwave.cim.search.url}")
    private lateinit var customerSearchUrl: String

    @Value("\${realwave.cim.manager.url}")
    private lateinit var customerUrl: String

    @Value("\${realwave.coupon.url}")
    private lateinit var couponUrl: String

    @Autowired
    private lateinit var customerOrderManagerErrorDecoder: CustomerOrderManagerErrorDecoder

    @Autowired
    private lateinit var memberGetMemberErrorDecoder: MemberGetMemberErrorDecoder

    @Autowired
    private lateinit var catalogSearchErrorDecoder: CatalogSearchErrorDecoder

    @Autowired
    private lateinit var productCatalogManagerErrorDecoder: ProductCatalogManagerErrorDecoder

    @Autowired
    private lateinit var customerInfoErrorDecoder: CustomerInfoErrorDecoder

    @Autowired
    private lateinit var eventStoreErrorDecoder: EventStoreErrorDecoder

    @Autowired
    private lateinit var couponErrorDecoder: CouponErrorDecoder

    @Autowired
    private lateinit var eventStoreBasicAuth: EventStoreBasicAuth

    @Autowired
    private lateinit var salesManagerFeignInterceptor: SalesManagerFeignInterceptor

    @Autowired
    private lateinit var feignConfigurer: FeignConfigurer

    private val jacksonEncoder = JacksonEncoder(JacksonExtension.jacksonObjectMapper)
    private val jacksonDecoder = JacksonDecoder(JacksonExtension.jacksonObjectMapper)

    @Bean
    @Qualifier(FeignQualifier.EVENT_STORE_API)
    open fun eventStoreClientFeign(): EventStoreApiService {

        return Feign.builder()
            .encoder(jacksonEncoder)
            .decoder(jacksonDecoder)
            .client(OkHttpClient())
            .requestInterceptor(eventStoreBasicAuth)
            .logger(Slf4jLogger())
            .logLevel(Logger.Level.FULL)
            .options(Request.Options(TEN_SECONDS, ONE_MINUTE))
            .retryer(Retryer.NEVER_RETRY)
            .errorDecoder(eventStoreErrorDecoder)
            .requestInterceptor(salesManagerFeignInterceptor)
            .target(EventStoreApiService::class.java, eventStoreUrl)
    }

    @Bean
    @Qualifier(FeignQualifier.CUSTOMER_ORDER_MANAGER_API)
    open fun customerOrderManagerClientFeign(): CustomerOrderManagerApiService {

        return feignConfigurer.configure()
            .defaultFeignContract()
            .encoder(jacksonEncoder)
            .decoder(jacksonDecoder)
            .logger(RwSlf4jLogger())
            .logLevel(Logger.Level.FULL)
            .options(Request.Options(TEN_SECONDS, ONE_MINUTE))
            .retryer(Retryer.NEVER_RETRY)
            .errorDecoder(customerOrderManagerErrorDecoder)
            .requestInterceptor(salesManagerFeignInterceptor)
            .target(CustomerOrderManagerApiService::class.java, customerOrderManagerUrl)
    }

    @Bean
    open fun memberGetMemberClientFeign(): MemberGetMemberApiService {
        return feignConfigurer.configure()
            .defaultFeignContract()
            .encoder(jacksonEncoder)
            .decoder(jacksonDecoder)
            .logger(RwSlf4jLogger())
            .logLevel(Logger.Level.FULL)
            .options(Request.Options(TEN_SECONDS, ONE_MINUTE))
            .retryer(Retryer.NEVER_RETRY)
            .errorDecoder(memberGetMemberErrorDecoder)
            .requestInterceptor(salesManagerFeignInterceptor)
            .target(MemberGetMemberApiService::class.java, memberGetMemberUrl)
    }

    @Bean
    open fun catalogOfferClientFeign(): CmsOfferClient {
        return feignConfigurer.configure()
            .encoder(jacksonEncoder)
            .decoder(jacksonDecoder)
            .logger(RwSlf4jLogger())
            .logLevel(Logger.Level.FULL)
            .options(Request.Options(TEN_SECONDS, ONE_MINUTE))
            .retryer(Retryer.NEVER_RETRY)
            .errorDecoder(catalogSearchErrorDecoder)
            .requestInterceptor(salesManagerFeignInterceptor)
            .target(CmsOfferClient::class.java, catalogSearchUrl)
    }

    @Bean
    open fun pcmClientFeign(): PcmCompositionClient {
        return feignConfigurer.configure()
            .encoder(jacksonEncoder)
            .decoder(jacksonDecoder)
            .logger(RwSlf4jLogger())
            .logLevel(Logger.Level.FULL)
            .options(Request.Options(TEN_SECONDS, ONE_MINUTE))
            .retryer(Retryer.NEVER_RETRY)
            .errorDecoder(productCatalogManagerErrorDecoder)
            .requestInterceptor(salesManagerFeignInterceptor)
            .target(PcmCompositionClient::class.java, productCatalogManagerUrl)
    }

    @Bean
    open fun customerInfoClientFeign(): CustomerSearchApiService {
        return feignConfigurer.configure()
            .defaultFeignContract()
            .encoder(jacksonEncoder)
            .decoder(jacksonDecoder)
            .logger(RwSlf4jLogger())
            .logLevel(Logger.Level.FULL)
            .options(Request.Options(TEN_SECONDS, ONE_MINUTE))
            .retryer(Retryer.NEVER_RETRY)
            .errorDecoder(customerInfoErrorDecoder)
            .requestInterceptor(salesManagerFeignInterceptor)
            .target(CustomerSearchApiService::class.java, customerSearchUrl)
    }

    @Bean
    open fun customerClientFeign(): CustomerApiService {
        log.info("Customer manager url: {}", customerUrl)
        return feignConfigurer.configure()
            .defaultFeignContract()
            .encoder(jacksonEncoder)
            .decoder(jacksonDecoder)
            .logger(RwSlf4jLogger())
            .logLevel(Logger.Level.FULL)
            .options(Request.Options(TEN_SECONDS, ONE_MINUTE))
            .retryer(Retryer.NEVER_RETRY)
            .errorDecoder(customerInfoErrorDecoder)
            .requestInterceptor(salesManagerFeignInterceptor)
            .target(CustomerApiService::class.java, customerUrl)
    }

    @Bean
    open fun couponClientFeign(): CouponApiService {
        return feignConfigurer.configure()
            .defaultFeignContract()
            .encoder(jacksonEncoder)
            .decoder(jacksonDecoder)
            .logger(RwSlf4jLogger())
            .logLevel(Logger.Level.FULL)
            .options(Request.Options(TEN_SECONDS, ONE_MINUTE))
            .retryer(Retryer.NEVER_RETRY)
            .errorDecoder(couponErrorDecoder)
            .requestInterceptor(salesManagerFeignInterceptor)
            .target(CouponApiService::class.java, couponUrl)
    }

}
