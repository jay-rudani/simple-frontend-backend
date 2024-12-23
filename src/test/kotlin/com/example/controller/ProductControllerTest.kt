package com.example.controller

import com.example.dto.ProductDto
import com.example.model.Product
import com.example.service.ProductService
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.sql.Timestamp

@WebMvcTest(ProductController::class)
class ProductControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockitoBean
    private lateinit var productService: ProductService

    @Test
    fun `getProducts should return products view`() {
        // Arrange: Mock service response
        val mockProducts = listOf(
            Product(
                1L,
                "Product 1",
                "Vendor A",
                "Type A",
                Timestamp.valueOf("2024-01-01 00:00:00"),
                Timestamp.valueOf("2024-01-02 00:00:00"),
                emptyList()
            ),
            Product(
                2L,
                "Product 2",
                "Vendor B",
                "Type B",
                Timestamp.valueOf("2024-01-03 00:00:00"),
                Timestamp.valueOf("2024-01-04 00:00:00"),
                emptyList()
            )
        )
        `when`(productService.getAllProducts()).thenReturn(mockProducts)

        // Act & Assert
        mockMvc.perform(get("/test"))
            .andExpect(status().isOk)
            .andExpect(view().name("products"))
            .andExpect(model().attribute("products", mockProducts))
    }

    @Test
    fun `getProductsTable should return products table fragment`() {
        // Arrange: Mock service response
        val mockProducts = listOf(
            Product(
                1L,
                "Product 1",
                "Vendor A",
                "Type A",
                Timestamp.valueOf("2024-01-01 00:00:00"),
                Timestamp.valueOf("2024-01-02 00:00:00"),
                emptyList()
            )
        )
        `when`(productService.getAllProducts()).thenReturn(mockProducts)

        // Act & Assert
        mockMvc.perform(get("/test/table"))
            .andExpect(status().isOk)
            .andExpect(view().name("products :: products-table"))
            .andExpect(model().attribute("products", mockProducts))
    }

    @Test
    fun `addProduct should save product and return updated products table fragment`() {
        // Arrange: Mock input DTO and service responses
        val productDto = ProductDto("Product 3", "Vendor C", "Type C")
        val updatedProducts = listOf(
            Product(
                1L,
                "Product 1",
                "Vendor A",
                "Type A",
                Timestamp.valueOf("2024-01-01 00:00:00"),
                Timestamp.valueOf("2024-01-02 00:00:00"),
                emptyList()
            ),
            Product(
                2L,
                "Product 2",
                "Vendor B",
                "Type B",
                Timestamp.valueOf("2024-01-03 00:00:00"),
                Timestamp.valueOf("2024-01-04 00:00:00"),
                emptyList()
            ),
            Product(
                3L,
                "Product 3",
                "Vendor C",
                "Type C",
                Timestamp.valueOf("2024-01-05 00:00:00"),
                Timestamp.valueOf("2024-01-06 00:00:00"),
                emptyList()
            )
        )

        // Mock saveProduct to return a Long
        `when`(productService.saveProduct(productDto)).thenReturn(123L)
        `when`(productService.getAllProducts()).thenReturn(updatedProducts)

        // Act & Assert
        mockMvc.perform(
            post("/test/product")
                .param("title", productDto.title)
                .param("vendor", productDto.vendor)
                .param("type", productDto.type)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
        )
            .andExpect(status().isOk)
            .andExpect(view().name("products :: products-table"))
            .andExpect(model().attribute("products", updatedProducts))
    }
}