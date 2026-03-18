package warehouse.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductData {
    private String productID;
    private String productName;
    private String productCategory;
    private double productQuantity;
    private String productUnit;
    private double productPrice;
}
