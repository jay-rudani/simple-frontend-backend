package com.example.service

import com.example.dao.ProductDAO
import com.example.dto.ProductDto
import com.example.dto.VariantDto
import com.example.model.Product
import com.fasterxml.jackson.databind.JsonNode
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import java.time.LocalDateTime

/**
 * Service class responsible for managing products in the application.
 *
 * This class contains methods to fetch, save, and retrieve products. It includes a scheduled task
 * to fetch and save products from an external API if no products exist in the database.
 *
 * The methods include:
 * - `fetchAndSaveProducts()`: Fetches product data from an external API and saves the first 10 products.
 * - `getAllProducts()`: Retrieves all products stored in the database.
 * - `saveProduct()`: Saves a given product to the database.
 *
 * The class also interacts with `ProductDAO` for database operations and uses `RestTemplate`
 * for fetching data from the external API.
 *
 * @param restTemplate The RestTemplate used to fetch data from external APIs.
 * @param productDAO The DAO responsible for product-related database operations.
 */
@Service
@EnableScheduling
class ProductService(
    private val restTemplate: RestTemplate,
    private val productDAO: ProductDAO
) {
    // Logger to log important events and errors
    val logger: Logger = LoggerFactory.getLogger(ProductService::class.java)

    /**
     * Scheduled task to fetch and save products from an external API.
     *
     * If no products exist in the database, it fetches product data from the API and
     * processes the first 10 products to save them along with their variants.
     *
     * This method is annotated with `@Scheduled` to run periodically, starting immediately.
     */
    @Scheduled(initialDelay = 0)
    fun fetchAndSaveProducts() {
        try {

            // Check if products already exist in the database
            val productCount = productDAO.getCountOfProducts()

            if (productCount == 0) {
                logger.info("No products found in database. Starting initial data fetch...")

                // Fetch product data from the external API
                val response = restTemplate.getForObject("https://famme.no/products.json", JsonNode::class.java)

                response?.get("products")?.let { products ->

                    // Process and save the first 10 products
                    products.take(10).forEach { product ->
                        val now = LocalDateTime.now()

                        // Product id for variants
                        val productId = product["id"].asLong()

                        // Create list of variants for a product
                        val listOfVariantDtos = mutableListOf(VariantDto())

                        // Adding variants to the list
                        product["variants"].forEach { variantNode ->

                            val variantDto = VariantDto(
                                id = variantNode["id"].asLong(),
                                productId = productId,
                                title = variantNode["title"].asText(),
                                sku = variantNode["sku"].asText(),
                                price = variantNode["price"].asDouble(),
                                available = variantNode["available"].asBoolean(),
                                option1 = variantNode["option1"].asText(),
                                option2 = variantNode["option2"].asText(),
                                now,
                                now
                            )
                            listOfVariantDtos.add(variantDto)
                        }

                        // Create product dto to save
                        val productDto = ProductDto(
                            id = productId,
                            title = product["title"].asText(),
                            vendor = product["vendor"].asText(),
                            type = product["product_type"].asText(),
                            listOfVariantDtos,
                            now,
                            now
                        )
                        productDAO.saveProduct(productDto)

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
     * Retrieves all the products from the database.
     *
     * @return A list of all products.
     */
    fun getAllProducts(): List<Product> {

        logger.info("Service Layer:: Executing getAllProducts()")

        // Calling ProductDAO method to get all products
        return productDAO.getAllProducts()
    }

    /**
     * Saves the given product information into the database.
     *
     * @param productDto The data transfer object containing product details to be saved.
     * @return The unique identifier (ID) of the saved product.
     */
    fun saveProduct(productDto: ProductDto): Long {

        logger.info("Service Layer:: Executing saveProduct(productDto)")

        // Calling ProductDAO method to get all products
        return productDAO.saveProduct(productDto)
    }
}