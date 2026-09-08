package com.itasset.assetservice;

import com.itasset.assetservice.entity.Asset;
import com.itasset.assetservice.entity.Category;
import com.itasset.assetservice.entity.Location;
import com.itasset.assetservice.enums.AssetStatus;
import com.itasset.assetservice.repository.AssetRepository;
import com.itasset.assetservice.repository.CategoryRepository;
import com.itasset.assetservice.repository.LocationRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

// runs once on startup: fills empty tables with sample data so the app is testable
@Component
public class DataSeeder implements CommandLineRunner {

    private final CategoryRepository categoryRepository;
    private final LocationRepository locationRepository;
    private final AssetRepository assetRepository;

    public DataSeeder(CategoryRepository categoryRepository,
                      LocationRepository locationRepository,
                      AssetRepository assetRepository) {
        this.categoryRepository = categoryRepository;
        this.locationRepository = locationRepository;
        this.assetRepository = assetRepository;
    }

    @Override
    public void run(String... args) {
        // already seeded? skip inserting, just report — avoids duplicates on every restart
        if (categoryRepository.count() == 0) {
            seed();
        }
        // checkpoint: read the counts back through each repository
        System.out.printf("Seeded data -> categories: %d, locations: %d, assets: %d%n",
                categoryRepository.count(), locationRepository.count(), assetRepository.count());
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
