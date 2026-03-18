package warehouse.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import warehouse.model.WarehouseData;
import warehouse.model.ProductData;
import warehouse.service.GeminiService;
import warehouse.service.WarehouseService;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/report")
@RequiredArgsConstructor
public class ReportController {

    private final WarehouseService warehouseService;
    private final GeminiService geminiService;

    @GetMapping
    public Map<String, String> generateAllReports() {
        List<WarehouseData> warehouses = warehouseService.getAllWarehouses();
        Map<String, String> reports = new LinkedHashMap<>();

        reports.put("report1_stock_by_product",
            geminiService.generateReport(buildPrompt1(warehouses)));
        reports.put("report2_low_stock",
            geminiService.generateReport(buildPrompt2(warehouses)));
        reports.put("report3_value_by_warehouse",
            geminiService.generateReport(buildPrompt3(warehouses)));

        return reports;
    }

    @GetMapping("/stock")
    public Map<String, String> reportTotalStock() {
        List<WarehouseData> warehouses = warehouseService.getAllWarehouses();
        return Map.of("report", geminiService.generateReport(buildPrompt1(warehouses)));
    }

    @GetMapping("/lowstock")
    public Map<String, String> reportLowStock() {
        List<WarehouseData> warehouses = warehouseService.getAllWarehouses();
        return Map.of("report", geminiService.generateReport(buildPrompt2(warehouses)));
    }

    @GetMapping("/value")
    public Map<String, String> reportWarehouseValue() {
        List<WarehouseData> warehouses = warehouseService.getAllWarehouses();
        return Map.of("report", geminiService.generateReport(buildPrompt3(warehouses)));
    }

    private String buildPrompt1(List<WarehouseData> warehouses) {
        Map<String, Double> totals = new LinkedHashMap<>();
        for (WarehouseData w : warehouses) {
            if (w.getProductData() == null) continue;
            for (ProductData p : w.getProductData()) {
                totals.merge(p.getProductName(), p.getProductQuantity(), Double::sum);
            }
        }
        String data = totals.entrySet().stream()
            .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
            .limit(15)
            .map(e -> e.getKey() + ": " + e.getValue().intValue())
            .collect(Collectors.joining("\n"));

        return """
            Du bist ein Lagerhaus-Analyst. Analysiere folgende Lagerdaten und erstelle einen
            kurzen deutschen Bericht (max 150 Wörter) über den Gesamtlagerbestand der Top 15
            Produkte über alle Lagerstandorte. Hebe die Produkte mit dem höchsten und niedrigsten
            Bestand hervor und gib eine kurze Empfehlung ab.

            Daten (Produkt: Gesamtmenge):
            """ + data;
    }

    private String buildPrompt2(List<WarehouseData> warehouses) {
        List<String> lowStock = new ArrayList<>();
        for (WarehouseData w : warehouses) {
            if (w.getProductData() == null) continue;
            for (ProductData p : w.getProductData()) {
                if (p.getProductQuantity() < 50) {
                    lowStock.add(w.getWarehouseName() + " - " + p.getProductName()
                        + ": " + (int) p.getProductQuantity() + " " + p.getProductUnit());
                }
            }
        }
        String data = String.join("\n", lowStock);

        return """
            Du bist ein Lagerhaus-Analyst. Folgende Produkte haben einen kritisch niedrigen
            Lagerbestand (unter 50 Einheiten). Erstelle einen kurzen deutschen Bericht
            (max 150 Wörter) mit einer Dringlichkeitseinschätzung und konkreten
            Nachbestellempfehlungen.

            Kritische Produkte (Standort - Produkt: Menge):
            """ + data;
    }

    private String buildPrompt3(List<WarehouseData> warehouses) {
        Map<String, Double> values = new LinkedHashMap<>();
        for (WarehouseData w : warehouses) {
            if (w.getProductData() == null) continue;
            double total = w.getProductData().stream()
                .mapToDouble(p -> p.getProductQuantity() * p.getProductPrice())
                .sum();
            values.put(w.getWarehouseName(), Math.round(total * 100.0) / 100.0);
        }
        String data = values.entrySet().stream()
            .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
            .map(e -> e.getKey() + ": €" + e.getValue())
            .collect(Collectors.joining("\n"));

        return """
            Du bist ein Lagerhaus-Analyst. Analysiere den Gesamtlagerwert der folgenden
            Lagerstandorte und erstelle einen kurzen deutschen Bericht (max 150 Wörter).
            Vergleiche die Standorte, identifiziere den wertvollsten und günstigsten Standort
            und gib eine strategische Empfehlung ab.

            Lagerwert pro Standort:
            """ + data;
    }
}
