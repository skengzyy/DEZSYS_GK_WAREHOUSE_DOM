package warehouse.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import warehouse.model.ProductData;
import warehouse.model.WarehouseData;
import warehouse.repository.WarehouseRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class WarehouseService {

    private final WarehouseRepository repository;

    public List<WarehouseData> getAllWarehouses() {
        return repository.findAll();
    }

    public Optional<WarehouseData> getWarehouseById(String warehouseID) {
        return repository.findByWarehouseID(warehouseID);
    }

    public WarehouseData saveWarehouse(WarehouseData warehouse) {
        return repository.save(warehouse);
    }

    public void deleteWarehouse(String warehouseID) {
        repository.findByWarehouseID(warehouseID)
            .ifPresent(repository::delete);
    }

    public List<ProductData> getAllProducts() {
        return repository.findAll().stream()
            .flatMap(w -> w.getProductData() != null ? w.getProductData().stream() : java.util.stream.Stream.empty())
            .toList();
    }

    public List<WarehouseData> getWarehousesByProduct(String productID) {
        return repository.findAll().stream()
            .filter(w -> w.getProductData() != null &&
                w.getProductData().stream().anyMatch(p -> p.getProductID().equals(productID)))
            .toList();
    }

    public WarehouseData addProduct(String warehouseID, ProductData product) {
        WarehouseData warehouse = repository.findByWarehouseID(warehouseID)
            .orElseThrow(() -> new RuntimeException("Warehouse not found: " + warehouseID));
        if (warehouse.getProductData() == null) {
            warehouse.setProductData(new ArrayList<>());
        }
        warehouse.getProductData().add(product);
        return repository.save(warehouse);
    }

    public WarehouseData deleteProduct(String warehouseID, String productID) {
        WarehouseData warehouse = repository.findByWarehouseID(warehouseID)
            .orElseThrow(() -> new RuntimeException("Warehouse not found: " + warehouseID));
        if (warehouse.getProductData() != null) {
            warehouse.getProductData().removeIf(p -> p.getProductID().equals(productID));
        }
        return repository.save(warehouse);
    }
}
