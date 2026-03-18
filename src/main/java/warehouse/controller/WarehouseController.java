package warehouse.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import warehouse.model.WarehouseData;
import warehouse.service.WarehouseService;

import java.util.List;

@RestController
@RequestMapping("/warehouse")
@RequiredArgsConstructor
public class WarehouseController {

    private final WarehouseService service;

    @GetMapping
    public List<WarehouseData> getAll() {
        return service.getAllWarehouses();
    }

    @GetMapping("/{id}")
    public ResponseEntity<WarehouseData> getById(@PathVariable String id) {
        return service.getWarehouseById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public WarehouseData create(@RequestBody WarehouseData warehouse) {
        return service.saveWarehouse(warehouse);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        service.deleteWarehouse(id);
        return ResponseEntity.noContent().build();
    }
}
