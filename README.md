# Protokoll
## Middleware Engineering
### Document Oriented Middleware using MongoDB

| Feld | Inhalt |
|---|---|
| Aufgabe | GK8.2 Document Oriented Middleware |
| Lehrveranstaltung | Middleware Engineering / DEZSYS |
| Repository | DEZSYS_GK_WAREHOUSE_DOM |
| Technologien | Spring Boot 3.2, MongoDB, Docker, Groq AI |
| Datum | 18. März 2026 |
| Bewertung | Vertiefung (vollständig) |

---

## 1. Einführung und Aufgabenstellung

Diese Übung demonstriert die Implementierung eines dokumentenorientierten dezentralen Systems mit Spring Data MongoDB. Ziel war es, eine Middleware zu entwickeln, die Lagerdaten mehrerer Standorte zentral in einer MongoDB-Datenbank speichert und über eine vollständige REST-Schnittstelle zugänglich macht.

Die Applikation wurde von Grund auf auf einem Arch Linux System mit Neovim als Editor entwickelt. Sämtliche Probleme, die während der Entwicklung aufgetreten sind, wurden schrittweise gelöst und sind in diesem Protokoll dokumentiert.

### 1.1 Projektziele

- Installation und Konfiguration von MongoDB via Docker
- Entwurf einer geeigneten JSON-Datenstruktur für Lagerstandorte und Produkte
- Implementierung einer vollständigen REST API mit Spring Boot
- Automatische Datengenerierung: 5 Lagerstandorte, 300 Produkte, 6 Kategorien
- Mongo Shell CRUD-Operationen und Reporting-Abfragen
- KI-Integration via Groq API (Llama 3.3) zur automatischen Berichterstellung

---

## 2. Systemeinrichtung (Phase 1)

### 2.1 Voraussetzungen auf Arch Linux

Folgende Pakete wurden installiert:

```bash
sudo pacman -S jdk21-openjdk docker httpie
sudo systemctl enable --now docker
sudo usermod -aG docker $USER
```

**Problem:** Nach der Installation von JDK 21 verwendete das System weiterhin JDK 17, da die Umgebungsvariable nicht automatisch aktualisiert wurde.

**Lösung:**
```bash
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk
export PATH=$JAVA_HOME/bin:$PATH
# Permanent in ~/.zshrc eingetragen
```

### 2.2 MongoDB via Docker

MongoDB wurde als Docker-Container gestartet und auf Port 27017 gebunden:

```bash
docker pull mongo
docker run -d -p 27017:27017 --name mongo mongo
docker exec -it mongo mongosh
```

**Problem:** Der erste Verbindungsversuch mit `mongosh` schlug fehl (`ECONNREFUSED`), weil mongosh auf dem Host-System ausgeführt wurde, nicht innerhalb des Containers.

**Lösung:** mongosh immer via `docker exec -it mongo mongosh` ausführen.

Verifikation im mongosh:
```js
show dbs
// Ausgabe: admin, config, local
```

### 2.3 Repository klonen

```bash
git clone https://github.com/ThomasMicheler/DEZSYS_GK_WAREHOUSE_DOM.git
cd DEZSYS_GK_WAREHOUSE_DOM
chmod +x gradlew
```

---

## 3. Datenstruktur und Projektaufbau (Phase 2)

### 3.1 JSON-Datenstruktur

Die Entscheidung fiel auf ein eingebettetes Dokumentenmodell: Produkte werden direkt im Warehouse-Dokument als Array gespeichert. Dies ermöglicht einen einzelnen Lesezugriff ohne JOIN-Operationen.

```json
{
  "warehouseID": "WH-001",
  "warehouseName": "Wien Zentrum",
  "warehouseCity": "Wien",
  "warehousePostalCode": "1010",
  "warehouseCountry": "Austria",
  "timestamp": "2026-03-18T14:00:00",
  "productData": [
    {
      "productID": "WH-001-PRD-001",
      "productName": "Bio Orangensaft",
      "productCategory": "Getränke",
      "productQuantity": 608,
      "productUnit": "L",
      "productPrice": 1.99
    }
  ]
}
```

### 3.2 Projektstruktur

