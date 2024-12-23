@file:Suppress("UNCHECKED_CAST")

package com.example.service

import com.example.dto.ProductDto
import com.example.dto.VariantDto
import com.example.model.Product
import com.example.model.Variant
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestTemplate
import java.sql.ResultSet
import java.sql.Timestamp
import java.time.Instant
import java.time.LocalDateTime

@ExtendWith(value = [MockitoExtension::class])
class ProductServiceTest {

    private lateinit var productService: ProductService
    private lateinit var jdbcClient: JdbcClient
    private lateinit var restTemplate: RestTemplate

    @Captor
    private lateinit var productMapperCaptor: ArgumentCaptor<RowMapper<Product>>

    @Captor
    private lateinit var variantMapperCaptor: ArgumentCaptor<RowMapper<Variant>>

    companion object {
        private const val SELECT_PRODUCTS_SQL = """
            SELECT id, title, vendor, type, created_at, updated_at 
            FROM products"""

        private const val SELECT_VARIANTS_SQL = """
            SELECT id, product_id, title, sku, price, available, 
                   option1, option2, created_at, updated_at
            FROM variants 
            WHERE product_id = ?"""

        private const val SELECT_COUNT_SQL = "SELECT COUNT(*) FROM products"
        private const val API_URL = "https://famme.no/products.json"

        private const val INSERT_PRODUCT_SQL = """INSERT INTO products (id, title, vendor, type, created_at, updated_at)
VALUES (?, ?, ?, ?, ?, ?)"""
        private val INSERT_VARIANT_SQL = """
                                INSERT INTO variants (
                                    id, product_id, title, sku, price, available,
                                    option1, option2, created_at, updated_at
                                )
                                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                                """.trimIndent()
    }

    @BeforeEach
    fun setUp() {
        jdbcClient = mock(JdbcClient::class.java)
        restTemplate = mock(RestTemplate::class.java)
        productService = ProductService(restTemplate = restTemplate, jdbcClient = jdbcClient)
    }

    @Test
    fun `getAllProducts should return all of products`() {
        val now = Timestamp.from(Instant.now())

        // Create mocked ResultSet for products
        val productRs = mock(ResultSet::class.java).apply {
            `when`(getLong("id")).thenReturn(1L)
            `when`(getString("title")).thenReturn("Test Product")
            `when`(getString("vendor")).thenReturn("Test Vendor")
            `when`(getString("type")).thenReturn("Test Type")
            `when`(getTimestamp("created_at")).thenReturn(now)
            `when`(getTimestamp("updated_at")).thenReturn(now)
        }

        // Create mocked ResultSet for variants
        val variantRs = mock(ResultSet::class.java).apply {
            `when`(getLong("id")).thenReturn(1L)
            `when`(getLong("product_id")).thenReturn(1L)
            `when`(getString("title")).thenReturn("Test Variant")
            `when`(getString("sku")).thenReturn("TEST-SKU")
            `when`(getDouble("price")).thenReturn(9.99)
            `when`(getBoolean("available")).thenReturn(true)
            `when`(getString("option1")).thenReturn("Option1")
            `when`(getString("option2")).thenReturn("Option2")
            `when`(getTimestamp("created_at")).thenReturn(now)
            `when`(getTimestamp("updated_at")).thenReturn(now)
        }

        // Mock the product query chain
        val productSpecMock = mock(JdbcClient.StatementSpec::class.java)
        val productMappedQuerySpec = mock(JdbcClient.MappedQuerySpec::class.java) as JdbcClient.MappedQuerySpec<Product>

        doReturn(productSpecMock)
            .`when`(jdbcClient)
            .sql(SELECT_PRODUCTS_SQL.trimIndent())

        doReturn(productMappedQuerySpec)
            .`when`(productSpecMock)
            .query(productMapperCaptor.capture())

        doReturn(
            listOf(
                Product(
                    id = 1L,
                    title = "Test Product",
                    vendor = "Test Vendor",
                    productType = "Test Type",
                    createdAt = now,
                    updatedAt = now,
                    variants = emptyList()
                )
            )
        )
            .`when`(productMappedQuerySpec)
            .list()

        // Mock the variant query chain
        val variantSpecMock = mock(JdbcClient.StatementSpec::class.java)
        val variantMappedQuerySpec = mock(JdbcClient.MappedQuerySpec::class.java) as JdbcClient.MappedQuerySpec<Variant>

        doReturn(variantSpecMock)
            .`when`(jdbcClient)
            .sql(SELECT_VARIANTS_SQL.trimIndent())

        doReturn(variantSpecMock)
            .`when`(variantSpecMock)
            .param(any())

        doReturn(variantMappedQuerySpec)
            .`when`(variantSpecMock)
            .query(variantMapperCaptor.capture())

        doReturn(
            listOf(
                Variant(
                    id = 1L,
                    productId = 1L,
                    title = "Test Variant",
                    sku = "TEST-SKU",
                    price = 9.99,
                    available = true,
                    option1 = "Option1",
                    option2 = "Option2",
                    createdAt = now,
                    updatedAt = now
                )
            )
        )
            .`when`(variantMappedQuerySpec)
            .list()

        // Execute the method to trigger capturing
        val results = productService.getAllProducts()

        // Get the captured mappers
        val productMapper = productMapperCaptor.value
        val variantMapper = variantMapperCaptor.value

        // Test the actual mapping
        val mappedProduct = productMapper.mapRow(productRs, 1)!!
        assertNotNull(mappedProduct)
        assertEquals(1L, mappedProduct.id)
        assertEquals("Test Product", mappedProduct.title)
        assertEquals("Test Vendor", mappedProduct.vendor)
        assertEquals("Test Type", mappedProduct.productType)
        assertEquals(now, mappedProduct.createdAt)
        assertEquals(now, mappedProduct.updatedAt)

        val mappedVariant = variantMapper.mapRow(variantRs, 1)!!
        assertNotNull(mappedVariant)
        assertEquals(1L, mappedVariant.id)
        assertEquals(1L, mappedVariant.productId)
        assertEquals("Test Variant", mappedVariant.title)
        assertEquals("TEST-SKU", mappedVariant.sku)
        assertEquals(9.99, mappedVariant.price)
        assertTrue(mappedVariant.available)
        assertEquals("Option1", mappedVariant.option1)
        assertEquals("Option2", mappedVariant.option2)
        assertEquals(now, mappedVariant.createdAt)
        assertEquals(now, mappedVariant.updatedAt)

        // Verify the full results
        assertEquals(1, results.size)
        assertEquals(1, results[0].variants.size)
    }

