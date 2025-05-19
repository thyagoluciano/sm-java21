package br.com.zup.test.realwave.sales.manager.application.config

import br.com.zup.realwave.common.context.web.filter.TrackingFilter
import br.com.zup.realwave.sales.manager.command.application.filter.TenantValidationFilter
import org.junit.Before
import org.junit.Rule
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.restdocs.JUnitRestDocumentation
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation
import org.springframework.restdocs.mockmvc.RestDocumentationResultHandler
import org.springframework.restdocs.operation.preprocess.Preprocessors
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext

@SpringBootTest
@RunWith(SpringRunner::class)
@ContextConfiguration(classes = [(ApplicationTestConfig::class)])
abstract class ApplicationBaseTest {

    @Rule
    @JvmField
    var restDocumentation = JUnitRestDocumentation("target/generated-snippets")

    @Autowired
    lateinit var context: WebApplicationContext

    protected lateinit var mockMvc: MockMvc

    protected lateinit var document: RestDocumentationResultHandler

    @Autowired
    lateinit var tenantValidationFilter: TenantValidationFilter

    @Autowired
    lateinit var trackingFilters: List<FilterRegistrationBean>

    @Before
    fun setUp() {
        document = MockMvcRestDocumentation.document(
            "{method-name}",
            Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
            Preprocessors.preprocessResponse(Preprocessors.prettyPrint())
        )
        mockMvc = MockMvcBuilders.webAppContextSetup(this.context)
            .addFilters<DefaultMockMvcBuilder>(
                tenantValidationFilter,
                trackingFilters.first { it.filter is TrackingFilter }.filter
            )
            .apply<DefaultMockMvcBuilder>(MockMvcRestDocumentation.documentationConfiguration(this.restDocumentation))
            .alwaysDo<DefaultMockMvcBuilder>(this.document).build()
    }

}
