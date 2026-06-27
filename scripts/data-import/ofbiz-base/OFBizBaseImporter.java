import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.CRC32;

/**
 * One-time local/dev importer for old OFBiz base data.
 *
 * <p>Build/run example:
 * javac -cp "$MYSQL_JAR:$POSTGRES_JAR" scripts/data-import/ofbiz-base/OFBizBaseImporter.java
 * java -cp "scripts/data-import/ofbiz-base:$MYSQL_JAR:$POSTGRES_JAR" OFBizBaseImporter dry-run
 */
public class OFBizBaseImporter {

    private static final DateTimeFormatter DIR_TS = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");
    private static final DateTimeFormatter HUMAN_TS = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final long DEFAULT_TENANT_ID = Long.parseLong(envOrDefault("IMPORT_TENANT_ID", "1"));
    private static final Path OUTPUT_ROOT = Path.of(envOrDefault("IMPORT_OUTPUT_DIR", "/tmp/ofbiz-base-import"));
    private static final Set<String> EXCLUDED_CATEGORY_WORDS = Set.of(
        "series", "serise", "ebay", "amazon", "control", "remote", "motor", "valance", "cassette", "罩壳", "控制方式"
    );
    private static final List<String> BACKUP_TABLES = List.of(
        "pc_category",
        "pc_unit",
        "pc_manufacturer",
        "pc_material_type_group",
        "pc_material_type",
        "pc_base_attribute",
        "pc_material",
        "pc_material_attribute",
        "pc_media_asset",
        "pc_media_binding",
        "pc_change_log"
    );

    private final ImportReport report = new ImportReport();
    private final Map<String, String> supplierCodeByPartyId = new HashMap<>();
    private final Map<String, ManufacturerRow> manufacturerByPartyId = new HashMap<>();
    private final Map<String, MaterialTypeRow> materialTypeByCode = defaultMaterialTypeMap();

    public static void main(String[] args) throws Exception {
        if (args.length == 0 || "--help".equals(args[0]) || "-h".equals(args[0])) {
            printUsage();
            return;
        }
        OFBizBaseImporter importer = new OFBizBaseImporter();
        String mode = args[0].trim().toLowerCase(Locale.ROOT);
        switch (mode) {
            case "backup" -> importer.backupOnly();
            case "dry-run" -> importer.dryRun();
            case "apply" -> importer.apply();
            case "restore" -> importer.restore(parseBackupDir(args));
            default -> {
                System.err.println("Unknown mode: " + mode);
                printUsage();
                System.exit(2);
            }
        }
    }

    private static void printUsage() {
        System.out.println("""
            OFBiz base data importer

            Modes:
              backup                    Backup current target base-info tables.
              dry-run                   Read source and target, write report only.
              apply                     Backup target first, then upsert imported data.
              restore --backup-dir DIR   Restore target base-info tables from a backup directory.

            Required env:
              OFBIZ_DB_URL OFBIZ_DB_USER OFBIZ_DB_PASSWORD
              TARGET_DB_URL TARGET_DB_USER TARGET_DB_PASSWORD

            Optional env:
              IMPORT_OUTPUT_DIR=/tmp/ofbiz-base-import
              IMPORT_TENANT_ID=1
            """);
    }

    private static Path parseBackupDir(String[] args) {
        for (int i = 1; i < args.length - 1; i++) {
            if ("--backup-dir".equals(args[i])) {
                return Path.of(args[i + 1]);
            }
        }
        throw new IllegalArgumentException("restore requires --backup-dir DIR");
    }

    private void backupOnly() throws Exception {
        try (Connection target = openTargetConnection()) {
            checkTargetTables(target);
            Path dir = backup(target);
            System.out.println("Backup created: " + dir);
        }
    }

    private void dryRun() throws Exception {
        Path runDir = createRunDir("dry-run");
        try (Connection source = openSourceConnection(); Connection target = openTargetConnection()) {
            checkSourceTables(source);
            checkTargetTables(target);
            ImportData data = loadImportData(source);
            analyzeTarget(target, data);
            report.write(runDir);
            System.out.println(report.summaryText());
            System.out.println("Report: " + runDir);
        }
    }

    private void apply() throws Exception {
        Path runDir = createRunDir("apply");
        try (Connection source = openSourceConnection(); Connection target = openTargetConnection()) {
            checkSourceTables(source);
            checkTargetTables(target);
            Path backupDir = backup(target);
            System.out.println("Backup created before apply: " + backupDir);

            ImportData data = loadImportData(source);
            target.setAutoCommit(false);
            try {
                upsertBaseData(target, data);
                target.commit();
            } catch (Exception e) {
                target.rollback();
                throw e;
            } finally {
                target.setAutoCommit(true);
            }
            report.put("backupDir", backupDir.toString());
            report.write(runDir);
            System.out.println(report.summaryText());
            System.out.println("Report: " + runDir);
        }
    }

    private void restore(Path backupDir) throws Exception {
        Path restoreSql = backupDir.resolve("restore.sql");
        if (!Files.isRegularFile(restoreSql)) {
            throw new IllegalArgumentException("restore.sql not found in " + backupDir);
        }
        try (Connection target = openTargetConnection(); Statement statement = target.createStatement()) {
            checkTargetTables(target);
            String sql = Files.readString(restoreSql, StandardCharsets.UTF_8);
            statement.execute(sql);
            System.out.println("Restored from: " + backupDir);
        }
    }

    private Connection openSourceConnection() throws SQLException {
        return openConnection("OFBIZ_DB_URL", "OFBIZ_DB_USER", "OFBIZ_DB_PASSWORD");
    }

    private Connection openTargetConnection() throws SQLException {
        return openConnection("TARGET_DB_URL", "TARGET_DB_USER", "TARGET_DB_PASSWORD");
    }

    private static Connection openConnection(String urlKey, String userKey, String passwordKey) throws SQLException {
        String url = requiredEnv(urlKey);
        String user = requiredEnv(userKey);
        String password = requiredEnv(passwordKey);
        return DriverManager.getConnection(url, user, password);
    }

    private void checkSourceTables(Connection source) throws SQLException {
        requireTables(source, "PRODUCT", "UOM", "SUPPLIER_PRODUCT", "PARTY_GROUP", "PRODUCT_CATEGORY");
        report.put("sourceConnection", "OK");
    }

    private void checkTargetTables(Connection target) throws SQLException {
        requireTables(target, BACKUP_TABLES.toArray(String[]::new));
        report.put("targetConnection", "OK");
    }

    private static void requireTables(Connection connection, String... tableNames) throws SQLException {
        DatabaseMetaData metaData = connection.getMetaData();
        for (String tableName : tableNames) {
            if (!tableExists(metaData, tableName)) {
                throw new SQLException("Required table not found: " + tableName);
            }
        }
    }

    private static boolean tableExists(DatabaseMetaData metaData, String tableName) throws SQLException {
        for (String candidate : List.of(tableName, tableName.toLowerCase(Locale.ROOT), tableName.toUpperCase(Locale.ROOT))) {
            try (ResultSet rs = metaData.getTables(null, null, candidate, new String[]{"TABLE"})) {
                if (rs.next()) {
                    return true;
                }
            }
        }
        return false;
    }

    private Path backup(Connection target) throws SQLException, IOException {
        Path backupDir = OUTPUT_ROOT.resolve("backup-" + LocalDateTime.now().format(DIR_TS));
        Files.createDirectories(backupDir);
        StringBuilder restoreSql = new StringBuilder();
        StringBuilder manifest = new StringBuilder();
        restoreSql.append("BEGIN;\n");
        restoreSql.append("TRUNCATE TABLE ").append(String.join(", ", BACKUP_TABLES)).append(";\n");
        for (String table : BACKUP_TABLES) {
            int rowCount = appendTableBackup(target, restoreSql, table);
            manifest.append(table).append('\t').append(rowCount).append('\n');
        }
        restoreSql.append("COMMIT;\n");
        Files.writeString(backupDir.resolve("restore.sql"), restoreSql.toString(), StandardCharsets.UTF_8);
        Files.writeString(backupDir.resolve("manifest.tsv"), manifest.toString(), StandardCharsets.UTF_8);
        return backupDir;
    }

