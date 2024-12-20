package com.example.model

import java.sql.Timestamp

/**
 * Data class representing a variant of a product.
 *
 * This class encapsulates the details of a variant associated with a product,
 * including its unique identifier, product ID, SKU, price, availability status,
 * option details, and creation/update timestamps.
 *
 * @property id The unique identifier of the variant.
 * @property productId The identifier of the product to which this variant belongs.
 * @property title The title or name of the variant.
 * @property sku The Stock Keeping Unit (SKU) identifier of the variant.
 * @property price The price of the variant.
 * @property available The availability status of the variant (true if available, false otherwise).
 * @property option1 The first option associated with the variant, such as size or color.
 * @property option2 The second option associated with the variant, such as material or style.
 * @property createdAt The timestamp when the variant was created.
 * @property updatedAt The timestamp when the variant was last updated.
 */
data class Variant(
    val id: Long,
    val productId: Long,
    val title: String,
    val sku: String,
    val price: Double,
    val available: Boolean,
    val option1: String,
    val option2: String,
    val createdAt: Timestamp,
    val updatedAt: Timestamp
)
