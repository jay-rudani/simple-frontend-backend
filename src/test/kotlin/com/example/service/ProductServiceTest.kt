@file:Suppress("SqlSourceToSinkFlow")

package com.example.service

import com.example.dao.ProductDAO
import com.example.dto.ProductDto
import com.example.dto.VariantDto
import com.example.model.Product
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.web.client.RestTemplate
import java.sql.Timestamp
import java.time.LocalDateTime

@ExtendWith(value = [MockitoExtension::class])
class ProductServiceTest {

    private lateinit var productService: ProductService

    @Mock
    private lateinit var restTemplate: RestTemplate

    @Mock
    private lateinit var productDAO: ProductDAO

    private val objectMapper = ObjectMapper()

    private lateinit var dummyProductDto: ProductDto

    private lateinit var jsonNode: JsonNode


    @BeforeEach
    fun setup() {
        MockitoAnnotations.openMocks(this)
        productService = ProductService(restTemplate, productDAO)
        dummyProductDto = ProductDto(
            id = 1L,
            title = "Test Product",
            vendor = "Test Vendor",
            type = "Test Type",
            variants = listOf(
                VariantDto(
                    id = 101L,
                    productId = 1L,
                    title = "Variant 1",
                    sku = "SKU-001",
                    price = 99.99,
                    available = true,
                    option1 = "Size M",
                    option2 = "Color Blue",
                    createdAt = LocalDateTime.now(),
                    updatedAt = LocalDateTime.now()
                )
            ),
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )

        val jsonResponse = """
            {
                "products": [
                    {
                        "id": 1,
                        "title": "Test Product",
                        "vendor": "Test Vendor",
                        "product_type": "Test Type",
                        "variants": [
                            {
                                "id": 101,
                                "title": "Variant 1",
                                "sku": "SKU-001",
                                "price": 99.99,
                                "available": true,
                                "option1": "Size M",
                                "option2": "Color Blue"
                            }
                        ]
                    }
                ]
            }
        """.trimIndent()
        jsonNode = objectMapper.readTree(jsonResponse)
    }

    @Test
    fun `fetchAndSaveProducts should save products when database is empty`() {
        // Given
        `when`(productDAO.getCountOfProducts()).thenReturn(0)
        `when`(restTemplate.getForObject("https://famme.no/products.json", JsonNode::class.java))
            .thenReturn(jsonNode)

        // When
        productService.fetchAndSaveProducts()

        // Then
        verify(productDAO).getCountOfProducts()
        verify(restTemplate).getForObject("https://famme.no/products.json", JsonNode::class.java)
    }

    @Test
    fun `fetchAndSaveProducts should not fetch products when database is not empty`() {
        // Given
        `when`(productDAO.getCountOfProducts()).thenReturn(5)

        // When
        productService.fetchAndSaveProducts()

        // Then
        verify(productDAO).getCountOfProducts()
        verify(restTemplate, never()).getForObject(anyString(), any<Class<JsonNode>>())
    }

    @Test
    fun `fetchAndSaveProducts should handle getCountOfProducts error gracefully`() {
        // Given
        doThrow(RuntimeException("Database error"))
            .`when`(productDAO).getCountOfProducts()

        // When
        productService.fetchAndSaveProducts()

        // Then - verify the method completes without throwing exception
        verify(productDAO).getCountOfProducts()
        verifyNoMoreInteractions(productDAO)
        verifyNoInteractions(restTemplate)
    }

    @Test
    fun `getAllProducts should return list of products from DAO`() {
        // Given
        val expectedProducts = listOf(
            Product(
                id = 1L,
                title = "Test Product",
                vendor = "Test Vendor",
                productType = "Test Type",
                variants = listOf(),
                createdAt = Timestamp.valueOf(LocalDateTime.now()),
                updatedAt = Timestamp.valueOf(LocalDateTime.now())
            )
        )
        `when`(productDAO.getAllProducts()).thenReturn(expectedProducts)

        // When
        val result = productService.getAllProducts()

        // Then
        verify(productDAO).getAllProducts()
        assertEquals(expectedProducts, result)
    }

    @Test
    fun `saveProduct should delegate to DAO and return product id`() {
        // Given
        val productDto = ProductDto(
            id = 1L,
            title = "Test Product",
            vendor = "Test Vendor",
            type = "Test Type",
            variants = listOf(),
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        val expectedId = 1L
        `when`(productDAO.saveProduct(productDto)).thenReturn(expectedId)

        // When
        val result = productService.saveProduct(productDto)

        // Then
        verify(productDAO).saveProduct(productDto)
        assert(result == expectedId)
        assertEquals(1L, result)
    }
}