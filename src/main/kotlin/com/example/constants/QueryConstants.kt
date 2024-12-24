package com.example.constants

/**
 * A utility object that contains SQL query constants used for interacting with the database.
 * These queries are used for retrieving and saving data related to products and variants.
 *
 * The queries include:
 * - `GET_ALL_PRODUCTS`: Retrieves all products from the products table.
 * - `GET_ALL_VARIANTS_OF_A_PRODUCT`: Retrieves all variants of a specific product based on the product ID.
 * - `SAVE_PRODUCT`: Inserts a new product into the products table.
 * - `SAVE_VARIANT`: Inserts a new variant associated with a product into the variants table.
 * - `PRODUCT_COUNT`: Retrieves the total count of products in the products table.
 */
object QueryConstants {

    /**
     * SQL query to retrieve all products from the products table.
     *
     * This query selects the columns: id, title, vendor, type, created_at, and updated_at.
     */
    val GET_ALL_PRODUCTS =
        """
            SELECT id, title, vendor, type, created_at, updated_at 
            FROM products
        """.trimIndent()

    /**
     * SQL query to retrieve all variants of a specific product by product ID.
     *
     * This query selects the columns: id, product_id, title, sku, price, available, option1, option2, created_at, and updated_at.
     * The query filters the variants by the `product_id` parameter.
     */
    val GET_ALL_VARIANTS_OF_A_PRODUCT =
        """
            SELECT id, product_id, title, sku, price, available, 
                   option1, option2, created_at, updated_at
            FROM variants 
            WHERE product_id = :id
        """.trimIndent()

    /**
     * SQL query to insert a new product into the products table.
     *
     * This query inserts the values for the columns: id, title, vendor, type, created_at, and updated_at.
     */
    val SAVE_PRODUCT =
        """
            INSERT INTO products (id, title, vendor, type, created_at, updated_at)
            VALUES (:id, :title, :vendor, :type, :created_at, :updated_at)
        """.trimIndent()

    /**
     * SQL query to insert a new variant associated with a product into the variants table.
     *
     * This query inserts the values for the columns: id, product_id, title, sku, price, available, option1, option2, created_at, and updated_at.
     * The `product_id` is used to associate the variant with a specific product.
     */
    val SAVE_VARIANT =
        """
            INSERT INTO variants (
                    id, product_id, title, sku, price, available,
                    option1, option2, created_at, updated_at
                )
                VALUES (:id, :product_id, :title, :sku, :price, :available, :option1, :option2, :created_at, :updated_at)
        """.trimIndent()

    /**
     * SQL query to retrieve the count of products in the products table.
     *
     * This query returns the total number of rows in the `products` table.
     */
    val PRODUCT_COUNT =
        """
            SELECT COUNT(*) FROM products
        """.trimIndent()
}