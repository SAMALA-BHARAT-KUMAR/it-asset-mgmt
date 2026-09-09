package com.itasset.assetservice;

import com.itasset.assetservice.entity.Asset;
import com.itasset.assetservice.entity.Category;
import com.itasset.assetservice.entity.Location;
import com.itasset.assetservice.entity.User;
import com.itasset.assetservice.enums.AssetStatus;
import com.itasset.assetservice.enums.Role;
import com.itasset.assetservice.repository.AssetRepository;
import com.itasset.assetservice.repository.CategoryRepository;
import com.itasset.assetservice.repository.LocationRepository;
import com.itasset.assetservice.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

// runs once on startup: fills empty tables with sample data so the app is testable
@Component
public class DataSeeder implements CommandLineRunner {

    private final CategoryRepository categoryRepository;
    private final LocationRepository locationRepository;
    private final AssetRepository assetRepository;
    private final UserRepository userRepository;

    public DataSeeder(CategoryRepository categoryRepository,
                      LocationRepository locationRepository,
                      AssetRepository assetRepository,
                      UserRepository userRepository) {
        this.categoryRepository = categoryRepository;
        this.locationRepository = locationRepository;
        this.assetRepository = assetRepository;
        this.userRepository = userRepository;
    }

    @Override
    public void run(String... args) {
        // already seeded? skip inserting, just report — avoids duplicates on every restart
        if (categoryRepository.count() == 0) {
            seed();
        }
        if (userRepository.count() == 0) {
            seedUsers();
        }
        // checkpoint: read the counts back through each repository
        System.out.printf("Seeded data -> categories: %d, locations: %d, assets: %d, users: %d%n",
                categoryRepository.count(), locationRepository.count(), assetRepository.count(),
                userRepository.count());
    }

    private void seed() {
        // 5 categories
        List<Category> categories = List.of(
                category("Laptop", "Portable computers issued to staff"),
                category("Monitor", "External displays"),
                category("Printer", "Shared office printers"),
                category("Server", "Rack-mounted servers"),
                category("Phone", "Company mobile phones"));
        categoryRepository.saveAll(categories);

        // 3 locations
        List<Location> locations = List.of(
                location("HQ Tower", "3", "301"),
                location("HQ Tower", "5", "512"),
                location("Warehouse", "1", "W-01"));
        locationRepository.saveAll(locations);

        // 10 assets spread across categories, locations, and every status
        AssetStatus[] statuses = AssetStatus.values(); // ACTIVE, IN_REPAIR, RETIRED
        for (int i = 1; i <= 10; i++) {
            Asset asset = new Asset();
            asset.setAssetTag(String.format("AST-%03d", i));
            asset.setName("Asset " + i);
            asset.setSerialNumber("SN-" + (1000 + i));
            asset.setStatus(statuses[i % statuses.length]);      // cycle through statuses
            asset.setCategory(categories.get(i % categories.size())); // cycle categories
            asset.setLocation(locations.get(i % locations.size()));   // cycle locations
            assetRepository.save(asset);
        }
    }

    // 2 users: one ADMIN, one EMPLOYEE. password is a placeholder for now — Day 16 hashes it.
    private void seedUsers() {
        userRepository.save(user("admin", "Alice Admin", "admin@itasset.com", "IT", Role.ADMIN));
        userRepository.save(user("employee", "Bob Employee", "bob@itasset.com", "Sales", Role.EMPLOYEE));
    }

    private User user(String username, String fullName, String email, String department, Role role) {
        User u = new User();
        u.setUsername(username);
        u.setFullName(fullName);
        u.setEmail(email);
        u.setDepartment(department);
        u.setRole(role);
        u.setPasswordHash("placeholder"); // Day 16 replaces this with a real BCrypt hash
        return u;
    }

    private Category category(String name, String description) {
        Category c = new Category();
        c.setName(name);
        c.setDescription(description);
        return c;
    }

    private Location location(String building, String floor, String room) {
        Location l = new Location();
        l.setBuilding(building);
        l.setFloor(floor);
        l.setRoom(room);
        return l;
    }
}
