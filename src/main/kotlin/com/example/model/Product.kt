package com.example.model

import java.sql.Timestamp

/**
 * Data class representing a product in the system.
 *
 * This class encapsulates the details of a product, including its ID, title, vendor,
 * product type, creation and update timestamps, and associated variants.
 * It is typically used to represent a product entity in a database or for transferring
 * product data between different layers of the application.
 *
 * @property id The unique identifier of the product.
 * @property title The title or name of the product.
 * @property vendor The vendor or manufacturer of the product.
 * @property productType The type or category of the product.
 * @property createdAt The timestamp when the product was created.
 * @property updatedAt The timestamp when the product was last updated.
 * @property variants A list of variants associated with the product.
 */
data class Product(
    val id: Long,
    val title: String,
    val vendor: String,
    val productType: String,
    val createdAt: Timestamp,
    val updatedAt: Timestamp,
    val variants: List<Variant> = emptyList()
)