    private int appendTableBackup(Connection target, StringBuilder restoreSql, String table) throws SQLException {
        int count = 0;
        try (Statement statement = target.createStatement();
             ResultSet rs = statement.executeQuery("SELECT * FROM " + table + " ORDER BY 1")) {
            ResultSetMetaData meta = rs.getMetaData();
            int columnCount = meta.getColumnCount();
            StringBuilder columns = new StringBuilder();
            for (int i = 1; i <= columnCount; i++) {
                if (i > 1) {
                    columns.append(", ");
                }
                columns.append(meta.getColumnName(i));
            }
            while (rs.next()) {
                restoreSql.append("INSERT INTO ").append(table).append(" (").append(columns).append(") VALUES (");
                for (int i = 1; i <= columnCount; i++) {
                    if (i > 1) {
                        restoreSql.append(", ");
                    }
                    restoreSql.append(sqlLiteral(rs.getObject(i)));
                }
                restoreSql.append(");\n");
                count++;
            }
        }
        return count;
    }

    private ImportData loadImportData(Connection source) throws SQLException {
        ImportData data = new ImportData();
        data.typeGroups.addAll(defaultMaterialTypeGroups());
        data.types.addAll(defaultMaterialTypes());
        data.units.addAll(loadUnits(source));
        data.manufacturers.addAll(loadManufacturers(source));
        data.categories.addAll(loadCategories(source));
        data.materials.addAll(loadMaterials(source));
        report.count("loaded.typeGroups", data.typeGroups.size());
        report.count("loaded.types", data.types.size());
        report.count("loaded.units", data.units.size());
        report.count("loaded.manufacturers", data.manufacturers.size());
        report.count("loaded.categories", data.categories.size());
        report.count("loaded.materials", data.materials.size());
        return data;
    }

    private List<UnitRow> loadUnits(Connection source) throws SQLException {
        Map<String, UnitRow> rows = new TreeMap<>();
        try (Statement st = source.createStatement();
             ResultSet rs = st.executeQuery("""
                 SELECT DISTINCT u.uom_id, u.abbreviation, u.description, u.uom_type_id
                 FROM UOM u
                 WHERE u.uom_id IN (
                   SELECT quantity_uom_id FROM PRODUCT WHERE quantity_uom_id IS NOT NULL
                   UNION
                   SELECT quantity_uom_id FROM SUPPLIER_PRODUCT WHERE quantity_uom_id IS NOT NULL
                 )
                 ORDER BY u.uom_id
                 """)) {
            while (rs.next()) {
                String code = cleanCode(rs.getString("uom_id"));
                String name = firstNonBlank(rs.getString("abbreviation"), rs.getString("description"), code);
                rows.put(code, new UnitRow(code, normalizeUnitName(name), rs.getString("description"), inferUnitType(code, rs.getString("uom_type_id"))));
            }
        }
        rows.putIfAbsent("PCS", new UnitRow("PCS", "个", "Piece", "COUNT"));
        rows.putIfAbsent("SET", new UnitRow("SET", "套", "Set", "COUNT"));
        rows.putIfAbsent("M", new UnitRow("M", "米", "Meter", "LENGTH"));
        return new ArrayList<>(rows.values());
    }

    private List<ManufacturerRow> loadManufacturers(Connection source) throws SQLException {
        Map<String, ManufacturerRow> rows = new LinkedHashMap<>();
        rows.put("900", new ManufacturerRow("900", "通用厂家/暂无厂家", "通用", true, true, null));
        List<PartySupplierCount> suppliers = new ArrayList<>();
        try (Statement st = source.createStatement();
             ResultSet rs = st.executeQuery("""
                 SELECT sp.party_id, COALESCE(pg.group_name, sp.party_id) AS group_name, COUNT(DISTINCT sp.product_id) AS product_count
                 FROM SUPPLIER_PRODUCT sp
                 LEFT JOIN PARTY_GROUP pg ON pg.party_id = sp.party_id
                 WHERE sp.party_id IS NOT NULL
                 GROUP BY sp.party_id, pg.group_name
                 ORDER BY product_count DESC, sp.party_id
                 """)) {
            while (rs.next()) {
                String partyId = rs.getString("party_id");
                String name = firstNonBlank(rs.getString("group_name"), partyId);
                if (isBlank(name)) {
                    report.skip("manufacturer", partyId, "manufacturer name is blank");
                    continue;
                }
                suppliers.add(new PartySupplierCount(partyId, name, rs.getInt("product_count")));
            }
        }
        AtomicInteger next = new AtomicInteger(1);
        for (PartySupplierCount supplier : suppliers) {
            String code = nextManufacturerCode(next);
            ManufacturerRow row = new ManufacturerRow(code, supplier.name(), shortName(supplier.name()), true, true, supplier.partyId());
            rows.put(code, row);
            supplierCodeByPartyId.put(supplier.partyId(), code);
            manufacturerByPartyId.put(supplier.partyId(), row);
        }
        return new ArrayList<>(rows.values());
    }

    private List<CategoryRow> loadCategories(Connection source) throws SQLException {
        Map<String, CategoryRow> rows = new LinkedHashMap<>();
        try (Statement st = source.createStatement();
             ResultSet rs = st.executeQuery("""
                 SELECT product_category_id, category_name, description
                 FROM PRODUCT_CATEGORY
                 WHERE product_category_type_id = 'CATALOG_CATEGORY'
                 ORDER BY product_category_id
                 """)) {
            while (rs.next()) {
                String oldId = rs.getString("product_category_id");
                String name = firstNonBlank(rs.getString("category_name"), rs.getString("description"), oldId);
                if (!shouldImportCategory(oldId, name)) {
                    report.skip("category", oldId, "excluded by catalog category rule");
                    continue;
                }
                String code = "OLD_" + cleanCode(oldId);
                rows.put(oldId, new CategoryRow(code, translateCategoryName(name), name, null, oldId));
            }
        }
        applyCategoryParents(source, rows);
        return new ArrayList<>(rows.values());
    }

    private void applyCategoryParents(Connection source, Map<String, CategoryRow> rows) throws SQLException {
        if (rows.isEmpty()) {
            return;
        }
        try (Statement st = source.createStatement();
             ResultSet rs = st.executeQuery("""
                 SELECT parent_product_category_id, product_category_id
                 FROM PRODUCT_CATEGORY_ROLLUP
                 WHERE thru_date IS NULL OR thru_date > NOW()
                 """)) {
            while (rs.next()) {
                String parentOldId = rs.getString("parent_product_category_id");
                String childOldId = rs.getString("product_category_id");
                CategoryRow parent = rows.get(parentOldId);
                CategoryRow child = rows.get(childOldId);
                if (parent != null && child != null && !Objects.equals(parent.code(), child.code())) {
                    rows.put(childOldId, child.withParentCode(parent.code()));
                }
            }
        } catch (SQLException e) {
            report.skip("category-parent", "PRODUCT_CATEGORY_ROLLUP", "parent relation skipped: " + e.getMessage());
        }
    }

