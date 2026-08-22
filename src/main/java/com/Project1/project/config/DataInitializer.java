package com.Project1.project.config;

import com.Project1.project.entity.Category;
import com.Project1.project.entity.Product;
import com.Project1.project.repository.CategoryRepository;
import com.Project1.project.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
@Profile("!test")
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final com.Project1.project.repository.UserRepository userRepository;
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    public DataInitializer(CategoryRepository categoryRepository,
                           ProductRepository productRepository,
                           com.Project1.project.repository.UserRepository userRepository,
                           org.springframework.security.crypto.password.PasswordEncoder passwordEncoder) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        log.info("Checking and synchronizing multi-category catalog products & image URLs...");

        // Ensure default admin user exists
        if (userRepository.findByEmail("admin@ecommerce.com").isEmpty()) {
            com.Project1.project.entity.User admin = new com.Project1.project.entity.User();
            admin.setName("Admin User");
            admin.setEmail("admin@ecommerce.com");
            admin.setPassword(passwordEncoder.encode("Admin123!"));
            admin.setRole(com.Project1.project.entity.Role.ROLE_ADMIN);
            admin.setEmailVerified(true);
            userRepository.save(admin);
            log.info("Created default administrator account: admin@ecommerce.com");
        }

        // Ensure all seeded/existing accounts have emailVerified = true
        userRepository.findAll().forEach(u -> {
            if (!u.isEmailVerified()) {
                u.setEmailVerified(true);
                userRepository.save(u);
                log.info("Verified email for existing user: {}", u.getEmail());
            }
        });

        // 1. Categories
        Category electronics = findOrCreateCategory("Electronics & Computing", "Laptops, monitors, PC components, and high-tech peripherals");
        Category audio = findOrCreateCategory("Audio & Wearables", "Noise-cancelling headphones, wireless earbuds, and smart accessories");
        Category fashion = findOrCreateCategory("Fashion & Apparel", "Modern minimalist apparel, outerwear, and comfortable footwear");
        Category home = findOrCreateCategory("Home & Kitchen", "Smart appliances, barista essentials, and contemporary home decor");
        Category books = findOrCreateCategory("Books & Stationery", "Software engineering bestsellers, tech guides, and premium journals");

        // 2. Products - Electronics & Computing
        upsertProduct(
                "MacBook Pro 16\" M3 Max",
                "Flagship 16-inch Liquid Retina XDR display with Apple M3 Max chip, 36GB unified memory, and 1TB ultra-fast SSD.",
                new BigDecimal("2499.00"),
                15,
                electronics,
                List.of(
                        "https://images.unsplash.com/photo-1517336714731-489689fd1ca8?auto=format&fit=crop&w=800&q=80",
                        "https://images.unsplash.com/photo-1611186871348-b1ce696e52c9?auto=format&fit=crop&w=800&q=80"
                )
        );

        upsertProduct(
                "Ultra-Wide Curved Gaming Monitor 34\"",
                "34-inch WQHD (3440 x 1440) 144Hz curved gaming display with HDR400, 1ms response time, and USB-C 90W power delivery.",
                new BigDecimal("499.99"),
                25,
                electronics,
                List.of(
                        "https://images.unsplash.com/photo-1527443224154-c4a3942d3acf?auto=format&fit=crop&w=800&q=80"
                )
        );

        upsertProduct(
                "Custom Mechanical Keyboard RGB",
                "Hot-swappable mechanical keyboard featuring Gateron Yellow switches, sound-dampening foam, and PBT double-shot keycaps.",
                new BigDecimal("129.50"),
                40,
                electronics,
                List.of(
                        "https://images.unsplash.com/photo-1587829741301-dc798b83add3?auto=format&fit=crop&w=800&q=80"
                )
        );

        upsertProduct(
                "Keyboard",
                "Compact tactile mechanical keyboard with customizable RGB backlighting and durable aluminum top plate.",
                new BigDecimal("99.99"),
                9,
                electronics,
                List.of(
                        "https://images.unsplash.com/photo-1587829741301-dc798b83add3?auto=format&fit=crop&w=800&q=80"
                )
        );

        upsertProduct(
                "Pro Ergonomic Wireless Mouse",
                "High precision wireless mouse with rechargeable battery, silent clicks, and ergonomic palm contouring.",
                new BigDecimal("1499.00"),
                14,
                electronics,
                List.of(
                        "https://images.unsplash.com/photo-1527864550417-7fd91fc51a46?auto=format&fit=crop&w=800&q=80"
                )
        );

        // 3. Products - Audio & Wearables
        upsertProduct(
                "Sony WH-1000XM5 Wireless Headphones",
                "Industry-leading active noise cancellation with Auto NC Optimizer, 30-hour battery life, and crystal clear hands-free calling.",
                new BigDecimal("348.00"),
                30,
                audio,
                List.of(
                        "https://images.unsplash.com/photo-1505740420928-5e560c06d30e?auto=format&fit=crop&w=800&q=80",
                        "https://images.unsplash.com/photo-1484704849700-f032a568e944?auto=format&fit=crop&w=800&q=80"
                )
        );

        upsertProduct(
                "AirPods Pro (2nd Generation)",
                "Active Noise Cancellation with Adaptive Audio, Transparency mode, Personalized Spatial Audio, and MagSafe USB-C Case.",
                new BigDecimal("229.00"),
                50,
                audio,
                List.of(
                        "https://images.unsplash.com/photo-1600294037681-c80b4cb5b434?auto=format&fit=crop&w=800&q=80"
                )
        );

        upsertProduct(
                "GPS Fitness Smartwatch Series 9",
                "Advanced health tracking with ECG, Blood Oxygen monitoring, Always-On Retina display, and 50m water resistance.",
                new BigDecimal("399.00"),
                20,
                audio,
                List.of(
                        "https://images.unsplash.com/photo-1523275335684-37898b6baf30?auto=format&fit=crop&w=800&q=80"
                )
        );

        // 4. Products - Fashion & Apparel
        upsertProduct(
                "Classic Washed Denim Jacket",
                "Tailored 100% heavyweight cotton denim jacket with reinforced brass buttons and signature dual chest flap pockets.",
                new BigDecimal("89.00"),
                35,
                fashion,
                List.of(
                        "https://images.unsplash.com/photo-1576995853123-5a10305d93c0?auto=format&fit=crop&w=800&q=80"
                )
        );

        upsertProduct(
                "Premium Organic Cotton Hoodie",
                "Ultra-soft 450 GSM French Terry fleece hooded sweatshirt with double-layered hood and relaxed unisex fit.",
                new BigDecimal("64.99"),
                75,
                fashion,
                List.of(
                        "https://images.unsplash.com/photo-1556905055-8f358a7a47b2?auto=format&fit=crop&w=800&q=80"
                )
        );

        upsertProduct(
                "All-Terrain Cushioned Running Shoes",
                "Engineered breathable mesh running sneakers with responsive nitrogen-infused foam midsole and high-traction rubber outsole.",
                new BigDecimal("139.95"),
                45,
                fashion,
                List.of(
                        "https://images.unsplash.com/photo-1542291026-7eec264c27ff?auto=format&fit=crop&w=800&q=80"
                )
        );

        // 5. Products - Home & Kitchen
        upsertProduct(
                "Precision Automatic Espresso Machine",
                "Commercial-grade 15-bar Italian pump espresso machine with integrated conical burr grinder and stainless micro-foam steam wand.",
                new BigDecimal("699.95"),
                10,
                home,
                List.of(
                        "https://images.unsplash.com/photo-1517668808822-9ebb02f2a0e6?auto=format&fit=crop&w=800&q=80"
                )
        );

        upsertProduct(
                "Insulated Stainless Steel Bottle (1L)",
                "Vacuum double-wall insulation keeps beverages ice cold for 24 hours or piping hot for 12 hours. BPA-free powder coat finish.",
                new BigDecimal("29.99"),
                120,
                home,
                List.of(
                        "https://images.unsplash.com/photo-1602143407151-7111542de6e8?auto=format&fit=crop&w=800&q=80"
                )
        );

        upsertProduct(
                "Dimmable Minimalist Desk Lamp",
                "Warm architectural LED desk lamp with touch sensor brightness control, anodized aluminum arm, and integrated wireless charging pad.",
                new BigDecimal("54.00"),
                40,
                home,
                List.of(
                        "https://images.unsplash.com/photo-1507473885765-e6ed057f782c?auto=format&fit=crop&w=800&q=80"
                )
        );

        // 6. Products - Books & Stationery
        upsertProduct(
                "Clean Architecture by Robert C. Martin",
                "A Craftsman's Guide to Software Structure and Design. Essential reading for building resilient, decoupled software systems.",
                new BigDecimal("36.50"),
                50,
                books,
                List.of(
                        "https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?auto=format&fit=crop&w=800&q=80"
                )
        );

        upsertProduct(
                "Hardcover Dot-Grid Leather Journal",
                "Archival quality 160 GSM bleed-proof dotted paper with vegan leather hardcover, elastic closure band, and ribbon bookmark.",
                new BigDecimal("19.99"),
                85,
                books,
                List.of(
                        "https://images.unsplash.com/photo-1589829085413-56de8ae18c73?auto=format&fit=crop&w=800&q=80"
                )
        );

        log.info("Catalog synchronization completed! Active products in DB: {}", productRepository.count());
    }

    private Category findOrCreateCategory(String name, String description) {
        return categoryRepository.findByName(name).orElseGet(() -> {
            Category category = new Category();
            category.setName(name);
            category.setDescription(description);
            return categoryRepository.save(category);
        });
    }

    private void upsertProduct(String name, String description, BigDecimal price, int stock, Category category, List<String> imageUrls) {
        Optional<Product> existingOpt = productRepository.findAll().stream()
                .filter(p -> p.getName().trim().equalsIgnoreCase(name.trim()) || p.getName().toLowerCase().contains(name.toLowerCase()))
                .findFirst();

        if (existingOpt.isPresent()) {
            Product existing = existingOpt.get();
            boolean updated = false;

            // Correct category association if mismatched
            if (existing.getCategory() == null || !existing.getCategory().getId().equals(category.getId())) {
                existing.setCategory(category);
                updated = true;
            }

            // Correct empty or missing imageUrls
            if (existing.getImageUrls() == null || existing.getImageUrls().isEmpty()) {
                existing.setImageUrls(new ArrayList<>(imageUrls));
                updated = true;
            }

            if (updated) {
                productRepository.save(existing);
                log.info("Updated product [{}] with correct category [{}] and {} image URLs.",
                        existing.getName(), category.getName(), existing.getImageUrls().size());
            }
            return;
        }

        Product p = new Product();
        p.setName(name);
        p.setDescription(description);
        p.setPrice(price);
        p.setStockQuantity(stock);
        p.setCategory(category);
        p.setImageUrls(new ArrayList<>(imageUrls));
        productRepository.save(p);
        log.info("Created new product [{}] in category [{}].", name, category.getName());
    }
}
