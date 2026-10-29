package com.example.demo.service;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.example.demo.model.DashboardSummary;
import com.example.demo.model.Product;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProductServiceTest {

    private final ProductService productService = new ProductService();

    @Test
    void catalogueIsSeededWithSixMockedProducts() {
        assertThat(productService.getAllProducts()).hasSize(6);
    }

    @Test
    void addProductAppendsAndGeneratesTheNextId() {
        Product created = productService.addProduct("Orion Tablet 11", "Computing", 399.90, 15);

        assertThat(created.id()).isEqualTo("P-2007");
        assertThat(productService.getAllProducts()).hasSize(7);
        assertThat(productService.getProductById("P-2007")).isEqualTo(created);
    }

    @Test
    void addProductGeneratesSequentialIds() {
        Product first = productService.addProduct("Orion Tablet 11", "Computing", 399.90, 15);
        Product second = productService.addProduct("Orion Tablet 13", "Computing", 499.90, 10);

        assertThat(first.id()).isEqualTo("P-2007");
        assertThat(second.id()).isEqualTo("P-2008");
    }

    @Test
    void addProductRoundsThePriceToTwoDecimals() {
        Product created = productService.addProduct("Mystery Gadget", "Accessories", 19.999, 3);

        assertThat(created.price()).isEqualTo(20.00);
    }

    @Test
    void addProductAllowsDuplicateNames() {
        Product first = productService.addProduct("Pulse Phone X", "Phones", 100, 1);
        Product second = productService.addProduct("Pulse Phone X", "Phones", 100, 1);

        assertThat(first.id()).isNotEqualTo(second.id());
        assertThat(productService.getAllProducts()).hasSize(8);
    }

    @Test
    void addProductRejectsInvalidInput() {
        assertThatThrownBy(() -> productService.addProduct("  ", "Phones", 10, 1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("name");

        assertThatThrownBy(() -> productService.addProduct("Phone", "", 10, 1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("category");

        assertThatThrownBy(() -> productService.addProduct("Phone", "Phones", -1, 1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("price");

        assertThatThrownBy(() -> productService.addProduct("Phone", "Phones", 10, -1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("stock");

        assertThat(productService.getAllProducts()).hasSize(6);
    }

    @Test
    void getProductByIdMatchesIgnoringCase() {
        assertThat(productService.getProductById("p-2003").name()).isEqualTo("Pulse Phone X");
    }

    @Test
    void getProductByIdFailsForUnknownId() {
        assertThatThrownBy(() -> productService.getProductById("P-9999"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("P-9999");
    }

    @Test
    void searchesStillSeeTheAddedProduct() {
        productService.addProduct("Nimbus Laptop 13", "Computing", 1099.00, 5);

        List<Product> computing = productService.getProductsByCategory("computing");
        assertThat(computing).hasSize(3);
        assertThat(computing).extracting(Product::name).contains("Nimbus Laptop 13");

        assertThat(productService.getLowStockProducts(5)).extracting(Product::id).contains("P-2007");
    }

    @Test
    void dashboardSummaryReflectsTheAddedProduct() {
        ProductService products = new ProductService();
        DashboardService dashboard = new DashboardService(new UserService(), products);

        products.addProduct("Nimbus Laptop 13", "Computing", 1000.00, 10);

        DashboardSummary summary = dashboard.getDashboardSummary();

        assertThat(summary.totalProducts()).isEqualTo(7);
        assertThat(summary.categories()).isEqualTo(3);
        assertThat(summary.inventoryValue()).isEqualTo(149039.0);
    }

    @Test
    void serverInfoExposesTheCurrentToolCount() {
        DashboardService dashboard = new DashboardService(new UserService(), productService);

        assertThat(dashboard.getServerInfo().exposedTools()).isEqualTo(DashboardService.EXPOSED_TOOLS);
        assertThat(DashboardService.EXPOSED_TOOLS).isEqualTo(10);
    }
}