    private List<MaterialRow> loadMaterials(Connection source) throws SQLException {
        Map<String, SupplierRow> supplierByProduct = loadSupplierRows(source);
        Map<String, String> categoryTextByProduct = loadProductCategoryText(source);
        List<MaterialRow> rows = new ArrayList<>();
        try (Statement st = source.createStatement();
             ResultSet rs = st.executeQuery("""
                 SELECT product_id, internal_name, product_name, description, quantity_uom_id
                 FROM PRODUCT
                 WHERE product_type_id = 'SUBASSEMBLY'
                 ORDER BY product_id
                 """)) {
            while (rs.next()) {
                String productId = rs.getString("product_id");
                String name = firstNonBlank(rs.getString("internal_name"), rs.getString("product_name"), productId);
                if (isBlank(productId) || isBlank(name)) {
                    report.skip("material", productId, "PRODUCT id or name is blank");
                    continue;
                }
                SupplierRow supplier = supplierByProduct.get(productId);
                ManufacturerRow manufacturer = supplier == null ? null : manufacturerByPartyId.get(supplier.partyId());
                String manufacturerCode = manufacturer == null ? "900" : manufacturer.code();
                String manufacturerName = manufacturer == null ? "通用厂家/暂无厂家" : manufacturer.name();
                String rawName = name;
                String categoryText = categoryTextByProduct.getOrDefault(productId, "");
                String text = String.join(" ",
                    nullToEmpty(rs.getString("internal_name")),
                    nullToEmpty(rs.getString("product_name")),
                    nullToEmpty(rs.getString("description")),
                    supplier == null ? "" : nullToEmpty(supplier.supplierProductName()),
                    categoryText
                );
                String model = findLabeledValue(text, "型号", "款号").orElse(null);
                String spec = findSpec(text).orElse(null);
                if (isBlank(spec)) {
                    spec = name;
                    report.warn("material", productId, "spec fallback to material name");
                }
                String typeCode = inferMaterialType(text);
                MaterialTypeRow type = materialTypeByCode.getOrDefault(typeCode, materialTypeByCode.get("ACCESSORY_OTHER"));
                String groupCode = type.groupCode();
                String unitCode = cleanCode(firstNonBlank(supplier == null ? null : supplier.quantityUomId(), rs.getString("quantity_uom_id"), "PCS"));
                String color = findColor(rawName).or(() -> findColor(text)).orElse(null);
                BigDecimal price = supplier == null ? null : supplier.lastPrice();
                rows.add(new MaterialRow(
                    productId,
                    buildMaterialNameCn(rawName, typeCode, color, spec),
                    rawName,
                    typeCode,
                    type.nameCn(),
                    groupCode,
                    type.groupNameCn(),
                    unitCode,
                    null,
                    manufacturerCode,
                    manufacturerName,
                    supplier == null ? null : supplier.supplierProductId(),
                    model,
                    spec,
                    buildSpecModelText(model, spec),
                    color,
                    null,
                    price,
                    "source=ofbiz; productId=" + productId
                ));
            }
        }
        return rows;
    }

    private Map<String, String> loadProductCategoryText(Connection source) throws SQLException {
        Map<String, List<String>> values = new HashMap<>();
        try (Statement st = source.createStatement();
             ResultSet rs = st.executeQuery("""
                 SELECT pcm.product_id, pc.product_category_type_id, pc.category_name, pc.description
                 FROM PRODUCT_CATEGORY_MEMBER pcm
                 JOIN PRODUCT_CATEGORY pc ON pc.product_category_id = pcm.product_category_id
                 WHERE pcm.product_id IS NOT NULL
                 """)) {
            while (rs.next()) {
                String productId = rs.getString("product_id");
                values.computeIfAbsent(productId, ignored -> new ArrayList<>())
                    .add(firstNonBlank(rs.getString("product_category_type_id"), "") + ":" + firstNonBlank(rs.getString("category_name"), rs.getString("description"), ""));
            }
        } catch (SQLException e) {
            report.warn("material-category", "PRODUCT_CATEGORY_MEMBER", "category relation skipped: " + e.getMessage());
        }
        Map<String, String> result = new HashMap<>();
        values.forEach((productId, names) -> result.put(productId, String.join(" ", names)));
        return result;
    }

    private Map<String, SupplierRow> loadSupplierRows(Connection source) throws SQLException {
        Map<String, SupplierRow> rows = new HashMap<>();
        try (Statement st = source.createStatement();
             ResultSet rs = st.executeQuery("""
                 SELECT product_id, party_id, supplier_product_id, supplier_product_name, last_price, quantity_uom_id, available_from_date
                 FROM SUPPLIER_PRODUCT
                 WHERE product_id IS NOT NULL
                 ORDER BY product_id, available_from_date DESC
                 """)) {
            while (rs.next()) {
                String productId = rs.getString("product_id");
                if (rows.containsKey(productId)) {
                    continue;
                }
                rows.put(productId, new SupplierRow(
                    productId,
                    rs.getString("party_id"),
                    rs.getString("supplier_product_id"),
                    rs.getString("supplier_product_name"),
                    rs.getBigDecimal("last_price"),
                    rs.getString("quantity_uom_id")
                ));
            }
        }
        return rows;
    }

    private void analyzeTarget(Connection target, ImportData data) throws SQLException {
        report.count("target.existing.categories", count(target, "pc_category"));
        report.count("target.existing.units", count(target, "pc_unit"));
        report.count("target.existing.manufacturers", count(target, "pc_manufacturer"));
        report.count("target.existing.materials", count(target, "pc_material"));
        report.count("plan.upsert.categories", data.categories.size());
        report.count("plan.upsert.units", data.units.size());
        report.count("plan.upsert.manufacturers", data.manufacturers.size());
        report.count("plan.upsert.materials", data.materials.size());
    }

    private void upsertBaseData(Connection target, ImportData data) throws SQLException {
        Map<String, Long> groupIds = new HashMap<>();
        Map<String, Long> typeIds = new HashMap<>();
        Map<String, Long> manufacturerIds = new HashMap<>();
        Map<String, Long> categoryIds = new HashMap<>();

        for (MaterialTypeGroupRow row : data.typeGroups) {
            long id = upsertMaterialTypeGroup(target, row);
            groupIds.put(row.code(), id);
        }
        for (MaterialTypeRow row : data.types) {
            long groupId = groupIds.getOrDefault(row.groupCode(), importId("MTG", row.groupCode()));
            long id = upsertMaterialType(target, row, groupId);
            typeIds.put(row.code(), id);
        }
        for (UnitRow row : data.units) {
            upsertUnit(target, row);
        }
        for (ManufacturerRow row : data.manufacturers) {
            long id = upsertManufacturer(target, row);
            manufacturerIds.put(row.code(), id);
        }
        for (CategoryRow row : data.categories) {
            long parentId = row.parentCode() == null ? 0L : categoryIds.getOrDefault(row.parentCode(), 0L);
            long id = upsertCategory(target, row, parentId);
            categoryIds.put(row.code(), id);
        }
        for (MaterialRow row : data.materials) {
            long typeId = typeIds.getOrDefault(row.materialTypeCode(), importId("MT", row.materialTypeCode()));
            long groupId = groupIds.getOrDefault(row.attributeGroupCode(), importId("MTG", row.attributeGroupCode()));
            long manufacturerId = manufacturerIds.getOrDefault(row.manufacturerCode(), importId("MFG", row.manufacturerCode()));
            upsertMaterial(target, row, typeId, groupId, manufacturerId);
        }
    }

    private long upsertMaterialTypeGroup(Connection c, MaterialTypeGroupRow row) throws SQLException {
        long id = existingId(c, "pc_material_type_group", "group_id", "group_code", row.code()).orElse(importId("MTG", row.code()));
        boolean exists = exists(c, "pc_material_type_group", "group_id", id);
        if (exists) {
            try (PreparedStatement ps = c.prepareStatement("""
                UPDATE pc_material_type_group
                SET group_name_cn=?, group_name_en=?, system_flag='Y', editable_flag='N', status='ENABLED',
                    sort_order=?, remark=?, update_by='ofbiz-import', update_time=?
                WHERE group_id=? AND tenant_id=?
                """)) {
                bind(ps, row.nameCn(), row.nameEn(), row.sortOrder(), "source=ofbiz-import", now(), id, DEFAULT_TENANT_ID);
                report.count("updated.typeGroups", ps.executeUpdate());
            }
        } else {
            try (PreparedStatement ps = c.prepareStatement("""
                INSERT INTO pc_material_type_group
                (group_id, tenant_id, group_code, group_name_cn, group_name_en, system_flag, editable_flag, status, sort_order, remark,
                 del_flag, create_by_id, create_by, create_time, update_by, update_time)
                VALUES (?, ?, ?, ?, ?, 'Y', 'N', 'ENABLED', ?, ?, '0', 1, 'ofbiz-import', ?, 'ofbiz-import', ?)
                """)) {
                Timestamp ts = now();
                bind(ps, id, DEFAULT_TENANT_ID, row.code(), row.nameCn(), row.nameEn(), row.sortOrder(), "source=ofbiz-import", ts, ts);
                report.count("inserted.typeGroups", ps.executeUpdate());
            }
        }
        return id;
    }