    @Test
    fun `saveProduct should insert product and variants`() {
        // Given
        val productDto = ProductDto(
            title = "Test Product",
            vendor = "Test Vendor",
            type = "Test Type",
            variants = listOf(
                VariantDto(
                    title = "Test Variant",
                    sku = "TEST-SKU",
                    price = 9.99,
                    available = true,
                    option1 = "Option1",
                    option2 = "Option2"
                )
            )
        )

        // Mock SQL execution for product insert
        val productStatementSpec = mock(JdbcClient.StatementSpec::class.java)
        val productExecuteSpec = mock(JdbcClient.StatementSpec::class.java)

        doReturn(productStatementSpec)
            .`when`(jdbcClient)
            .sql(INSERT_PRODUCT_SQL)

        doReturn(productExecuteSpec)
            .`when`(productStatementSpec)
            .params(
                any(),
                eq("Test Product"),
                eq("Test Vendor"),
                eq("Test Type"),
                any<LocalDateTime>(),
                any<LocalDateTime>()
            )

        doReturn(1)
            .`when`(productExecuteSpec)
            .update()

        // Mock SQL execution for variant insert
        val variantStatementSpec = mock(JdbcClient.StatementSpec::class.java)
        val variantExecuteSpec = mock(JdbcClient.StatementSpec::class.java)

        doReturn(variantStatementSpec)
            .`when`(jdbcClient)
            .sql(INSERT_VARIANT_SQL)

        doReturn(variantExecuteSpec)
            .`when`(variantStatementSpec)
            .params(
                any(),
                any(),
                eq("Test Variant"),
                eq("TEST-SKU"),
                eq(9.99),
                eq(true),
                eq("Option1"),
                eq("Option2"),
                any<LocalDateTime>(),
                any<LocalDateTime>()
            )

        doReturn(1)
            .`when`(variantExecuteSpec)
            .update()

        // When
        val result = productService.saveProduct(productDto)

        // Then
        assertNotNull(result)

        // Verify product insert using exact SQL from service
        verify(jdbcClient).sql(INSERT_PRODUCT_SQL)
        verify(productExecuteSpec).update()

        // Verify variant insert using exact SQL from service
        verify(jdbcClient).sql(INSERT_VARIANT_SQL)
        verify(variantExecuteSpec).update()

        // Verify timestamps and main values were used
        verify(productStatementSpec).params(
            any(),
            eq("Test Product"),
            eq("Test Vendor"),
            eq("Test Type"),
            any<LocalDateTime>(),
            any<LocalDateTime>()
        )

        verify(variantStatementSpec).params(
            any(),
            any(),
            eq("Test Variant"),
            eq("TEST-SKU"),
            eq(9.99),
            eq(true),
            eq("Option1"),
            eq("Option2"),
            any<LocalDateTime>(),
            any<LocalDateTime>()
        )
    }