```
src/main/java/warehouse/
├── Application.java
├── model/
│   ├── WarehouseData.java     (@Document)
│   └── ProductData.java       (eingebettetes Objekt)
├── repository/
│   └── WarehouseRepository.java
├── service/
│   ├── WarehouseService.java
│   └── GeminiService.java     (Groq AI)
├── controller/
│   ├── WarehouseController.java
│   ├── ProductController.java
│   └── ReportController.java
└── generator/
    └── DataGenerator.java     (CommandLineRunner)
```

### 3.3 Wichtige Modell-Klassen

**WarehouseData.java**
```java
@Document(collection = "warehouse")
@Data @NoArgsConstructor @AllArgsConstructor
public class WarehouseData {
    @Id private String id;
    private String warehouseID, warehouseName;
    private String warehouseCity, warehousePostalCode, warehouseCountry;
    private LocalDateTime timestamp;
    private List<ProductData> productData;
}
```

**ProductData.java (kein @Id — eingebettet)**
```java
@Data @NoArgsConstructor @AllArgsConstructor
public class ProductData {
    private String productID, productName, productCategory;
    private double productQuantity, productPrice;
    private String productUnit;
}
```

### 3.4 application.properties

```properties
spring.application.name=warehouse-dom
spring.data.mongodb.host=localhost
spring.data.mongodb.port=27017
spring.data.mongodb.database=warehousedb
server.port=8080
gemini.api.key=<GROQ_API_KEY>
```

---

## 4. REST API Implementierung (Phase 3)

### 4.1 Implementierte Endpunkte

| Methode | Endpunkt | Beschreibung |
|---|---|---|
| POST | /warehouse | Neuen Lagerstandort hinzufügen |
| GET | /warehouse | Alle Lagerstandorte abrufen |
| GET | /warehouse/{id} | Lagerstandort nach ID abrufen |
| DELETE | /warehouse/{id} | Lagerstandort löschen |
| POST | /product?warehouseID= | Produkt zu Lagerstandort hinzufügen |
| GET | /product | Alle Produkte aller Standorte |
| GET | /product/{id} | Produkt nach ID + Lagerstandorte |
| DELETE | /product/{id}?warehouseID= | Produkt von Standort löschen |
| GET | /report/stock | KI-Bericht: Gesamtlagerbestand |
| GET | /report/lowstock | KI-Bericht: Kritisch niedrige Bestände |
| GET | /report/value | KI-Bericht: Lagerwert pro Standort |

### 4.2 Testdurchführung

```bash
# Warehouse erstellen
http POST localhost:8080/warehouse warehouseID=WH-001 \
  warehouseName='Wien Zentrum' warehouseCity=Wien

# Alle Warehouses abrufen
http GET localhost:8080/warehouse

# Einzelnes Warehouse
http GET localhost:8080/warehouse/WH-003

# Alle Produkte (300 Stück)
http GET localhost:8080/product | python3 -c \
  "import sys,json; d=json.load(sys.stdin); print(len(d), 'products')"
# Ausgabe: 300 products
```

---

## 5. Datengenerator (Phase 4)

### 5.1 Beschreibung

Der `DataGenerator` implementiert `CommandLineRunner` und wird automatisch beim Start der Applikation ausgeführt. Er prüft, ob bereits Daten vorhanden sind (idempotent), und generiert andernfalls 5 Lagerstandorte mit je 60 Produkten aus 6 Kategorien.

### 5.2 Generierte Datenmenge

| Parameter | Wert |
|---|---|
| Lagerstandorte | 5 (Wien, Graz, Linz, Salzburg, Innsbruck) |
| Produkte pro Standort | 60 |
| Gesamtprodukte | 300 |
| Produktkategorien | 6 (Getränke, Waschmittel, Tierfutter, Reinigung, Lebensmittel, Hygiene) |
| Produkte pro Kategorie | 10 |
| Lagerbestand | Zufällig 10–1000 Einheiten (Seed: 42) |

### 5.3 Startup-Ausgabe

```
>>> DataGenerator.run() called
>>> Current warehouse count: 0
>>> Generating warehouse data...
>>> Saved: Wien Zentrum with 60 products
>>> Saved: Graz Süd with 60 products
>>> Saved: Linz Bahnhof with 60 products
>>> Saved: Salzburg West with 60 products
>>> Saved: Innsbruck Nord with 60 products
>>> Done! Total warehouses: 5
```

### 5.4 Aufgetretene Probleme

**Problem:** Der DataGenerator wurde von Spring nicht erkannt und ausgeführt.

