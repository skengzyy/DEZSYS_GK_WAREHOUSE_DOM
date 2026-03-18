package warehouse.generator;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import warehouse.model.ProductData;
import warehouse.model.WarehouseData;
import warehouse.repository.WarehouseRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Component
public class DataGenerator implements CommandLineRunner {

    private final WarehouseRepository repository;
    private final Random random = new Random(42);

    public DataGenerator(WarehouseRepository repository) {
        this.repository = repository;
    }

    private static final String[][] PRODUCTS = {
        {"Getränke", "Bio Orangensaft", "L", "1.99"},
        {"Getränke", "Bio Apfelsaft", "L", "1.89"},
        {"Getränke", "Mineralwasser Still", "L", "0.59"},
        {"Getränke", "Mineralwasser Prickelnd", "L", "0.59"},
        {"Getränke", "Eistee Zitrone", "L", "1.29"},
        {"Getränke", "Eistee Pfirsich", "L", "1.29"},
        {"Getränke", "Cola Classic", "L", "1.49"},
        {"Getränke", "Cola Zero", "L", "1.49"},
        {"Getränke", "Energydrink Red", "Stk", "1.99"},
        {"Getränke", "Tomatensaft", "L", "1.39"},
        {"Waschmittel", "Ariel Color Pulver", "kg", "8.99"},
        {"Waschmittel", "Persil Universal", "kg", "9.49"},
        {"Waschmittel", "Lenor Weichspüler", "L", "4.99"},
        {"Waschmittel", "Fairy Spülmittel", "ml", "2.99"},
        {"Waschmittel", "Domestos WC Reiniger", "ml", "2.49"},
        {"Waschmittel", "Ajax Allzweckreiniger", "L", "3.49"},
        {"Waschmittel", "Calgon Wasserenthärter", "kg", "12.99"},
        {"Waschmittel", "Vanish Fleckenentferner", "kg", "7.99"},
        {"Waschmittel", "Perwoll Wolle", "L", "5.99"},
        {"Waschmittel", "Frosch Zitrone", "L", "3.29"},
        {"Tierfutter", "Whiskas Rind", "g", "0.89"},
        {"Tierfutter", "Whiskas Huhn", "g", "0.89"},
        {"Tierfutter", "Purina Felix Lachs", "g", "0.99"},
        {"Tierfutter", "Pedigree Adult Rind", "kg", "4.99"},
        {"Tierfutter", "Pedigree Welpen", "kg", "5.49"},
        {"Tierfutter", "Royal Canin Katze", "kg", "14.99"},
        {"Tierfutter", "Royal Canin Hund", "kg", "16.99"},
        {"Tierfutter", "Dreamies Snacks Katze", "g", "1.49"},
        {"Tierfutter", "Trill Vogelfutter", "kg", "3.99"},
        {"Tierfutter", "Aqua Fish Fischfutter", "g", "2.49"},
        {"Reinigung", "Swiffer Staubtücher", "Stk", "4.99"},
        {"Reinigung", "Vileda Wischmopp", "Stk", "12.99"},
        {"Reinigung", "Spontex Schwämme 5er", "Stk", "2.49"},
        {"Reinigung", "Mr Muscle Glasreiniger", "ml", "2.99"},
        {"Reinigung", "Saugstauberbeutel Ingres", "Stk", "5.99"},
        {"Reinigung", "Müllbeutel 35L 20er", "Stk", "2.99"},
        {"Reinigung", "Aluminiumfolie 30m", "Stk", "2.49"},
        {"Reinigung", "Frischhaltefolie 30m", "Stk", "1.99"},
        {"Reinigung", "Backpapier 10m", "Stk", "1.79"},
        {"Reinigung", "WC Bürste Set", "Stk", "6.99"},
        {"Lebensmittel", "Vollmilch 3.5%", "L", "1.19"},
        {"Lebensmittel", "Butter 250g", "g", "2.29"},
        {"Lebensmittel", "Eier 10er Bio", "Stk", "3.49"},
        {"Lebensmittel", "Gouda Scheiben", "g", "2.99"},
        {"Lebensmittel", "Toastbrot 500g", "g", "1.49"},
        {"Lebensmittel", "Nudeln Spaghetti 500g", "g", "0.99"},
        {"Lebensmittel", "Reis Basmati 1kg", "kg", "2.49"},
        {"Lebensmittel", "Tomaten Dose 400g", "g", "0.89"},
        {"Lebensmittel", "Olivenöl Extra Virgin", "L", "6.99"},
        {"Lebensmittel", "Mehl Type 700", "kg", "0.99"},
        {"Hygiene", "Oral-B Zahnbürste", "Stk", "4.99"},
        {"Hygiene", "Colgate Zahnpasta", "ml", "2.49"},
        {"Hygiene", "Head Shoulders Shampoo", "ml", "4.99"},
        {"Hygiene", "Nivea Body Lotion", "ml", "3.99"},
        {"Hygiene", "Gillette Rasierer", "Stk", "8.99"},
        {"Hygiene", "Always Ultra Binden", "Stk", "3.49"},
        {"Hygiene", "Pampers Windeln Gr3", "Stk", "9.99"},
        {"Hygiene", "Tempo Taschentücher", "Stk", "1.99"},
        {"Hygiene", "Dove Duschgel", "ml", "2.99"},
        {"Hygiene", "Rexona Deodorant", "ml", "2.99"},
    };

    private static final String[][] WAREHOUSES = {
      {"WH-001", "Wien Zentrum",   "Wien",      "1010", "Austria"},
      {"WH-002", "Graz Süd",       "Graz",      "8020", "Austria"},
      {"WH-003", "Linz Bahnhof",   "Linz",      "4010", "Austria"},
      {"WH-004", "Salzburg West",  "Salzburg",  "5020", "Austria"},
      {"WH-005", "Innsbruck Nord", "Innsbruck", "6020", "Austria"},
    };

    @Override
    public void run(String... args) throws Exception {
        System.out.println(">>> DataGenerator.run() called");
        long count = repository.count();
        System.out.println(">>> Current warehouse count: " + count);
        if (count > 0) {
            System.out.println(">>> Data already exists, skipping.");
            return;
        }
        System.out.println(">>> Generating warehouse data...");
        for (String[] wh : WAREHOUSES) {
            List<ProductData> products = new ArrayList<>();
            for (int i = 0; i < PRODUCTS.length; i++) {
                String[] p = PRODUCTS[i];
                products.add(new ProductData(
                    String.format("%s-PRD-%03d", wh[0], i + 1),
                    p[1], p[0],
                    10 + random.nextInt(991),
                    p[2],
                    Double.parseDouble(p[3])
                ));
            }
            repository.save(new WarehouseData(
                null, wh[0], wh[1], wh[2], wh[3], wh[4],
                LocalDateTime.now(), products
            ));
            System.out.println(">>> Saved: " + wh[1] + " with " + products.size() + " products");
        }
        System.out.println(">>> Done! Total warehouses: " + repository.count());
    }
}
