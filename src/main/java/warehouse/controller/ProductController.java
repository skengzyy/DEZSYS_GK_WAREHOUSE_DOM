package warehouse.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import warehouse.model.ProductData;
import warehouse.model.WarehouseData;
import warehouse.service.WarehouseService;

import java.util.List;

@RestController
@RequestMapping("/product")
@RequiredArgsConstructor
public class ProductController {

    private final WarehouseService service;

    @GetMapping
    public List<ProductData> getAll() {
        return service.getAllProducts();
    }

    @GetMapping("/{id}")
    public List<WarehouseData> getWarehousesByProduct(@PathVariable String id) {
        return service.getWarehousesByProduct(id);
    }

    @PostMapping
    public ResponseEntity<WarehouseData> addProduct(
            @RequestParam String warehouseID,
            @RequestBody ProductData product) {
        return ResponseEntity.ok(service.addProduct(warehouseID, product));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<WarehouseData> deleteProduct(
            @PathVariable String id,
            @RequestParam String warehouseID) {
        return ResponseEntity.ok(service.deleteProduct(warehouseID, id));
    }
}