**Ursache:** Die Datei wurde versehentlich im falschen Repository (`DEZSYS_GK73_WAREHOUSE_MOM` statt `DEZSYS_GK_WAREHOUSE_DOM`) erstellt.

**Lösung:** Datei im korrekten Verzeichnis neu erstellt. Auf Lombok-Annotation `@RequiredArgsConstructor` verzichtet und stattdessen expliziten Konstruktor verwendet, da das `final Random`-Feld keinen Spring-Bean darstellte.

---

## 6. Mongo Shell Operationen (Phase 5)

Alle Operationen wurden in der MongoDB Shell innerhalb des Docker-Containers ausgeführt:

```bash
docker exec -it mongo mongosh warehousedb
```

### 6.1 CRUD Operationen

#### 1. Alle Lagerstandorte anzeigen (Read)

```js
db.warehouse.find({}, {warehouseID:1, warehouseName:1, warehouseCity:1, _id:0})
```

Ergebnis:
```js
[
  { warehouseID: 'WH-001', warehouseName: 'Wien Zentrum',   warehouseCity: 'Wien' },
  { warehouseID: 'WH-002', warehouseName: 'Graz Süd',       warehouseCity: 'Graz' },
  { warehouseID: 'WH-003', warehouseName: 'Linz Bahnhof',   warehouseCity: 'Linz' },
  { warehouseID: 'WH-004', warehouseName: 'Salzburg West',  warehouseCity: 'Salzburg' },
  { warehouseID: 'WH-005', warehouseName: 'Innsbruck Nord', warehouseCity: 'Innsbruck' }
]
```

#### 2. Produkt hinzufügen (Create / $push)

```js
db.warehouse.updateOne(
  { warehouseID: "WH-001" },
  { $push: { productData: {
    productID: "WH-001-PRD-999",
    productName: "Testprodukt Neu",
    productCategory: "Getränke",
    productQuantity: 100,
    productUnit: "Stk",
    productPrice: 2.99
  }}}
)
```

Ergebnis: `{ acknowledged: true, matchedCount: 1, modifiedCount: 1 }`

#### 3. Lagerbestand ändern (Update / $set)

```js
db.warehouse.updateOne(
  { warehouseID: "WH-001", "productData.productID": "WH-001-PRD-001" },
  { $set: { "productData.$.productQuantity": 9999 } }
)
```

Ergebnis: `{ acknowledged: true, matchedCount: 1, modifiedCount: 1 }`

#### 4. Produkt löschen (Delete / $pull)

```js
db.warehouse.updateOne(
  { warehouseID: "WH-001" },
  { $pull: { productData: { productID: "WH-001-PRD-999" } } }
)
```

Ergebnis: `{ acknowledged: true, matchedCount: 1, modifiedCount: 1 }`

#### 5. Lagerstandort löschen (Delete)

```js
db.warehouse.deleteOne({ warehouseID: "WH-005" })
```

Ergebnis: `{ acknowledged: true, deletedCount: 1 }`

---

### 6.2 Reporting-Abfragen (Vertiefung)

#### Fragestellung 1: Gesamtlagerbestand eines Produktes über alle Standorte

Wie hoch ist der Gesamtbestand von "Bio Orangensaft" über alle Lagerstandorte?

```js
db.warehouse.aggregate([
  { $unwind: "$productData" },
  { $match: { "productData.productName": "Bio Orangensaft" } },
  { $group: {
    _id: "$productData.productName",
    totalQuantity: { $sum: "$productData.productQuantity" }
  }}
])
```

Ergebnis: `[ { _id: 'Bio Orangensaft', totalQuantity: 11432 } ]`

#### Fragestellung 2: Produkte mit kritisch niedrigem Lagerbestand

Welche Produkte haben einen Lagerbestand von unter 50 Einheiten?

```js
db.warehouse.aggregate([
  { $unwind: "$productData" },
  { $match: { "productData.productQuantity": { $lt: 50 } } },
  { $project: {
    _id: 0,
    warehouse: "$warehouseName",
    product: "$productData.productName",
    qty: "$productData.productQuantity"
  }},
  { $sort: { qty: 1 } }
])
```

Ergebnis (Auszug):
```
Graz Süd       - Frosch Zitrone           25
Graz Süd       - Pedigree Adult Rind      26
Linz Bahnhof   - Dreamies Snacks Katze   27
Linz Bahnhof   - Calgon Wasserenthärter  28
Linz Bahnhof   - Perwoll Wolle           28
Wien Zentrum   - Calgon Wasserenthärter  45
Salzburg West  - Frischhaltefolie 30m    48
```

