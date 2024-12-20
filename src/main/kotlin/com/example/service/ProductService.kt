package com.example.service

import com.example.dto.ProductDto
import com.example.model.Product
import com.example.model.Variant
import com.fasterxml.jackson.databind.JsonNode
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import java.time.LocalDateTime
import java.util.*

/**
 * Service class responsible for managing product-related operations.
 *
 * This service fetches products from an external API, saves them to the database,
 * and provides methods to retrieve products and save new ones.
 * It also includes a scheduled task to fetch and store products periodically.
 *
 * @param restTemplate The RestTemplate used to make API calls to fetch products.
 * @param jdbcClient The JdbcClient used to interact with the database.
 */
@Service
@EnableScheduling
class ProductService(
    val restTemplate: RestTemplate,
    val jdbcClient: JdbcClient
) {
    // Logger to log important events and errors
    val logger: Logger = LoggerFactory.getLogger(ProductService::class.java)

    /**
     * Scheduled task that fetches and saves products from an external API if no products exist in the database.
     *
     * This method is executed at a fixed interval and performs an initial data fetch from an external API
     * when no products are found in the database. It inserts both product and variant data into the database.
     */
    @Scheduled(initialDelay = 0)
    fun fetchAndSaveProducts() {
        try {

            // Check if products already exist in the database
            val productCount = jdbcClient.sql("SELECT COUNT(*) FROM products")
                .query(Int::class.java)
                .single()

            if (productCount == 0) {
                logger.info("No products found in database. Starting initial data fetch...")

                // Fetch product data from the external API
                val response = restTemplate.getForObject("https://famme.no/products.json", JsonNode::class.java)

                response?.get("products")?.let { products ->

                    // Process and save the first 10 products
                    products.take(10).forEach { product ->
                        val now = LocalDateTime.now()

                        // Insert product data into the database
                        val productId = product["id"].asLong()
                        jdbcClient.sql(
                            """
                        INSERT INTO products (id, title, vendor, type, created_at, updated_at)
                        VALUES (?, ?, ?, ?, ?, ?)
                        """
                        )
                            .params(
                                productId,
                                product["title"].asText(),
                                product["vendor"].asText(),
                                product["product_type"].asText(),
                                now,
                                now
                            )
                            .update()
                        logger.info("Product saved successfully")

                        // Insert variant data for each product
                        product["variants"].forEach { variantNode ->
                            jdbcClient.sql(
                                """
                            INSERT INTO variants (
                                id, product_id, title, sku, price, available,
                                option1, option2, created_at, updated_at
                            )
                            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                            """
                            )
                                .params(
                                    variantNode["id"].asLong(),
                                    productId,
                                    variantNode["title"].asText(),
                                    variantNode["sku"].asText(),
                                    variantNode["price"].asDouble(),
                                    variantNode["available"].asBoolean(),
                                    variantNode["option1"].asText(),
                                    variantNode["option2"].asText(),
                                    now,
                                    now
                                )
                                .update()
                        }
                        logger.info("Product variants saved successfully")
                    }
                }
            } else {
                logger.info("Products already exist in database. Skipping initial data fetch.")
            }
        } catch (e: Exception) {
            logger.error("Error during product fetch and save: ${e.message}", e)
        }
    }

    /**
     * Retrieves all products from the database, including their associated variants.
     *
     * This method first retrieves all products from the `products` table and then fetches
     * the variants for each product from the `variants` table. The resulting product list
     * is returned with their associated variants.
     *
     * @return A list of products with their variants.
     */
    fun getAllProducts(): List<Product> {

        logger.info("Getting all products...")

        // Get all products from the database
        val products = jdbcClient.sql(
            """
            SELECT id, title, vendor, type, created_at, updated_at 
            FROM products
        """
        )
            .query { rs, _ ->
                Product(
                    id = rs.getLong("id"),
                    title = rs.getString("title"),
                    vendor = rs.getString("vendor"),
                    productType = rs.getString("type"),
                    createdAt = rs.getTimestamp("created_at"),
                    updatedAt = rs.getTimestamp("updated_at")
                )
            }
            .list()

        // For each product, get its associated variants
        return products.map { product ->
            val variants = jdbcClient.sql(
                """
                SELECT id, product_id, title, sku, price, available, 
                       option1, option2, created_at, updated_at
                FROM variants 
                WHERE product_id = ?
                """
            )
                .param(product.id)
                .query { rs, _ ->
                    Variant(
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
     * Saves a new product and its variants into the database.
     *
     * This method inserts a new product into the `products` table and then inserts its variants
     * into the `variants` table. A new product ID is generated based on the current system time
     * and variants are also assigned unique IDs.
     *
     * @param productDto The product data transfer object containing product and variant details.
     * @return The ID of the newly created product.
     */
    fun saveProduct(productDto: ProductDto): Long {
        val now = LocalDateTime.now()

        // Generate a new product ID
        val productId = System.currentTimeMillis() + Random().nextInt(1000)

        // Insert product into the database
        jdbcClient.sql(
            """
            INSERT INTO products (id, title, vendor, type, created_at, updated_at)
            VALUES (?, ?, ?, ?, ?, ?)
            """
        )
            .params(
                productId,
                productDto.title,
                productDto.vendor,
                productDto.type,
                now,
                now
            )
            .update()

        // Insert each variant associated with the product into the database
        productDto.variants.forEach { variant ->
            val variantId = System.currentTimeMillis() + Random().nextInt(1000)
            jdbcClient.sql(
                """
                INSERT INTO variants (
                    id, product_id, title, sku, price, available,
                    option1, option2, created_at, updated_at
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """
            )
                .params(
                    variantId,
                    productId,
                    variant.title,
                    variant.sku,
                    variant.price,
                    variant.available,
                    variant.option1,
                    variant.option2,
                    now,
                    now
                )
                .update()
        }

        // Return the newly created product ID
        return productId
    }
}