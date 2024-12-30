@file:Suppress("SqlSourceToSinkFlow")

package com.example.dao

import com.example.constants.QueryConstants
import com.example.dto.ProductDto
import com.example.dto.VariantDto
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.util.*

/**
 * DAO class responsible for handling database operations related to products and their variants.
 * This class uses `JdbcClient` for executing SQL queries and mapping the results to domain objects.
 *
 * The methods include:
 * - `getAllProducts()`: Retrieves all products from the database along with their variants.
 * - `saveProduct()`: Saves a product and its associated variants to the database.
 * - `getCountOfProducts()`: Retrieves the count of products in the database.
 *
 * @param jdbcClient The JdbcClient used to interact with the database.
 */
@Component
class ProductDAO(
    private val jdbcClient: JdbcClient
) {

    // Logger to log important events and errors
    val logger: Logger = LoggerFactory.getLogger(ProductDAO::class.java)

    /**
     * Retrieves all products from the database along with their variants.
     *
     * This method executes two queries: one to fetch all products and another to fetch variants for each product.
     * It then associates the variants with their respective products and returns a list of products with their variants.
     *
     * @return A list of all products with their associated variants.
     */
    fun getAllProducts(): List<ProductDto> {

        logger.info("DAO Layer:: Executing getAllProducts()")

        // Executing the query to get products
        val products = jdbcClient.sql(
            QueryConstants.GET_ALL_PRODUCTS
        )
            .query { rs, _ ->
                ProductDto(
                    id = rs.getLong("id"),
                    title = rs.getString("title"),
                    vendor = rs.getString("vendor"),
                    type = rs.getString("type"),
                    createdAt = rs.getTimestamp("created_at"),
                    updatedAt = rs.getTimestamp("updated_at")
                )
            }
            .list()
            .toList()

        logger.info("getAllProducts(): Products has been fetched")

        logger.info("getAllProducts():: Mapping all variants with its product")

        // For each product, get its associated variants
        return products.map { product ->

            // Adding parameters for sql query
            val params = MapSqlParameterSource()
                .addValue("id", product.id)

            // Executing the query to get variants for each product with params
            val variants = jdbcClient.sql(
                QueryConstants.GET_ALL_VARIANTS_OF_A_PRODUCT
            )
                .paramSource(params)
                .query { rs, _ ->
                    VariantDto(
                        id = rs.getLong("id"),
                        productId = rs.getLong("product_id"),
                        title = rs.getString("title"),
                        sku = rs.getString("sku"),
                        price = rs.getDouble("price"),
                        available = rs.getBoolean("available"),
                        option1 = rs.getString("option1"),
                        option2 = rs.getString("option2"),
                        createdAt = rs.getTimestamp("created_at"),
                        updatedAt = rs.getTimestamp("updated_at")
                    )
                }
                .list()

            // Return product with its associated variants
            product.copy(variants = variants)
        }
    }

    /**
     * Saves a product and its associated variants to the database.
     *
     * This method first inserts the product data into the product table and then inserts each associated variant
     * into the variant table. If a product or variant has an ID of 0, a new ID is generated.
     *
     * @param productDto The data transfer object containing the product and its variants to be saved.
     * @return The unique identifier (ID) of the saved product.
     */
    fun saveProduct(productDto: ProductDto): Long {

        logger.info("DAO Layer:: Executing saveProduct(productDto)")

        // Get date and time
        val now = LocalDateTime.now()

        // Generate a new product ID
        val productId = if (productDto.id == 0L) {
            System.currentTimeMillis() + Random().nextInt(1000)
        } else productDto.id

        // Adding parameters for sql query
        var params = MapSqlParameterSource()
            .addValue("id", productId)
            .addValue("title", productDto.title)
            .addValue("vendor", productDto.vendor)
            .addValue("type", productDto.type)
            .addValue("created_at", now)
            .addValue("updated_at", now)

        // Executing the query with params
        jdbcClient.sql(
            QueryConstants.SAVE_PRODUCT
        )
            .paramSource(params)
            .update()

        logger.info("saveProduct(productDto): Products has been saved")

        // Insert each variant associated with the product into the database
        productDto.variants.forEach { variant ->

            // Generate a new variant ID
            val variantId = if (variant.id == 0L) {
                System.currentTimeMillis() + Random().nextInt(1000)
            } else variant.id

            // Adding parameters for sql query
            params = MapSqlParameterSource()
                .addValue("id", variantId)
                .addValue("product_id", productId)
                .addValue("title", variant.title)
                .addValue("sku", variant.sku)
                .addValue("price", variant.price)
                .addValue("available", variant.available)
                .addValue("option1", variant.option1)
                .addValue("option2", variant.option2)
                .addValue("created_at", now)
                .addValue("updated_at", now)

            // Executing the query with params
            jdbcClient.sql(
                QueryConstants.SAVE_VARIANT
            )
                .paramSource(params)
                .update()
        }

        logger.info("saveProduct(productDto): Variants have been saved & execution completed")

        return productId
    }

    /**
     * Retrieves the count of products in the database.
     *
     * @return The number of products in the database.
     */
    fun getCountOfProducts(): Int {

        logger.info("DAO Layer:: Executing getCountOfProducts()")

        return jdbcClient.sql(QueryConstants.PRODUCT_COUNT)
            .query(Int::class.java)
            .single()
    }
}