#### Fragestellung 3: Gesamtlagerwert pro Lagerstandort

Welcher Lagerstandort hat den höchsten Gesamtwert (Menge × Preis)?

```js
db.warehouse.aggregate([
  { $unwind: "$productData" },
  { $group: {
    _id: "$warehouseName",
    totalValue: { $sum: {
      $multiply: ["$productData.productQuantity", "$productData.productPrice"]
    }}
  }},
  { $sort: { totalValue: -1 } }
])
```

Ergebnis:
```
Wien Zentrum:   € 129.535,22
Salzburg West:  € 123.468,52
Graz Süd:       € 113.562,73
Linz Bahnhof:   € 111.141,78
```

---

## 7. KI-Integration via Groq API (Phase Vertiefung)

### 7.1 Technische Umsetzung

Die KI-Integration verwendet die Groq API mit dem Modell `llama-3.3-70b-versatile`. Die Kommunikation erfolgt über Spring WebFlux (`WebClient`). Drei Reporting-Endpunkte senden aufbereitete Lagerdaten als Prompt an die KI und erhalten deutsche Analyseberichte zurück.

| Parameter | Wert |
|---|---|
| API Provider | Groq (console.groq.com) |
| Modell | llama-3.3-70b-versatile |
| Endpoint | https://api.groq.com/openai/v1/chat/completions |
| Authentifizierung | Bearer Token (API Key) |
| Protokoll | OpenAI-kompatibles REST API |

### 7.2 Gesendete Prompts an die KI

#### Prompt 1 – Gesamtlagerbestand (GET /report/stock)

```
Du bist ein Lagerhaus-Analyst. Analysiere folgende Lagerdaten und erstelle
einen kurzen deutschen Bericht (max 150 Wörter) über den Gesamtlagerbestand
der Top 15 Produkte über alle Lagerstandorte. Hebe die Produkte mit dem
höchsten und niedrigsten Bestand hervor und gib eine kurze Empfehlung ab.

Daten (Produkt: Gesamtmenge):
Bio Apfelsaft: 4091
Trill Vogelfutter: 3982
... [15 Produkte]
```

#### Prompt 2 – Kritische Bestände (GET /report/lowstock)

```
Du bist ein Lagerhaus-Analyst. Folgende Produkte haben einen kritisch
niedrigen Lagerbestand (unter 50 Einheiten). Erstelle einen kurzen deutschen
Bericht (max 150 Wörter) mit Dringlichkeitseinschätzung und konkreten
Nachbestellempfehlungen.

Kritische Produkte (Standort - Produkt: Menge):
Graz Süd - Frosch Zitrone: 25 L
... [alle kritischen Produkte]
```

#### Prompt 3 – Lagerwert (GET /report/value)

```
Du bist ein Lagerhaus-Analyst. Analysiere den Gesamtlagerwert der folgenden
Lagerstandorte und erstelle einen kurzen deutschen Bericht (max 150 Wörter).
Vergleiche die Standorte, identifiziere den wertvollsten und günstigsten
Standort und gib eine strategische Empfehlung ab.

Lagerwert pro Standort:
Wien Zentrum: €129535.22
... [alle Standorte]
```

### 7.3 KI-Antworten

#### Antwort zu Prompt 1 (Lagerbestand)

> **Lagerstandortanalyse:** Der Gesamtlagerbestand der Top 15 Produkte über alle Lagerstandorte beträgt insgesamt 43.451 Einheiten. Das Produkt mit dem höchsten Bestand ist Bio Apfelsaft mit 4.091 Einheiten, während Perwoll Wolle mit 2.759 Einheiten den niedrigsten Bestand aufweist. Es wird empfohlen, den Bestand an Bio Apfelsaft zu überprüfen, um mögliche Überbestände zu vermeiden, und den Bestand an Perwoll Wolle aufzustocken, um Nachbestellungen zu vermeiden.

#### Antwort zu Prompt 2 (Kritische Bestände)

