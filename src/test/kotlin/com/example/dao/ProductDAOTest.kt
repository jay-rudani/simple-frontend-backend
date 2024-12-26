@file:Suppress("SqlSourceToSinkFlow")

package com.example.dao

import com.example.constants.QueryConstants
import com.example.dto.ProductDto
import com.example.dto.VariantDto
import com.example.model.Product
import com.example.model.Variant
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.simple.JdbcClient
import java.sql.ResultSet
import java.sql.Timestamp
import java.time.LocalDateTime

@ExtendWith(MockitoExtension::class)
class ProductDAOTest {

    @Mock
    private lateinit var jdbcClient: JdbcClient

    private lateinit var productDAO: ProductDAO

    @BeforeEach
    fun setup() {
        productDAO = ProductDAO(jdbcClient)
    }

    @Test
    fun `test getAllProducts returns products with variants with row mapping`() {
        // Given
        val now = Timestamp.valueOf(LocalDateTime.now())

        // Mock ResultSets
        val mockProductRs = mock(ResultSet::class.java)
        val mockVariantRs = mock(ResultSet::class.java)

        // Setup Product ResultSet
        `when`(mockProductRs.getLong("id")).thenReturn(1L)
        `when`(mockProductRs.getString("title")).thenReturn("Test Product")
        `when`(mockProductRs.getString("vendor")).thenReturn("Test Vendor")
        `when`(mockProductRs.getString("type")).thenReturn("Test Type")
        `when`(mockProductRs.getTimestamp("created_at")).thenReturn(now)
        `when`(mockProductRs.getTimestamp("updated_at")).thenReturn(now)

        // Setup Variant ResultSet
        `when`(mockVariantRs.getLong("id")).thenReturn(1L)
        `when`(mockVariantRs.getLong("product_id")).thenReturn(1L)
        `when`(mockVariantRs.getString("title")).thenReturn("Test Variant")
        `when`(mockVariantRs.getString("sku")).thenReturn("TEST-SKU")
        `when`(mockVariantRs.getDouble("price")).thenReturn(99.99)
        `when`(mockVariantRs.getBoolean("available")).thenReturn(true)
        `when`(mockVariantRs.getString("option1")).thenReturn("Option 1")
        `when`(mockVariantRs.getString("option2")).thenReturn("Option 2")
        `when`(mockVariantRs.getTimestamp("created_at")).thenReturn(now)
        `when`(mockVariantRs.getTimestamp("updated_at")).thenReturn(now)

        // Setup JdbcClient mocks
        val mockProductStatementSpec = mock(JdbcClient.StatementSpec::class.java)
        val mockProductQuerySpec = mock(JdbcClient.MappedQuerySpec::class.java) as JdbcClient.MappedQuerySpec<Product>
        val mockVariantStatementSpec = mock(JdbcClient.StatementSpec::class.java)
        val mockVariantQuerySpec = mock(JdbcClient.MappedQuerySpec::class.java) as JdbcClient.MappedQuerySpec<Variant>

        `when`(jdbcClient.sql(QueryConstants.GET_ALL_PRODUCTS)).thenReturn(mockProductStatementSpec)
        `when`(mockProductStatementSpec.query(any<RowMapper<Product>>())).thenReturn(mockProductQuerySpec)
        `when`(mockProductQuerySpec.list()).thenReturn(
            listOf(
                Product(
                    id = 1L,
                    title = "Test Product",
                    vendor = "Test Vendor",
                    productType = "Test Type",
                    createdAt = now,
                    updatedAt = now
                )
            )
        )

        `when`(jdbcClient.sql(QueryConstants.GET_ALL_VARIANTS_OF_A_PRODUCT)).thenReturn(mockVariantStatementSpec)
        `when`(mockVariantStatementSpec.paramSource(any())).thenReturn(mockVariantStatementSpec)
        `when`(mockVariantStatementSpec.query(any<RowMapper<Variant>>())).thenReturn(mockVariantQuerySpec)
        `when`(mockVariantQuerySpec.list()).thenReturn(
            listOf(
                Variant(
                    id = 1L,
                    productId = 1L,
                    title = "Test Variant",
                    sku = "TEST-SKU",
                    price = 99.99,
                    available = true,
                    option1 = "Option 1",
                    option2 = "Option 2",
                    createdAt = now,
                    updatedAt = now
                )
            )
        )

        // When
        val result = productDAO.getAllProducts()

        // Then
        // Capture the row mappers
        val productRowMapperCaptor = ArgumentCaptor.forClass(RowMapper::class.java)
        val variantRowMapperCaptor = ArgumentCaptor.forClass(RowMapper::class.java)

        verify(mockProductStatementSpec).query(productRowMapperCaptor.capture())
        verify(mockVariantStatementSpec).query(variantRowMapperCaptor.capture())

        // Execute captured row mappers
        val mappedProduct = productRowMapperCaptor.value.mapRow(mockProductRs, 0) as Product
        val mappedVariant = variantRowMapperCaptor.value.mapRow(mockVariantRs, 0) as Variant

        // Verify Product mapping
        assertEquals(1L, mappedProduct.id)
        assertEquals("Test Product", mappedProduct.title)
        assertEquals("Test Vendor", mappedProduct.vendor)
        assertEquals("Test Type", mappedProduct.productType)
        assertEquals(now, mappedProduct.createdAt)
        assertEquals(now, mappedProduct.updatedAt)

        // Verify Variant mapping
        assertEquals(1L, mappedVariant.id)
        assertEquals(1L, mappedVariant.productId)
        assertEquals("Test Variant", mappedVariant.title)
        assertEquals("TEST-SKU", mappedVariant.sku)
        assertEquals(99.99, mappedVariant.price)
        assertTrue(mappedVariant.available)
        assertEquals("Option 1", mappedVariant.option1)
        assertEquals("Option 2", mappedVariant.option2)
        assertEquals(now, mappedVariant.createdAt)
        assertEquals(now, mappedVariant.updatedAt)

        // Verify final result
        assertNotNull(result)
        assertEquals(1, result.size)
        assertEquals(1, result[0].variants.size)
    }