    private long upsertMaterialType(Connection c, MaterialTypeRow row, long groupId) throws SQLException {
        long id = existingId(c, "pc_material_type", "material_type_id", "material_type_code", row.code()).orElse(importId("MT", row.code()));
        boolean exists = exists(c, "pc_material_type", "material_type_id", id);
        if (exists) {
            try (PreparedStatement ps = c.prepareStatement("""
                UPDATE pc_material_type
                SET material_type_name_cn=?, material_type_name_en=?, attribute_group_id=?, attribute_group_code=?,
                    attribute_group_name_cn=?, system_flag='Y', editable_flag='N', status='ENABLED', sort_order=?,
                    remark=?, update_by='ofbiz-import', update_time=?
                WHERE material_type_id=? AND tenant_id=?
                """)) {
                bind(ps, row.nameCn(), row.nameEn(), groupId, row.groupCode(), row.groupNameCn(), row.sortOrder(), "source=ofbiz-import", now(), id, DEFAULT_TENANT_ID);
                report.count("updated.types", ps.executeUpdate());
            }
        } else {
            try (PreparedStatement ps = c.prepareStatement("""
                INSERT INTO pc_material_type
                (material_type_id, tenant_id, material_type_code, material_type_name_cn, material_type_name_en,
                 attribute_group_id, attribute_group_code, attribute_group_name_cn, system_flag, editable_flag, status,
                 sort_order, remark, del_flag, create_by_id, create_by, create_time, update_by, update_time)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, 'Y', 'N', 'ENABLED', ?, ?, '0', 1, 'ofbiz-import', ?, 'ofbiz-import', ?)
                """)) {
                Timestamp ts = now();
                bind(ps, id, DEFAULT_TENANT_ID, row.code(), row.nameCn(), row.nameEn(), groupId, row.groupCode(), row.groupNameCn(), row.sortOrder(), "source=ofbiz-import", ts, ts);
                report.count("inserted.types", ps.executeUpdate());
            }
        }
        return id;
    }

    private long upsertUnit(Connection c, UnitRow row) throws SQLException {
        long id = existingId(c, "pc_unit", "unit_id", "unit_code", row.code()).orElse(importId("UNIT", row.code()));
        boolean exists = exists(c, "pc_unit", "unit_id", id);
        if (exists) {
            try (PreparedStatement ps = c.prepareStatement("""
                UPDATE pc_unit
                SET unit_name_cn=?, unit_name_en=?, unit_type=?, status='ENABLED', remark=?, update_by='ofbiz-import', update_time=?
                WHERE unit_id=? AND tenant_id=?
                """)) {
                bind(ps, row.nameCn(), row.nameEn(), row.type(), "source=ofbiz-import", now(), id, DEFAULT_TENANT_ID);
                report.count("updated.units", ps.executeUpdate());
            }
        } else {
            try (PreparedStatement ps = c.prepareStatement("""
                INSERT INTO pc_unit
                (unit_id, tenant_id, unit_code, unit_name_cn, unit_name_en, unit_type, precision_scale, rounding_mode,
                 status, del_flag, sort_order, remark, create_by_id, create_by, create_time, update_by, update_time)
                VALUES (?, ?, ?, ?, ?, ?, 4, 'HALF_UP', 'ENABLED', '0', 0, ?, 1, 'ofbiz-import', ?, 'ofbiz-import', ?)
                """)) {
                Timestamp ts = now();
                bind(ps, id, DEFAULT_TENANT_ID, row.code(), row.nameCn(), row.nameEn(), row.type(), "source=ofbiz-import", ts, ts);
                report.count("inserted.units", ps.executeUpdate());
            }
        }
        return id;
    }

    private long upsertManufacturer(Connection c, ManufacturerRow row) throws SQLException {
        long id = existingId(c, "pc_manufacturer", "manufacturer_id", "manufacturer_code", row.code()).orElse(importId("MFG", row.code()));
        boolean exists = exists(c, "pc_manufacturer", "manufacturer_id", id);
        if (exists) {
            try (PreparedStatement ps = c.prepareStatement("""
                UPDATE pc_manufacturer
                SET manufacturer_name=?, manufacturer_short_name=?, manufacturer_flag=?, supplier_flag=?, status='ENABLED',
                    remark=?, update_by='ofbiz-import', update_time=?
                WHERE manufacturer_id=? AND tenant_id=?
                """)) {
                bind(ps, row.name(), row.shortName(), row.manufacturerFlag(), row.supplierFlag(), buildSourceRemark(row.sourcePartyId()), now(), id, DEFAULT_TENANT_ID);
                report.count("updated.manufacturers", ps.executeUpdate());
            }
        } else {
            try (PreparedStatement ps = c.prepareStatement("""
                INSERT INTO pc_manufacturer
                (manufacturer_id, tenant_id, manufacturer_code, manufacturer_name, manufacturer_short_name, manufacturer_flag, supplier_flag,
                 status, del_flag, sort_order, remark, create_by_id, create_by, create_time, update_by, update_time)
                VALUES (?, ?, ?, ?, ?, ?, ?, 'ENABLED', '0', 0, ?, 1, 'ofbiz-import', ?, 'ofbiz-import', ?)
                """)) {
                Timestamp ts = now();
                bind(ps, id, DEFAULT_TENANT_ID, row.code(), row.name(), row.shortName(), row.manufacturerFlag(), row.supplierFlag(), buildSourceRemark(row.sourcePartyId()), ts, ts);
                report.count("inserted.manufacturers", ps.executeUpdate());
            }
        }
        return id;
    }

    private long upsertCategory(Connection c, CategoryRow row, long parentId) throws SQLException {
        long id = existingId(c, "pc_category", "category_id", "category_code", row.code()).orElse(importId("CAT", row.code()));
        boolean exists = exists(c, "pc_category", "category_id", id);
        String path = parentId == 0 ? row.code() : row.parentCode() + "/" + row.code();
        int level = parentId == 0 ? 1 : 2;
        if (exists) {
            try (PreparedStatement ps = c.prepareStatement("""
                UPDATE pc_category
                SET parent_id=?, category_name_cn=?, category_name_en=?, business_type='PRODUCT', category_level=?, category_path=?,
                    status='ENABLED', remark=?, update_by='ofbiz-import', update_time=?
                WHERE category_id=? AND tenant_id=?
                """)) {
                bind(ps, parentId, row.nameCn(), row.nameEn(), level, path, buildSourceRemark(row.sourceId()), now(), id, DEFAULT_TENANT_ID);
                report.count("updated.categories", ps.executeUpdate());
            }
        } else {
            try (PreparedStatement ps = c.prepareStatement("""
                INSERT INTO pc_category
                (category_id, tenant_id, parent_id, category_code, category_name_cn, category_name_en, business_type,
                 category_level, category_path, status, del_flag, sort_order, remark, create_by_id, create_by, create_time, update_by, update_time)
                VALUES (?, ?, ?, ?, ?, ?, 'PRODUCT', ?, ?, 'ENABLED', '0', 0, ?, 1, 'ofbiz-import', ?, 'ofbiz-import', ?)
                """)) {
                Timestamp ts = now();
                bind(ps, id, DEFAULT_TENANT_ID, parentId, row.code(), row.nameCn(), row.nameEn(), level, path, buildSourceRemark(row.sourceId()), ts, ts);
                report.count("inserted.categories", ps.executeUpdate());
            }
        }
        return id;
    }

