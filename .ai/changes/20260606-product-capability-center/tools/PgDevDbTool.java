import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PgDevDbTool {

    private static final Path CONFIG = Path.of("bocoo-admin/src/main/resources/application-dev.yml");
    private static final Path PRODUCT_SQL = Path.of("sql/postgresql/product_capability.sql");

    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            throw new IllegalArgumentException("Usage: PgDevDbTool preflight|apply|postcheck");
        }
        DbConfig config = readConfig();
        try (Connection connection = DriverManager.getConnection(config.url(), config.username(), config.password())) {
            try (Statement statement = connection.createStatement()) {
                statement.execute("SET TIME ZONE 'UTC'");
            }
            switch (args[0]) {
                case "preflight" -> preflight(connection);
                case "apply" -> applySql(connection);
                case "postcheck" -> postcheck(connection);
                default -> throw new IllegalArgumentException("Unknown command: " + args[0]);
            }
        }
    }

    private static DbConfig readConfig() throws Exception {
        String yml = Files.readString(CONFIG);
        String url = first(yml, "url:\\s*(jdbc:postgresql://\\S+)");
        String username = first(yml, "username:\\s*(\\S+)");
        String password = first(yml, "password:\\s*(\\S+)");
        if (!url.contains("sslmode=")) {
            url = url + (url.contains("?") ? "&" : "?") + "sslmode=disable";
        }
        return new DbConfig(url, username, password);
    }

    private static String first(String text, String pattern) {
        Matcher matcher = Pattern.compile(pattern).matcher(text);
        if (!matcher.find()) {
            throw new IllegalStateException("Missing config pattern: " + pattern);
        }
        return matcher.group(1);
    }

    private static void preflight(Connection connection) throws SQLException {
        System.out.println("preflight.pc_tables=" + scalar(connection, """
            SELECT count(*)
            FROM information_schema.tables
            WHERE table_schema = 'public'
              AND table_name LIKE 'pc_%'
            """));
        printRows(connection, "preflight.pc_table_names", """
            SELECT table_name
            FROM information_schema.tables
            WHERE table_schema = 'public'
              AND table_name LIKE 'pc_%'
            ORDER BY table_name
            """);
        printRows(connection, "preflight.menu_id_conflicts", """
            SELECT menu_id || ':' || menu_name || ':' || COALESCE(perms, '')
            FROM sys_menu
            WHERE menu_id BETWEEN 24000 AND 24099
            ORDER BY menu_id
            """);
        printRows(connection, "preflight.product_perm_conflicts", """
            SELECT menu_id || ':' || menu_name || ':' || COALESCE(perms, '')
            FROM sys_menu
            WHERE perms LIKE 'product:%'
            ORDER BY menu_id
            """);
        printRows(connection, "preflight.role1", """
            SELECT role_id || ':' || role_name || ':' || role_key
            FROM sys_role
            WHERE role_id = 1
            """);
    }

    private static void applySql(Connection connection) throws Exception {
        String sql = Files.readString(PRODUCT_SQL);
        List<String> statements = splitStatements(sql);
        int executed = 0;
        try (Statement statement = connection.createStatement()) {
            for (String part : statements) {
                if (!part.isBlank()) {
                    statement.execute(part);
                    executed++;
                }
            }
        }
        System.out.println("apply.executed_statements=" + executed);
    }

    private static void postcheck(Connection connection) throws SQLException {
        System.out.println("postcheck.pc_tables=" + scalar(connection, """
            SELECT count(*)
            FROM information_schema.tables
            WHERE table_schema = 'public'
              AND table_name LIKE 'pc_%'
            """));
        System.out.println("postcheck.product_menus=" + scalar(connection, """
            SELECT count(*)
            FROM sys_menu
            WHERE menu_id BETWEEN 24000 AND 24099
            """));
        System.out.println("postcheck.role1_product_menus=" + scalar(connection, """
            SELECT count(*)
            FROM sys_role_menu
            WHERE role_id = 1
              AND menu_id BETWEEN 24000 AND 24099
            """));
        printRows(connection, "postcheck.pc_tenant_columns", """
            SELECT table_name || '.' || column_name
            FROM information_schema.columns
            WHERE table_schema = 'public'
              AND table_name LIKE 'pc_%'
              AND column_name = 'tenant_id'
            ORDER BY table_name
            """);
        printRows(connection, "postcheck.missing_table_comments", """
            SELECT c.relname
            FROM pg_class c
            JOIN pg_namespace n ON n.oid = c.relnamespace
            WHERE n.nspname = 'public'
              AND c.relkind = 'r'
              AND c.relname LIKE 'pc_%'
              AND obj_description(c.oid, 'pg_class') IS NULL
            ORDER BY c.relname
            """);
    }

    private static String scalar(Connection connection, String sql) throws SQLException {
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            resultSet.next();
            return resultSet.getString(1);
        }
    }

    private static void printRows(Connection connection, String label, String sql) throws SQLException {
        List<String> values = new ArrayList<>();
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            while (resultSet.next()) {
                values.add(resultSet.getString(1));
            }
        }
        System.out.println(label + "=" + values);
    }

    private static List<String> splitStatements(String sql) {
        List<String> statements = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inSingleQuote = false;
        for (int i = 0; i < sql.length(); i++) {
            char ch = sql.charAt(i);
            current.append(ch);
            if (ch == '\'') {
                inSingleQuote = !inSingleQuote;
            } else if (ch == ';' && !inSingleQuote) {
                statements.add(current.toString());
                current.setLength(0);
            }
        }
        if (!current.isEmpty()) {
            statements.add(current.toString());
        }
        return statements;
    }

    private record DbConfig(String url, String username, String password) {
    }
}
