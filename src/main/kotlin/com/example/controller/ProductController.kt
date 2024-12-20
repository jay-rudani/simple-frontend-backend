package com.example.controller

import com.example.dto.ProductDto
import com.example.service.ProductService
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PostMapping

/**
 * Controller that handles product-related HTTP requests.
 *
 * This class is responsible for processing requests related to products, such as displaying
 * the list of products, adding a new product, and rendering the corresponding views.
 */
@Controller
class ProductController(

    // Service for handling product-related business logic
    val productService: ProductService
) {

    /**
     * Handles GET requests to the "/test" endpoint and displays a list of products.
     *
     * This method adds a list of products to the model and returns the view name "products"
     * which will render the products page.
     *
     * @param model The model object that holds attributes to be passed to the view.
     * @return The view name "products" to render the product list.
     */
    @GetMapping("/test")
    fun getProducts(model: Model): String {
        model.addAttribute("products", productService.getAllProducts())
        return "products"
    }

    /**
     * Handles GET requests to the "/test/table" endpoint and returns a fragment of the products table.
     *
     * This method adds the list of products to the model and returns a view fragment (products-table)
     * to be inserted into an existing page.
     *
     * @param model The model object that holds attributes to be passed to the view.
     * @return The view fragment "products :: products-table" to render the products table.
     */
    @GetMapping("/test/table")
    fun getProductsTable(model: Model): String {
        model.addAttribute("products", productService.getAllProducts())
        return "products :: products-table"
    }

    /**
     * Handles POST requests to the "/test/product" endpoint to add a new product.
     *
     * This method takes the product data from the form submission, saves the new product via
     * the product service, and returns a view fragment (products-table) with the updated list of products.
     *
     * @param productDto The product data submitted by the user.
     * @param model The model object that holds attributes to be passed to the view.
     * @return The view fragment "products :: products-table" to render the updated products table.
     */
    @PostMapping("/test/product")
    fun addProduct(@ModelAttribute productDto: ProductDto, model: Model): String {
        productService.saveProduct(productDto)
        model.addAttribute("products", productService.getAllProducts())
        return "products :: products-table"
    }
}