    private long upsertMaterial(Connection c, MaterialRow row, long typeId, long groupId, long manufacturerId) throws SQLException {
        long id = existingId(c, "pc_material", "material_id", "material_code", row.code()).orElse(importId("MAT", row.code()));
        boolean exists = exists(c, "pc_material", "material_id", id);
        String specModelText = buildSpecModelText(row.model(), row.spec());
        if (exists) {
            try (PreparedStatement ps = c.prepareStatement("""
                UPDATE pc_material
                SET material_name_cn=?, material_name_en=?, material_type_id=?, material_type_code=?, material_type_name_cn=?,
                    attribute_group_id=?, attribute_group_code=?, attribute_group_name_cn=?, unit_code=?, secondary_unit_code=?,
                    manufacturer_id=?, manufacturer_code=?, manufacturer_name=?, manufacturer_item_no=?, model=?, spec=?,
                    spec_model_text=?, color_name=?, weight_value=?, unit_price=?, audit_by='ofbiz-import', audit_time=?,
                    status='ENABLED', remark=?, update_by='ofbiz-import', update_time=?
                WHERE material_id=? AND tenant_id=?
                """)) {
                bind(ps,
                    row.nameCn(), row.nameEn(), typeId, row.materialTypeCode(), row.materialTypeNameCn(),
                    groupId, row.attributeGroupCode(), row.attributeGroupNameCn(), row.unitCode(), row.secondaryUnitCode(),
                    manufacturerId, row.manufacturerCode(), row.manufacturerName(), row.manufacturerItemNo(), row.model(), row.spec(),
                    specModelText, row.colorName(), row.weightValue(), row.unitPrice(), now(), row.remark(), now(), id, DEFAULT_TENANT_ID);
                report.count("updated.materials", ps.executeUpdate());
            }
        } else {
            try (PreparedStatement ps = c.prepareStatement("""
                INSERT INTO pc_material
                (material_id, tenant_id, material_code, material_name_cn, material_name_en,
                 material_type_id, material_type_code, material_type_name_cn, attribute_group_id, attribute_group_code, attribute_group_name_cn,
                 unit_code, secondary_unit_code, manufacturer_id, manufacturer_code, manufacturer_name, manufacturer_item_no,
                 model, spec, spec_model_text, color_name, weight_value, unit_price, audit_by, audit_time,
                 sort_order, status, del_flag, remark, create_by_id, create_by, create_time, update_by, update_time)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'ofbiz-import', ?,
                        0, 'ENABLED', '0', ?, 1, 'ofbiz-import', ?, 'ofbiz-import', ?)
                """)) {
                Timestamp ts = now();
                bind(ps,
                    id, DEFAULT_TENANT_ID, row.code(), row.nameCn(), row.nameEn(),
                    typeId, row.materialTypeCode(), row.materialTypeNameCn(), groupId, row.attributeGroupCode(), row.attributeGroupNameCn(),
                    row.unitCode(), row.secondaryUnitCode(), manufacturerId, row.manufacturerCode(), row.manufacturerName(), row.manufacturerItemNo(),
                    row.model(), row.spec(), specModelText, row.colorName(), row.weightValue(), row.unitPrice(), ts, row.remark(), ts, ts);
                report.count("inserted.materials", ps.executeUpdate());
            }
        }
        insertImportChangeLogIfMissing(c, row, id);
        return id;
    }

    private void insertImportChangeLogIfMissing(Connection c, MaterialRow row, long materialId) throws SQLException {
        try (PreparedStatement exists = c.prepareStatement("""
            SELECT 1 FROM pc_change_log
            WHERE tenant_id=? AND biz_type='MATERIAL' AND biz_code=? AND action_type='IMPORT'
            LIMIT 1
            """)) {
            bind(exists, DEFAULT_TENANT_ID, row.code());
            try (ResultSet rs = exists.executeQuery()) {
                if (rs.next()) {
                    return;
                }
            }
        }
        try (PreparedStatement ps = c.prepareStatement("""
            INSERT INTO pc_change_log
            (change_log_id, tenant_id, biz_module, biz_type, biz_id, biz_code, action_type, action_name,
             before_json, after_json, diff_json, operator_id, operator_name, operate_time, remark,
             create_by_id, create_by, create_time, update_by, update_time)
            VALUES (?, ?, 'BASE_INFO', 'MATERIAL', ?, ?, 'IMPORT', '老OFBiz导入',
                    NULL, ?, NULL, 1, 'ofbiz-import', ?, ?, 1, 'ofbiz-import', ?, 'ofbiz-import', ?)
            """)) {
            Timestamp ts = now();
            bind(ps, importId("LOG", row.code()), DEFAULT_TENANT_ID, materialId, row.code(), toJson(row), ts, row.remark(), ts, ts);
            report.count("inserted.changeLogs", ps.executeUpdate());
        }
    }