    @Test
    fun `test getAllProducts returns products with variants`() {
        // Given
        val mockProduct = Product(
            id = 1L,
            title = "Test Product",
            vendor = "Test Vendor",
            productType = "Test Type",
            createdAt = Timestamp.valueOf(LocalDateTime.now()),
            updatedAt = Timestamp.valueOf(LocalDateTime.now())
        )

        val mockVariant = Variant(
            id = 1L,
            productId = 1L,
            title = "Test Variant",
            sku = "TEST-SKU",
            price = 99.99,
            available = true,
            option1 = "Option 1",
            option2 = "Option 2",
            createdAt = Timestamp.valueOf(LocalDateTime.now()),
            updatedAt = Timestamp.valueOf(LocalDateTime.now())
        )

        // Mock JdbcClient behavior for products
        val mockProductQuerySpec = mock(JdbcClient.MappedQuerySpec::class.java) as JdbcClient.MappedQuerySpec<Product>
        val mockProductStatementSpec = mock(JdbcClient.StatementSpec::class.java)

        `when`(jdbcClient.sql(QueryConstants.GET_ALL_PRODUCTS)).thenReturn(mockProductStatementSpec)
        `when`(mockProductStatementSpec.query(any<RowMapper<Product>>())).thenReturn(mockProductQuerySpec)
        `when`(mockProductQuerySpec.list()).thenReturn(listOf(mockProduct))

        // Mock JdbcClient behavior for variants
        val mockVariantQuerySpec = mock(JdbcClient.MappedQuerySpec::class.java) as JdbcClient.MappedQuerySpec<Variant>
        val mockVariantStatementSpec = mock(JdbcClient.StatementSpec::class.java)

        `when`(jdbcClient.sql(QueryConstants.GET_ALL_VARIANTS_OF_A_PRODUCT)).thenReturn(mockVariantStatementSpec)
        `when`(mockVariantStatementSpec.paramSource(any())).thenReturn(mockVariantStatementSpec)
        `when`(mockVariantStatementSpec.query(any<RowMapper<Variant>>())).thenReturn(mockVariantQuerySpec)
        `when`(mockVariantQuerySpec.list()).thenReturn(listOf(mockVariant))

        // When
        val result = productDAO.getAllProducts()

        // Then
        assertNotNull(result)
        assertEquals(1, result.size)
        assertEquals(mockProduct.id, result[0].id)
        assertEquals(1, result[0].variants.size)
        assertEquals(mockVariant.id, result[0].variants[0].id)
    }

