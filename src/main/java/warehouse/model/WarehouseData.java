package warehouse.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "warehouse")
public class WarehouseData {

    @Id
    private String id;
    private String warehouseID;
    private String warehouseName;
    private String warehouseCity;
    private String warehousePostalCode;
    private String warehouseCountry;
    private LocalDateTime timestamp;
    private List<ProductData> productData;
}