    private Optional<Long> existingId(Connection c, String table, String idColumn, String codeColumn, String code) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("SELECT " + idColumn + " FROM " + table + " WHERE tenant_id=? AND " + codeColumn + "=? AND del_flag='0' LIMIT 1")) {
            bind(ps, DEFAULT_TENANT_ID, code);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(rs.getLong(1));
                }
            }
        }
        return Optional.empty();
    }

    private boolean exists(Connection c, String table, String idColumn, long id) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("SELECT 1 FROM " + table + " WHERE tenant_id=? AND " + idColumn + "=? LIMIT 1")) {
            bind(ps, DEFAULT_TENANT_ID, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private long count(Connection c, String table) throws SQLException {
        try (Statement st = c.createStatement(); ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM " + table)) {
            rs.next();
            return rs.getLong(1);
        }
    }

    private static List<MaterialTypeGroupRow> defaultMaterialTypeGroups() {
        return List.of(
            new MaterialTypeGroupRow("FABRIC", "面料", "Fabric", 10),
            new MaterialTypeGroupRow("ALUMINUM", "铝材", "Aluminum", 20),
            new MaterialTypeGroupRow("SYSTEM", "系统", "System", 30),
            new MaterialTypeGroupRow("ACCESSORY", "配件", "Accessory", 40),
            new MaterialTypeGroupRow("INSTALL_KIT", "安装包", "Install Kit", 50),
            new MaterialTypeGroupRow("PACKAGING", "包装", "Packaging", 60),
            new MaterialTypeGroupRow("PRINTED", "印刷品", "Printed Material", 70)
        );
    }

    private static List<MaterialTypeRow> defaultMaterialTypes() {
        return List.of(
            type("ROLLER_FABRIC", "卷帘面料", "Roller Fabric", "FABRIC", "面料", 10),
            type("BLACKOUT_FABRIC", "遮光面料", "Blackout Fabric", "FABRIC", "面料", 20),
            type("SOLAR_FABRIC", "阳光面料", "Solar Fabric", "FABRIC", "面料", 30),
            type("ZEBRA_FABRIC", "斑马帘面料", "Zebra Fabric", "FABRIC", "面料", 40),
            type("ROMAN_FABRIC", "罗马帘面料", "Roman Fabric", "FABRIC", "面料", 50),
            type("OUTDOOR_FABRIC", "户外帘面料", "Outdoor Fabric", "FABRIC", "面料", 60),
            type("HONEYCOMB_FABRIC", "蜂巢帘面料", "Honeycomb Fabric", "FABRIC", "面料", 70),
            type("WOVEN_WOOD_FABRIC", "木百叶/竹帘材料", "Woven Wood Material", "FABRIC", "面料", 80),
            type("FABRIC_OTHER", "其他面料", "Other Fabric", "FABRIC", "面料", 90),

            type("COVER_SHELL", "罩壳", "Cover Shell", "ALUMINUM", "铝材", 110),
            type("ALUMINUM_TUBE", "铝管", "Aluminum Tube", "ALUMINUM", "铝材", 120),
            type("TOP_RAIL", "上梁/上杆", "Top Rail", "ALUMINUM", "铝材", 130),
            type("BOTTOM_RAIL", "下梁/下杆", "Bottom Rail", "ALUMINUM", "铝材", 140),
            type("TRACK", "轨道", "Track", "ALUMINUM", "铝材", 150),
            type("ALUMINUM_PROFILE", "型材", "Aluminum Profile", "ALUMINUM", "铝材", 160),

            type("MOTOR", "电机", "Motor", "SYSTEM", "系统", 210),
            type("REMOTE_CONTROLLER", "遥控器", "Remote Controller", "SYSTEM", "系统", 220),
            type("CONTROLLER", "控制器", "Controller", "SYSTEM", "系统", 230),
            type("CHAIN", "拉珠/珠链", "Chain", "SYSTEM", "系统", 240),
            type("SPRING", "弹簧/弹片", "Spring", "SYSTEM", "系统", 250),
            type("CORD_LOOP", "绳环/拉绳", "Cord Loop", "SYSTEM", "系统", 260),

            type("END_CAP", "尾塞/端盖", "End Cap", "ACCESSORY", "配件", 310),
            type("LIMITER", "限位器", "Limiter", "ACCESSORY", "配件", 320),
            type("GLUE_STRIP", "胶条", "Glue Strip", "ACCESSORY", "配件", 330),
            type("HANDLE", "手柄", "Handle", "ACCESSORY", "配件", 340),
            type("BRACKET", "支架", "Bracket", "ACCESSORY", "配件", 350),
            type("SCREW", "螺丝", "Screw", "ACCESSORY", "配件", 360),
            type("SOLAR_PANEL", "太阳能板", "Solar Panel", "ACCESSORY", "配件", 370),
            type("ACCESSORY_OTHER", "其他配件", "Other Accessory", "ACCESSORY", "配件", 390),

            type("INSTALL_KIT", "安装包", "Install Kit", "INSTALL_KIT", "安装包", 410),
            type("EXPANSION_BOLT", "膨胀件", "Expansion Bolt", "INSTALL_KIT", "安装包", 420),
            type("INSTALL_CODE", "安装码", "Install Code", "INSTALL_KIT", "安装包", 430),

            type("CARTON", "纸箱", "Carton", "PACKAGING", "包装", 510),
            type("PET_BOX", "PET盒", "PET Box", "PACKAGING", "包装", 520),
            type("HONEYCOMB_BOARD", "蜂窝板", "Honeycomb Board", "PACKAGING", "包装", 530),
            type("FROSTED_BAG", "磨砂袋", "Frosted Bag", "PACKAGING", "包装", 540),
            type("PEARL_COTTON", "珍珠棉", "Pearl Cotton", "PACKAGING", "包装", 550),
            type("PACKAGING_BAG", "包装袋", "Packaging Bag", "PACKAGING", "包装", 560),

            type("LABEL", "标签", "Label", "PRINTED", "印刷品", 610),
            type("COLOR_CARD", "彩卡", "Color Card", "PRINTED", "印刷品", 620),
            type("BROCHURE", "宣传册", "Brochure", "PRINTED", "印刷品", 630),
            type("MANUAL", "说明书", "Manual", "PRINTED", "印刷品", 640)
        );
    }

    private static Map<String, MaterialTypeRow> defaultMaterialTypeMap() {
        Map<String, MaterialTypeRow> map = new HashMap<>();
        for (MaterialTypeRow row : defaultMaterialTypes()) {
            map.put(row.code(), row);
        }
        return map;
    }

    private static MaterialTypeRow type(String code, String nameCn, String nameEn, String groupCode, String groupNameCn, int sortOrder) {
        return new MaterialTypeRow(code, nameCn, nameEn, groupCode, groupNameCn, sortOrder);
    }

    private static String inferMaterialType(String text) {
        String value = nullToEmpty(text).toLowerCase(Locale.ROOT);
        if (containsAny(value, "说明书", "manual")) {
            return "MANUAL";
        }
        if (containsAny(value, "标签", "标贴", "label")) {
            return "LABEL";
        }
        if (containsAny(value, "彩卡", "color card", "brochure", "宣传册")) {
            return "COLOR_CARD";
        }
        if (containsAny(value, "纸箱", "carton")) {
            return "CARTON";
        }
        if (containsAny(value, "pet盒", "pet box", "pet")) {
            return "PET_BOX";
        }
        if (containsAny(value, "蜂窝板", "honeycomb board")) {
            return "HONEYCOMB_BOARD";
        }
        if (containsAny(value, "磨砂袋", "frosted bag")) {
            return "FROSTED_BAG";
        }
        if (containsAny(value, "珍珠棉", "pearl cotton")) {
            return "PEARL_COTTON";
        }
        if (containsAny(value, "包装袋", "bag", "包装")) {
            return "PACKAGING_BAG";
        }
        if (containsAny(value, "太阳能板", "solar panel")) {
            return "SOLAR_PANEL";
        }
        if (containsAny(value, "尾塞", "端盖", "end cap", "cap")) {
            return "END_CAP";
        }
        if (containsAny(value, "限位", "limiter")) {
            return "LIMITER";
        }
        if (containsAny(value, "螺丝", "螺钉", "螺母", "screw")) {
            return "SCREW";
        }
        if (containsAny(value, "支架", "bracket")) {
            return "BRACKET";
        }
        if (containsAny(value, "胶条", "glue", "adhesive")) {
            return "GLUE_STRIP";
        }
        if (containsAny(value, "手柄", "handle")) {
            return "HANDLE";
        }
        if (containsAny(value, "安装包", "零件包", "install kit", "kit")) {
            return "INSTALL_KIT";
        }
        if (containsAny(value, "膨胀", "expansion")) {
            return "EXPANSION_BOLT";
        }
        if (containsAny(value, "安装码")) {
            return "INSTALL_CODE";
        }
        if (containsAny(value, "电机", "motor")) {
            return "MOTOR";
        }
        if (containsAny(value, "遥控", "remote")) {
            return "REMOTE_CONTROLLER";
        }
        if (containsAny(value, "控制器", "controller")) {
            return "CONTROLLER";
        }
        if (containsAny(value, "拉珠", "珠链", "chain")) {
            return "CHAIN";
        }
        if (containsAny(value, "弹簧", "弹片", "spring")) {
            return "SPRING";
        }
        if (containsAny(value, "绳", "cord loop", "cord")) {
            return "CORD_LOOP";
        }
        if (containsAny(value, "罩壳", "cassette")) {
            return "COVER_SHELL";
        }
        if (containsAny(value, "铝管", "圆管", "tube")) {
            return "ALUMINUM_TUBE";
        }
        if (containsAny(value, "上梁", "上杆", "top rail")) {
            return "TOP_RAIL";
        }
        if (containsAny(value, "下梁", "下杆", "bottom rail")) {
            return "BOTTOM_RAIL";
        }
        if (containsAny(value, "轨道", "track")) {
            return "TRACK";
        }
        if (containsAny(value, "铝", "profile", "rail")) {
            return "ALUMINUM_PROFILE";
        }
        if (containsAny(value, "outdoor")) {
            return "OUTDOOR_FABRIC";
        }
        if (containsAny(value, "zebra")) {
            return "ZEBRA_FABRIC";
        }
        if (containsAny(value, "roman")) {
            return "ROMAN_FABRIC";
        }
        if (containsAny(value, "cellular", "honeycomb")) {
            return "HONEYCOMB_FABRIC";
        }
        if (containsAny(value, "woven", "wood", "basswood", "bamboo", "wps")) {
            return "WOVEN_WOOD_FABRIC";
        }
        if (containsAny(value, "blackout")) {
            return "BLACKOUT_FABRIC";
        }
        if (containsAny(value, "solar", "screen view")) {
            return "SOLAR_FABRIC";
        }
        if (containsAny(value, "fabric", "cloth", "shade", "serenity", "galaxy", "urban", "polaris", "mars", "venus", "tulip")) {
            return "ROLLER_FABRIC";
        }
        if (looksLikeFabricColorCode(value)) {
            return "ROLLER_FABRIC";
        }
        return "ACCESSORY_OTHER";
    }

    private static boolean looksLikeFabricColorCode(String value) {
        String text = nullToEmpty(value).trim().toLowerCase(Locale.ROOT);
        if (!Pattern.compile("\\b(ys|yss|yb|xs|xb|xss|sc|dtl|dbl|dbg)\\d*[a-z0-9-]*\\b").matcher(text).find()) {
            return false;
        }
        return containsAny(text, "white", "black", "grey", "gray", "beige", "khaki", "brown", "coffee", "chocolate", "blue", "cream", "ivory", "sand", "linen", "latte", "stripe");
    }

    private static Optional<String> findSpec(String text) {
        Optional<String> labeled = findLabeledValue(text, "规格", "尺寸", "门幅");
        if (labeled.isPresent()) {
            return labeled;
        }
        List<Pattern> patterns = List.of(
            Pattern.compile("(\\d+(?:\\.\\d+)?\\s*[xX*×]\\s*\\d+(?:\\.\\d+)?(?:\\s*[xX*×]\\s*\\d+(?:\\.\\d+)?)?\\s*(?:mm|cm|m|米|寸|英寸)?)"),
            Pattern.compile("(\\d+(?:\\.\\d+)?\\s*(?:mm|cm|m|米|寸|英寸))")
        );
        for (Pattern pattern : patterns) {
            Matcher matcher = pattern.matcher(nullToEmpty(text));
            if (matcher.find()) {
                return Optional.of(matcher.group(1).trim());
            }
        }
        return Optional.empty();
    }

    private static String translateCategoryName(String name) {
        String key = nullToEmpty(name).trim().toLowerCase(Locale.ROOT);
        return switch (key) {
            case "roller shades" -> "卷帘";
            case "blackout shades" -> "遮光卷帘";
            case "solar shades" -> "阳光卷帘";
            case "screen view shades" -> "透景卷帘";
            case "zebra shades" -> "斑马帘";
            case "cellular shades" -> "蜂巢帘";
            case "tdbu cellular shades" -> "上下开合蜂巢帘";
            case "day and night cellular shades" -> "日夜蜂巢帘";
            case "dream curtain" -> "梦幻帘";
            case "pull rod system" -> "拉杆系统";
            case "roman shades" -> "罗马帘";
            case "shangri-la shades" -> "香格里拉帘";
            case "outdoor roller shades" -> "户外卷帘";
            case "woven wood", "woven woods" -> "木织帘";
            case "wooden blinds" -> "木百叶";
            case "basswood" -> "椴木";
            case "bamboo" -> "竹材";
            case "paulownia" -> "泡桐木";
            case "pvc" -> "PVC";
            case "wps" -> "木塑";
            default -> name;
        };
    }

    private static String buildMaterialNameCn(String rawName, String typeCode, String color, String spec) {
        String name = firstNonBlank(rawName, spec);
        String colorCn = translateColor(firstNonBlank(color, extractTrailingColor(name).orElse(null)));
        String normalized = normalizeLegacyName(name);
        String prefix = switch (typeCode) {
            case "ROLLER_FABRIC" -> "卷帘面料";
            case "BLACKOUT_FABRIC" -> "遮光面料";
            case "SOLAR_FABRIC" -> "阳光面料";
            case "ZEBRA_FABRIC" -> "斑马帘面料";
            case "ROMAN_FABRIC" -> "罗马帘面料";
            case "OUTDOOR_FABRIC" -> "户外帘面料";
            case "HONEYCOMB_FABRIC" -> "蜂巢帘面料";
            case "WOVEN_WOOD_FABRIC" -> "木百叶/竹帘材料";
            case "SOLAR_PANEL" -> "太阳能板";
            case "CARTON" -> "纸箱";
            case "PET_BOX" -> "PET盒";
            case "MANUAL" -> "说明书";
            case "LABEL" -> "标签";
            case "COLOR_CARD" -> "彩卡";
            default -> null;
        };
        if (prefix != null && !startsWithChinesePrefix(normalized, prefix)) {
            if (!isBlank(colorCn)) {
                return prefix + " " + colorCn + " " + normalized;
            }
            return prefix + " " + normalized;
        }
        if (!isBlank(colorCn) && isMostlyCode(normalized)) {
            return normalized + " " + colorCn;
        }
        return normalized;
    }

    private static Optional<String> extractTrailingColor(String value) {
        Matcher matcher = Pattern.compile("--\\s*([A-Za-z ]+)$").matcher(nullToEmpty(value));
        if (matcher.find()) {
            return Optional.of(matcher.group(1).trim());
        }
        Matcher wordMatcher = Pattern.compile("\\b(White|Black|Night Black|Grey|Gray|Light Grey|Light Gray|Dark Grey|Dark Gray|Beige|Khaki|Brown|Coffee|Chocolate|Blue|Cream|Cube)$", Pattern.CASE_INSENSITIVE)
            .matcher(nullToEmpty(value).trim());
        if (wordMatcher.find()) {
            return Optional.of(wordMatcher.group(1).trim());
        }
        return Optional.empty();
    }

    private static String normalizeLegacyName(String value) {
        return nullToEmpty(value)
            .replaceAll("\\s+", " ")
            .replace("--", " ")
            .replace("  ", " ")
            .trim();
    }

    private static boolean startsWithChinesePrefix(String value, String prefix) {
        return nullToEmpty(value).startsWith(prefix);
    }

    private static boolean isMostlyCode(String value) {
        String text = nullToEmpty(value);
        if (text.isEmpty()) {
            return false;
        }
        long ascii = text.chars().filter(ch -> ch < 128).count();
        return ascii * 1.0 / text.length() > 0.8;
    }

    private static String translateColor(String color) {
        if (isBlank(color)) {
            return null;
        }
        String key = color.trim().toLowerCase(Locale.ROOT);
        return switch (key) {
            case "white" -> "白色";
            case "black", "night black" -> "黑色";
            case "grey", "gray", "light grey", "light gray" -> "灰色";
            case "dark grey", "dark gray" -> "深灰色";
            case "beige" -> "米色";
            case "khaki" -> "卡其色";
            case "brown" -> "棕色";
            case "light brown" -> "浅棕色";
            case "coffee" -> "咖啡色";
            case "chocolate" -> "巧克力色";
            case "blue" -> "蓝色";
            case "cream" -> "奶油色";
            case "cube" -> "方格";
            case "ivory" -> "象牙白";
            case "sand" -> "沙色";
            case "linen" -> "亚麻色";
            case "latte" -> "拿铁色";
            case "pinstripe" -> "细条纹";
            case "wide stripe" -> "宽条纹";
            case "hybrid" -> "混合纹";
            default -> color;
        };
    }

    private static Optional<String> findColor(String text) {
        Optional<String> labeled = findLabeledValue(text, "颜色");
        if (labeled.isPresent()) {
            return labeled;
        }
        Optional<String> trailing = extractTrailingColor(text);
        if (trailing.isPresent()) {
            return Optional.of(translateColor(trailing.get()));
        }
        for (String color : List.of("白色", "灰色", "黑色", "银白", "银色", "透明", "米色")) {
            if (nullToEmpty(text).contains(color)) {
                return Optional.of(color);
            }
        }
        return Optional.empty();
    }

    private static Optional<String> findLabeledValue(String text, String... labels) {
        for (String label : labels) {
            Pattern pattern = Pattern.compile(label + "\\s*[:：]\\s*([^;；,，\\n\\r]+)");
            Matcher matcher = pattern.matcher(nullToEmpty(text));
            if (matcher.find()) {
                String value = matcher.group(1).trim();
                if (!isBlank(value)) {
                    return Optional.of(value);
                }
            }
        }
        return Optional.empty();
    }

    private static boolean shouldImportCategory(String oldId, String name) {
        String value = (nullToEmpty(oldId) + " " + nullToEmpty(name)).toLowerCase(Locale.ROOT);
        for (String word : EXCLUDED_CATEGORY_WORDS) {
            if (value.contains(word.toLowerCase(Locale.ROOT))) {
                return false;
            }
        }
        return !isBlank(name);
    }

    private static String inferUnitType(String code, String oldType) {
        String value = (nullToEmpty(code) + " " + nullToEmpty(oldType)).toUpperCase(Locale.ROOT);
        if (containsAny(value, "LENGTH", "METER", "FOOT", "INCH", "MM", "CM", "M")) {
            return "LENGTH";
        }
        if (containsAny(value, "WEIGHT", "KG", "G")) {
            return "WEIGHT";
        }
        if (containsAny(value, "AREA", "SQ")) {
            return "AREA";
        }
        return "COUNT";
    }

    private static String normalizeUnitName(String name) {
        return switch (cleanCode(name)) {
            case "EA", "PCS" -> "个";
            case "SET" -> "套";
            case "M", "MTR" -> "米";
            case "KG" -> "千克";
            case "SQM" -> "平方米";
            default -> name;
        };
    }

    private static String nextManufacturerCode(AtomicInteger next) {
        int value = next.getAndIncrement();
        if (value == 900) {
            value = next.getAndIncrement();
        }
        if (value > 999) {
            throw new IllegalStateException("More than 999 manufacturers; cannot assign 3-digit code safely");
        }
        return "%03d".formatted(value);
    }

    private static String cleanCode(String value) {
        String cleaned = nullToEmpty(value).trim().toUpperCase(Locale.ROOT).replaceAll("[^A-Z0-9_]", "_");
        cleaned = cleaned.replaceAll("_+", "_");
        if (cleaned.length() > 64) {
            cleaned = cleaned.substring(0, 64);
        }
        return isBlank(cleaned) ? "UNKNOWN" : cleaned;
    }

    private static String shortName(String value) {
        String name = nullToEmpty(value).trim();
        return name.length() <= 32 ? name : name.substring(0, 32);
    }

    private static String buildSpecModelText(String model, String spec) {
        if (!isBlank(model) && !isBlank(spec)) {
            return "型号：" + model + "；规格：" + spec;
        }
        if (!isBlank(spec)) {
            return spec;
        }
        return model;
    }

    private static String buildSourceRemark(String sourceId) {
        return isBlank(sourceId) ? "source=ofbiz-import" : "source=ofbiz; sourceId=" + sourceId;
    }

    private static long importId(String namespace, String code) {
        CRC32 crc = new CRC32();
        crc.update((namespace + ":" + code).getBytes(StandardCharsets.UTF_8));
        long base = switch (namespace) {
            case "CAT" -> 910_000_000_000_000_000L;
            case "UNIT" -> 911_000_000_000_000_000L;
            case "MFG" -> 912_000_000_000_000_000L;
            case "MTG" -> 913_000_000_000_000_000L;
            case "MT" -> 914_000_000_000_000_000L;
            case "MAT" -> 915_000_000_000_000_000L;
            case "LOG" -> 917_000_000_000_000_000L;
            default -> 919_000_000_000_000_000L;
        };
        return base + (crc.getValue() % 999_999_999_999L);
    }

    private static Timestamp now() {
        return Timestamp.valueOf(LocalDateTime.now());
    }

    private static void bind(PreparedStatement ps, Object... values) throws SQLException {
        for (int i = 0; i < values.length; i++) {
            ps.setObject(i + 1, values[i]);
        }
    }

    private static String sqlLiteral(Object value) {
        if (value == null) {
            return "NULL";
        }
        if (value instanceof Number) {
            return value.toString();
        }
        if (value instanceof Boolean) {
            return ((Boolean) value) ? "TRUE" : "FALSE";
        }
        return "'" + value.toString().replace("'", "''") + "'";
    }

    private static String toJson(MaterialRow row) {
        return "{"
            + "\"materialCode\":\"" + jsonEscape(row.code()) + "\","
            + "\"materialNameCn\":\"" + jsonEscape(row.nameCn()) + "\","
            + "\"materialTypeCode\":\"" + jsonEscape(row.materialTypeCode()) + "\","
            + "\"spec\":\"" + jsonEscape(row.spec()) + "\","
            + "\"manufacturerCode\":\"" + jsonEscape(row.manufacturerCode()) + "\""
            + "}";
    }

    private static String jsonEscape(String value) {
        return nullToEmpty(value).replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private static boolean containsAny(String value, String... candidates) {
        return Arrays.stream(candidates).anyMatch(value::contains);
    }

    private static String firstNonBlank(String... values) {
        for (String value : values) {
            if (!isBlank(value)) {
                return value.trim();
            }
        }
        return "";
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private static String requiredEnv(String name) {
        String value = System.getenv(name);
        if (isBlank(value)) {
            throw new IllegalArgumentException("Missing required env: " + name);
        }
        return value;
    }

    private static String envOrDefault(String name, String defaultValue) {
        String value = System.getenv(name);
        return isBlank(value) ? defaultValue : value;
    }

    private static Path createRunDir(String mode) throws IOException {
        Path runDir = OUTPUT_ROOT.resolve(mode + "-" + LocalDateTime.now().format(DIR_TS) + "-" + UUID.randomUUID().toString().substring(0, 8));
        Files.createDirectories(runDir);
        return runDir;
    }

    private record UnitRow(String code, String nameCn, String nameEn, String type) {
    }

    private record ManufacturerRow(String code, String name, String shortName, boolean manufacturerFlag, boolean supplierFlag, String sourcePartyId) {
    }

    private record PartySupplierCount(String partyId, String name, int productCount) {
    }

    private record SupplierRow(String productId, String partyId, String supplierProductId, String supplierProductName,
                               BigDecimal lastPrice, String quantityUomId) {
    }

    private record CategoryRow(String code, String nameCn, String nameEn, String parentCode, String sourceId) {
        CategoryRow withParentCode(String value) {
            return new CategoryRow(code, nameCn, nameEn, value, sourceId);
        }
    }

    private record MaterialTypeGroupRow(String code, String nameCn, String nameEn, int sortOrder) {
    }

    private record MaterialTypeRow(String code, String nameCn, String nameEn, String groupCode, String groupNameCn, int sortOrder) {
    }

    private record MaterialRow(String code, String nameCn, String nameEn, String materialTypeCode, String materialTypeNameCn,
                               String attributeGroupCode, String attributeGroupNameCn, String unitCode, String secondaryUnitCode,
                               String manufacturerCode, String manufacturerName, String manufacturerItemNo, String model,
                               String spec, String specModelText, String colorName, BigDecimal weightValue, BigDecimal unitPrice,
                               String remark) {
    }

    private static final class ImportData {
        private final List<MaterialTypeGroupRow> typeGroups = new ArrayList<>();
        private final List<MaterialTypeRow> types = new ArrayList<>();
        private final List<UnitRow> units = new ArrayList<>();
        private final List<ManufacturerRow> manufacturers = new ArrayList<>();
        private final List<CategoryRow> categories = new ArrayList<>();
        private final List<MaterialRow> materials = new ArrayList<>();
    }

    private static final class ImportReport {
        private final Map<String, String> facts = new LinkedHashMap<>();
        private final Map<String, Long> counts = new TreeMap<>();
        private final List<String> skips = new ArrayList<>();
        private final List<String> warnings = new ArrayList<>();

        void put(String key, String value) {
            facts.put(key, value);
        }

        void count(String key, long value) {
            counts.merge(key, value, Long::sum);
        }

        void skip(String type, String code, String reason) {
            skips.add(type + "\t" + nullToEmpty(code) + "\t" + reason);
        }

        void warn(String type, String code, String reason) {
            warnings.add(type + "\t" + nullToEmpty(code) + "\t" + reason);
        }

        void write(Path dir) throws IOException {
            Files.createDirectories(dir);
            Files.writeString(dir.resolve("summary.md"), summaryText(), StandardCharsets.UTF_8);
            Files.writeString(dir.resolve("skipped.tsv"), "type\tcode\treason\n" + String.join("\n", skips), StandardCharsets.UTF_8);
            Files.writeString(dir.resolve("warnings.tsv"), "type\tcode\treason\n" + String.join("\n", warnings), StandardCharsets.UTF_8);
        }

        String summaryText() {
            StringBuilder sb = new StringBuilder();
            sb.append("# OFBiz Base Import Report\n\n");
            sb.append("Generated: ").append(LocalDateTime.now().format(HUMAN_TS)).append('\n').append('\n');
            if (!facts.isEmpty()) {
                sb.append("## Facts\n");
                facts.forEach((key, value) -> sb.append("- ").append(key).append(": ").append(value).append('\n'));
                sb.append('\n');
            }
            sb.append("## Counts\n");
            counts.forEach((key, value) -> sb.append("- ").append(key).append(": ").append(value).append('\n'));
            sb.append("- warnings: ").append(warnings.size()).append('\n');
            sb.append("- skipped: ").append(skips.size()).append('\n');
            sb.append('\n');
            sb.append("Reports do not include passwords or raw connection strings.\n");
            return sb.toString();
        }
    }
}