    @Test
    fun `test saveProduct creates new product with variants`() {
        // Given
        val productDto = ProductDto(
            id = 0L,
            title = "New Product",
            vendor = "New Vendor",
            type = "New Type",
            variants = listOf(
                VariantDto(
                    id = 0L,
                    title = "New Variant",
                    sku = "NEW-SKU",
                    price = 149.99,
                    available = true,
                    option1 = "New Option 1",
                    option2 = "New Option 2"
                )
            )
        )

        // Mock JdbcClient behavior for product insertion
        val mockProductStatementSpec = mock(JdbcClient.StatementSpec::class.java)
        `when`(jdbcClient.sql(QueryConstants.SAVE_PRODUCT)).thenReturn(mockProductStatementSpec)
        `when`(mockProductStatementSpec.paramSource(any())).thenReturn(mockProductStatementSpec)
        `when`(mockProductStatementSpec.update()).thenReturn(1)

        // Mock JdbcClient behavior for variant insertion
        val mockVariantStatementSpec = mock(JdbcClient.StatementSpec::class.java)
        `when`(jdbcClient.sql(QueryConstants.SAVE_VARIANT)).thenReturn(mockVariantStatementSpec)
        `when`(mockVariantStatementSpec.paramSource(any())).thenReturn(mockVariantStatementSpec)
        `when`(mockVariantStatementSpec.update()).thenReturn(1)

        // When
        val result = productDAO.saveProduct(productDto)

        // Then
        assertNotNull(result)
        verify(jdbcClient, times(1)).sql(QueryConstants.SAVE_PRODUCT)
        verify(jdbcClient, times(productDto.variants.size)).sql(QueryConstants.SAVE_VARIANT)
    }

    @Test
    fun `test getCountOfProducts returns correct count`() {
        // Given
        val expectedCount = 5
        val mockStatementSpec = mock(JdbcClient.StatementSpec::class.java)
        val mockQuerySpec = mock(JdbcClient.MappedQuerySpec::class.java) as JdbcClient.MappedQuerySpec<Int>

        `when`(jdbcClient.sql(QueryConstants.PRODUCT_COUNT)).thenReturn(mockStatementSpec)
        `when`(mockStatementSpec.query(Int::class.java)).thenReturn(mockQuerySpec)
        `when`(mockQuerySpec.single()).thenReturn(expectedCount)

        // When
        val result = productDAO.getCountOfProducts()

        // Then
        assertEquals(expectedCount, result)
        verify(jdbcClient, times(1)).sql(QueryConstants.PRODUCT_COUNT)
    }

    @Test
    fun `test saveProduct if id already exists`() {
        // Given
        val existingProductId = 123L
        val existingVariantId = 456L
        val productDto = ProductDto(
            id = existingProductId,
            title = "Updated Product",
            vendor = "Updated Vendor",
            type = "Updated Type",
            variants = listOf(
                VariantDto(
                    id = existingVariantId,  // Existing variant
                    title = "Updated Variant",
                    sku = "UPD-SKU-1",
                    price = 199.99,
                    available = true,
                    option1 = "Updated Option 1",
                    option2 = "Updated Option 2"
                ),
                VariantDto(
                    id = 0L,  // New variant
                    title = "New Variant",
                    sku = "UPD-SKU-2",
                    price = 299.99,
                    available = true,
                    option1 = "New Option 1",
                    option2 = "New Option 2"
                )
            )
        )

        // Mock JdbcClient behavior
        val mockStatementSpec = mock(JdbcClient.StatementSpec::class.java)
        `when`(jdbcClient.sql(any())).thenReturn(mockStatementSpec)
        `when`(mockStatementSpec.paramSource(any())).thenReturn(mockStatementSpec)
        `when`(mockStatementSpec.update()).thenReturn(1)

        // When
        val result = productDAO.saveProduct(productDto)

        // Then
        assertEquals(existingProductId, result)

        // Verify product save
        verify(jdbcClient, times(1)).sql(QueryConstants.SAVE_PRODUCT)

        // Verify variant saves
        verify(jdbcClient, times(productDto.variants.size)).sql(QueryConstants.SAVE_VARIANT)

        // Create argument captor for MapSqlParameterSource
        val paramSourceCaptor: ArgumentCaptor<MapSqlParameterSource> =
            ArgumentCaptor.forClass(MapSqlParameterSource::class.java)

        // Capture all paramSource calls
        verify(mockStatementSpec, times(3)).paramSource(paramSourceCaptor.capture())

        // Get all captured params
        val capturedParams = paramSourceCaptor.allValues

        // Verify product params
        assertEquals(existingProductId, capturedParams[0].getValue("id"))
        assertEquals("Updated Product", capturedParams[0].getValue("title"))

        // Verify existing variant params
        assertEquals(existingVariantId, capturedParams[1].getValue("id"))
        assertEquals(existingProductId, capturedParams[1].getValue("product_id"))
        assertEquals("UPD-SKU-1", capturedParams[1].getValue("sku"))

        // Verify new variant params
        assertTrue((capturedParams[2].getValue("id") as Long) > 0)
        assertEquals(existingProductId, capturedParams[2].getValue("product_id"))
        assertEquals("UPD-SKU-2", capturedParams[2].getValue("sku"))
    }
}