    @Test
    fun `fetchAndSaveProducts should handle errors gracefully`() {
        // Given
        val countStatementSpec = mock(JdbcClient.StatementSpec::class.java)
        val countMappedQuerySpec = mock(JdbcClient.MappedQuerySpec::class.java) as JdbcClient.MappedQuerySpec<Int>

        doReturn(countStatementSpec)
            .`when`(jdbcClient)
            .sql(SELECT_COUNT_SQL)

        doReturn(countMappedQuerySpec)
            .`when`(countStatementSpec)
            .query(Int::class.java)

        doReturn(0)
            .`when`(countMappedQuerySpec)
            .single()

        // Simple, direct mock of RestTemplate
        `when`(restTemplate.getForObject(API_URL, JsonNode::class.java))
            .thenThrow(RestClientException("API Error"))

        // When
        productService.fetchAndSaveProducts()

        // Then
        verify(jdbcClient).sql(SELECT_COUNT_SQL)
        verify(restTemplate).getForObject(API_URL, JsonNode::class.java)
        verify(jdbcClient, never()).sql(INSERT_PRODUCT_SQL)
    }

    @Test
    fun `fetchAndSaveProducts should skip fetch when database is not empty`() {
        // Given
        val countStatementSpec = mock(JdbcClient.StatementSpec::class.java)
        val countMappedQuerySpec = mock(JdbcClient.MappedQuerySpec::class.java) as JdbcClient.MappedQuerySpec<Int>

        // Mock count query to return non-zero
        doReturn(countStatementSpec)
            .`when`(jdbcClient)
            .sql(SELECT_COUNT_SQL)

        doReturn(countMappedQuerySpec)
            .`when`(countStatementSpec)
            .query(Int::class.java)

        doReturn(1)
            .`when`(countMappedQuerySpec)
            .single()

        // When
        productService.fetchAndSaveProducts()

        // Then
        verify(jdbcClient).sql(SELECT_COUNT_SQL)
        verify(restTemplate, never()).getForObject(API_URL, JsonNode::class.java)
        verify(jdbcClient, never()).sql(INSERT_PRODUCT_SQL)
        verify(jdbcClient, never()).sql(INSERT_VARIANT_SQL)
    }

    @Test
    fun `fetchAndSaveProducts should fetch and save when database is empty`() {
        // Given
        val countStatementSpec = mock(JdbcClient.StatementSpec::class.java)
        val countMappedQuerySpec = mock(JdbcClient.MappedQuerySpec::class.java) as JdbcClient.MappedQuerySpec<Int>

        // Mock count query to return 0
        doReturn(countStatementSpec)
            .`when`(jdbcClient)
            .sql(SELECT_COUNT_SQL)

        doReturn(countMappedQuerySpec)
            .`when`(countStatementSpec)
            .query(Int::class.java)

        doReturn(0)
            .`when`(countMappedQuerySpec)
            .single()

        // Mock JSON response
        val mockJsonResponse = """
            {
                "products": [
                    {
                        "id": 123,
                        "title": "Test Product",
                        "vendor": "Test Vendor",
                        "product_type": "Test Type",
                        "variants": [
                            {
                                "id": 456,
                                "title": "Test Variant",
                                "sku": "TEST-SKU",
                                "price": 99.99,
                                "available": true,
                                "option1": "Option 1",
                                "option2": "Option 2"
                            }
                        ]
                    }
                ]
            }
        """
        val objectMapper = ObjectMapper()
        val jsonNode = objectMapper.readTree(mockJsonResponse)

        doReturn(jsonNode)
            .`when`(restTemplate)
            .getForObject("https://famme.no/products.json", JsonNode::class.java)

        // Mock product insert
        val productStatementSpec = mock(JdbcClient.StatementSpec::class.java)
        val productExecuteSpec = mock(JdbcClient.StatementSpec::class.java)

        doReturn(productStatementSpec)
            .`when`(jdbcClient)
            .sql(INSERT_PRODUCT_SQL)

        doReturn(productExecuteSpec)
            .`when`(productStatementSpec)
            .params(
                eq(123L),
                eq("Test Product"),
                eq("Test Vendor"),
                eq("Test Type"),
                any<LocalDateTime>(),
                any<LocalDateTime>()
            )

        doReturn(1)
            .`when`(productExecuteSpec)
            .update()

        // Mock variant insert
        val variantStatementSpec = mock(JdbcClient.StatementSpec::class.java)
        val variantExecuteSpec = mock(JdbcClient.StatementSpec::class.java)

        doReturn(variantStatementSpec)
            .`when`(jdbcClient)
            .sql(INSERT_VARIANT_SQL)

        doReturn(variantExecuteSpec)
            .`when`(variantStatementSpec)
            .params(
                eq(456L),
                eq(123L),
                eq("Test Variant"),
                eq("TEST-SKU"),
                eq(99.99),
                eq(true),
                eq("Option 1"),
                eq("Option 2"),
                any<LocalDateTime>(),
                any<LocalDateTime>()
            )

        doReturn(1)
            .`when`(variantExecuteSpec)
            .update()

        // When
        productService.fetchAndSaveProducts()

        // Then
        verify(jdbcClient).sql(SELECT_COUNT_SQL)
        verify(restTemplate).getForObject("https://famme.no/products.json", JsonNode::class.java)
        verify(jdbcClient).sql(INSERT_PRODUCT_SQL)
        verify(jdbcClient).sql(INSERT_VARIANT_SQL)
        verify(productExecuteSpec).update()
        verify(variantExecuteSpec).update()
    }
}