> **Dringender Nachbestellbericht:** Wir haben einen kritisch niedrigen Lagerbestand bei mehreren Produkten. Die Dringlichkeit ist hoch, da die meisten Produkte unter 50 Einheiten liegen. Unverzüglich nachbestellen: Frosch Zitrone (25 L), Mineralwasser Still (19 L), Dove Duschgel (22 ml). Sofort nachbestellen: Calgon Wasserenthärter (Wien Zentrum: 45 kg, Linz Bahnhof: 28 kg). Überwachung erforderlich: Pedigree Adult Rind, Royal Canin Katze, Perwoll Wolle.

#### Antwort zu Prompt 3 (Lagerwert)

> **Lagerhaus-Analyse:** Der Gesamtlagerwert beträgt etwa €575.766. Wien Zentrum weist mit €129.535,22 den höchsten Wert auf. Strategische Empfehlung: Der Fokus sollte auf Wien Zentrum liegen, da es den höchsten Lagerwert aufweist. Ein Ausbau dieses Standorts könnte zu weiteren Wachstumsmöglichkeiten führen.

### 7.4 Aufgetretene Probleme

**Problem 1:** xAI (Grok) API – 403 Forbidden. Das neu erstellte Team hatte keine Credits.
**Lösung:** Wechsel zu Groq (kostenlos, OpenAI-kompatibel).

**Problem 2:** `WebClientResponseException` nicht abgefangen — App lieferte nur `500 Internal Server Error`.
**Lösung:** try-catch Block ergänzt, Fehler wird als lesbarer Text im JSON zurückgegeben.

---

## 8. Beantwortung der Fragestellungen

**F1: Nennen Sie 4 Vorteile eines NoSQL Repository im Gegensatz zu einem relationalen DBMS**

1. Schema-Flexibilität: Dokumente können unterschiedliche Felder haben, keine Migrationen nötig.
2. Horizontale Skalierbarkeit: einfaches Sharding über mehrere Server (Scale-out statt Scale-up).
3. Hohe Performance bei großen Datenmengen durch denormalisierte, eingebettete Dokumente — ein Read statt mehrerer JOINs.
4. Bessere Abbildung moderner Datenstrukturen wie verschachtelte JSON-Objekte und Arrays.

---

**F2: Nennen Sie 4 Nachteile eines NoSQL Repository im Gegensatz zu einem relationalen DBMS**

1. Keine vollständigen ACID-Transaktionen über mehrere Dokumente/Collections.
2. Keine standardisierte Abfragesprache — jede NoSQL-DB hat ihre eigene API.
3. Datenkonsistenz schwieriger sicherzustellen (eventual consistency statt strong consistency).
4. Weniger geeignet für stark relationale Daten mit vielen Beziehungen zwischen Entitäten.

---

**F3: Welche Schwierigkeiten ergeben sich bei der Zusammenführung der Daten?**

Keine JOINs möglich — Zusammenführung muss per `$lookup` (Aggregation Pipeline) oder im Applikationscode erfolgen. Redundante Datenhaltung: dieselben Produktdaten können in mehreren Warehouse-Dokumenten vorkommen. Bei Namensänderungen eines Produkts muss der Wert in allen Dokumenten aktualisiert werden. Unterschiedliche Zeitstempel und Datenformate der Standorte müssen normalisiert werden.

---

**F4: Welche Arten von NoSQL Datenbanken gibt es?**

Es gibt vier Haupttypen: Dokumentenorientierte Datenbanken, Key-Value-Datenbanken, Wide-Column-Datenbanken und Graph-Datenbanken.

---

**F5: Nennen Sie einen Vertreter für jede Art**

| Typ | Vertreter |
|---|---|
| Dokumentenorientiert | MongoDB |
| Key-Value | Redis |
| Wide-Column | Apache Cassandra |
| Graph | Neo4j |

---

**F6: Beschreiben Sie CA, CP und AP in Bezug auf das CAP Theorem**

Das CAP Theorem besagt, dass ein verteiltes System nur 2 von 3 Eigenschaften gleichzeitig garantieren kann:

- **CA** (Consistency + Availability): Alle Nodes liefern dieselben Daten, System immer verfügbar — kein Partitionstoleranz. Nur in nicht-verteilten Systemen möglich. Beispiel: klassisches RDBMS.
- **CP** (Consistency + Partition Tolerance): Daten immer konsistent, auch bei Netzwerkausfall — aber System kann temporär nicht verfügbar sein. Beispiel: MongoDB, HBase.
- **AP** (Availability + Partition Tolerance): System bleibt immer verfügbar, auch bei Partitionierung — aber Daten können kurzzeitig inkonsistent sein (eventual consistency). Beispiel: CouchDB, Cassandra.

