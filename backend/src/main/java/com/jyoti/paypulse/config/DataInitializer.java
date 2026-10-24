package com.jyoti.paypulse.config;

import com.jyoti.paypulse.order.dto.CreateOrderRequest;
import com.jyoti.paypulse.order.model.Product;
import com.jyoti.paypulse.order.repository.ProductRepository;
import com.jyoti.paypulse.order.service.OrderService;
import com.jyoti.paypulse.payment.model.Wallet;
import com.jyoti.paypulse.payment.repository.WalletRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.UUID;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private OrderService orderService;

    @Override
    public void run(String... args) throws Exception {
        if (productRepository.count() > 0) return;

        // 1. Seed Products Catalog
        Product p1 = new Product(
                "Mechanical Gaming Keyboard",
                "Hot-swappable tactile mechanical switches with per-key RGB backlighting and braided USB-C cable.",
                new BigDecimal("2499.00"),
                50,
                "Peripherals",
                "https://images.unsplash.com/photo-1587829741301-dc798b83add3?w=500&q=80"
        );

        Product p2 = new Product(
                "UltraWide 4K Productivity Monitor",
                "34-inch curved IPS panel with 144Hz refresh rate, HDR400, and 90W USB-C Power Delivery.",
                new BigDecimal("14999.00"),
                20,
                "Displays",
                "https://images.unsplash.com/photo-1527443224154-c4a3942d3acf?w=500&q=80"
        );

        Product p3 = new Product(
                "Wireless ANC Studio Headphones",
                "Active Noise Cancellation with 40mm Neodymium drivers, 35-hour battery life, and spatial audio.",
                new BigDecimal("3999.00"),
                35,
                "Audio",
                "https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=500&q=80"
        );

        Product p4 = new Product(
                "Ergonomic Lumbar Desk Chair",
                "Breathable mesh back with multi-directional 3D armrests and pneumatic height adjustment.",
                new BigDecimal("6499.00"),
                15,
                "Furniture",
                "https://images.unsplash.com/photo-1586023492125-27b2c045efd7?w=500&q=80"
        );

        Product p5 = new Product(
                "Minimalist Smart LED Desk Lamp",
                "Custom color temperature (2700K - 6500K) with touch dimming, auto-sleep timer, and wireless phone charger.",
                new BigDecimal("1199.00"),
                40,
                "Lighting",
                "https://images.unsplash.com/photo-1507473885765-e6ed057f782c?w=500&q=80"
        );

        productRepository.saveAll(Arrays.asList(p1, p2, p3, p4, p5));

        // 2. Seed Default User Wallet if not present
        if (walletRepository.findByUserEmail("jyoti@paypulse.io").isEmpty()) {
            Wallet wallet = new Wallet("jyoti@paypulse.io", new BigDecimal("15000.00"));
            walletRepository.save(wallet);
        }

        // 3. Seed Initial Orders to demonstrate the workflow
        CreateOrderRequest ord1 = new CreateOrderRequest();
        ord1.setProductId(p1.getId());
        ord1.setQuantity(1);
        ord1.setCustomerEmail("jyoti@paypulse.io");
        ord1.setIdempotencyKey("SEED-IDEMP-" + UUID.randomUUID().toString().substring(0, 8));
        orderService.createOrder(ord1);

        CreateOrderRequest ord2 = new CreateOrderRequest();
        ord2.setProductId(p3.getId());
        ord2.setQuantity(1);
        ord2.setCustomerEmail("jyoti@paypulse.io");
        ord2.setIdempotencyKey("SEED-IDEMP-" + UUID.randomUUID().toString().substring(0, 8));
        orderService.createOrder(ord2);

        System.out.println(">>> [PayPulse] Database successfully seeded with catalog, wallet, and initial transactions!");
    }
}