MongoDB ist **CP**.

---

**F7: Mit welchem Befehl können Sie den Lagerstand eines Produktes aller Lagerstandorte anzeigen?**

```js
db.warehouse.aggregate([
  { $unwind: "$productData" },
  { $match: { "productData.productName": "Bio Orangensaft" } },
  { $project: {
    _id: 0,
    warehouse: "$warehouseName",
    qty: "$productData.productQuantity"
  }}
])
```

---

**F8: Mit welchem Befehl können Sie den Lagerstand eines Produktes eines bestimmten Lagerstandortes anzeigen?**

```js
db.warehouse.aggregate([
  { $match: { warehouseID: "WH-001" } },
  { $unwind: "$productData" },
  { $match: { "productData.productName": "Bio Orangensaft" } },
  { $project: {
    _id: 0,
    warehouse: "$warehouseName",
    qty: "$productData.productQuantity"
  }}
])
```

---

## 9. Aufgetretene Probleme und Lösungen

| Problem | Ursache | Lösung |
|---|---|---|
| mongosh ECONNREFUSED | mongosh am Host gestartet, nicht im Container | `docker exec -it mongo mongosh` verwenden |
| invalid source release: 21 | JAVA_HOME zeigte auf JDK 17 trotz JDK 21 Installation | JAVA_HOME explizit in `~/.zshrc` gesetzt |
| WarehouseRepository Typ-Fehler | Repository noch auf `ProductData` statt `WarehouseData` | Datei mit `cat >` vollständig überschrieben |
| DataGenerator läuft nicht | Datei im falschen Repository erstellt | Datei im korrekten Verzeichnis neu erstellt |
| Port 8080 already in use | Alte App-Instanz noch aktiv | `pkill -f warehouse-dom` |
| MongoDB Connection refused | Docker Container gestoppt | `docker start mongo` |
| Grok API 403 Forbidden | Kein Guthaben auf xAI Account | Wechsel zu Groq (kostenlos) |
| 500 Internal Server Error /report | WebClientResponseException nicht abgefangen | try-catch Block ergänzt |

---

## 10. Zusammenfassung und Fazit

### 10.1 Erreichter Bewertungsstand

| Stufe | Status | Inhalt |
|---|---|---|
| Grundlagen | ✅ Vollständig | MongoDB, Spring Boot, REST API, Shell-Operationen, Fragestellungen |
| Erweiterte Grundlagen | ✅ Vollständig | Multi-Warehouse, alle 8 REST-Endpunkte, Datengenerator |
| Vertiefung | ✅ Vollständig | 300 Produkte, 6 Kategorien, 3 Reporting-Abfragen, KI-Integration |

### 10.2 Technische Erkenntnisse

- Eingebettete Dokumente sind ideal für 1:N-Beziehungen, wo die N-Seite immer gemeinsam mit dem Parent gelesen wird.
- `$unwind` ist notwendig, um auf Felder in eingebetteten Arrays zu filtern und zu aggregieren — ohne `$unwind` werden Arrays als Ganzes behandelt.
- `CommandLineRunner` wird nach dem Start des HTTP-Servers ausgeführt — Requests können bereits eingehen während der Generator läuft.
- Spring Data MongoDB generiert Repository-Methoden automatisch aus dem Methodennamen (z.B. `findByWarehouseID`).
- Das CAP Theorem ist kein absolutes Limit — MongoDB wählt CP, bietet aber mit `readPreference` und `writeConcern` feingranulare Kontrolle über den Konsistenz-Verfügbarkeits-Tradeoff.

### 10.3 Systemübersicht

```
┌─────────────────────────────────────────────────┐
│              Spring Boot App :8080               │
│                                                  │
│  WarehouseController  ProductController          │
│         └──────────────────┘                    │
│              WarehouseService                    │
│                    │                             │
│         WarehouseRepository (MongoDB)            │
│                                                  │
│  ReportController → GeminiService → Groq API    │
└────────────────────┬────────────────────────────┘
                     │
          ┌──────────▼──────────┐
          │  MongoDB :27017      │
          │  (Docker Container)  │
          │  warehousedb         │
          │  collection: warehouse│
          └──────────────────────┘
```

---

*Protokoll erstellt am 18. März 2026 | DEZSYS_GK_WAREHOUSE_DOM*
