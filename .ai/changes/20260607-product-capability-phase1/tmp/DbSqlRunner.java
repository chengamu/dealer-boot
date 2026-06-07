import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DbSqlRunner {
    private record DbConfig(String url, String username, String password) {
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 1 || (!"preflight".equals(args[0]) && !"execute".equals(args[0]) && !"postcheck".equals(args[0]))) {
            throw new IllegalArgumentException("Usage: DbSqlRunner preflight|execute|postcheck");
        }

        DbConfig config = readDevConfig();
        try (Connection connection = DriverManager.getConnection(config.url(), config.username(), config.password())) {
            connection.setAutoCommit(false);
            try (Statement statement = connection.createStatement()) {
                statement.execute("SET TIME ZONE 'UTC'");
                if ("execute".equals(args[0])) {
                    String sql = Files.readString(Path.of("sql/postgresql/product_capability.sql"), StandardCharsets.UTF_8);
                    statement.execute(sql);
                    connection.commit();
                    System.out.println("execute=ok");
                } else {
                    connection.commit();
                }
            } catch (Exception e) {
                connection.rollback();
                throw e;
            }

            printSummary(connection, args[0]);
        }
    }

    private static DbConfig readDevConfig() throws Exception {
        String yml = Files.readString(Path.of("bocoo-admin/src/main/resources/application-dev.yml"), StandardCharsets.UTF_8);
        Matcher matcher = Pattern.compile(
                "url:\\s*(jdbc:postgresql://[^\\n]+)\\s*\\n\\s*username:\\s*([^\\n#]+)\\s*\\n\\s*password:\\s*([^\\n#]+)",
                Pattern.MULTILINE).matcher(yml);
        if (!matcher.find()) {
            throw new IllegalStateException("Cannot find dev PostgreSQL datasource config");
        }
        return new DbConfig(matcher.group(1).trim(), matcher.group(2).trim(), matcher.group(3).trim());
    }

    private static void printSummary(Connection connection, String phase) throws Exception {
        System.out.println("phase=" + phase);
        queryOne(connection, "formal_active_menu_count",
                "select count(*) from sys_menu where menu_id between 24200 and 24499 and visible = '1' and status = '1'");
        queryOne(connection, "formal_child_page_count",
                "select count(*) from sys_menu where menu_id between 24200 and 24499 and menu_type = 'C' and visible = '1' and status = '1'");
        queryOne(connection, "formal_button_count",
                "select count(*) from sys_menu where menu_id between 24200 and 24499 and menu_type = 'F' and visible = '1' and status = '1'");
        queryOne(connection, "admin_role_grant_count",
                "select count(*) from sys_role_menu where role_id = 1 and menu_id between 24200 and 24499");
        queryOne(connection, "dict_type_count",
                "select count(*) from sys_dict_type where dict_type in ('product_material_type','product_component_type','product_business_type','product_unit')");
        queryOne(connection, "dict_data_count",
                "select count(*) from sys_dict_data where dict_type in ('product_material_type','product_component_type','product_business_type','product_unit')");
        queryOne(connection, "old_product_capability_visible_status",
                "select visible || '/' || status from sys_menu where menu_id = 24000");
        queryOne(connection, "old_product_capability_active_children",
                "select count(*) from sys_menu where menu_id between 24001 and 24199 and (visible <> '0' or status <> '0')");
        queryList(connection, "formal_roots",
                "select menu_id || ':' || menu_name || ':' || path || ':' || visible || '/' || status from sys_menu where menu_id in (24200,24300,24400) order by menu_id");
    }

    private static void queryOne(Connection connection, String label, String sql) throws Exception {
        try (Statement statement = connection.createStatement(); ResultSet resultSet = statement.executeQuery(sql)) {
            resultSet.next();
            System.out.println(label + "=" + resultSet.getString(1));
        }
    }

    private static void queryList(Connection connection, String label, String sql) throws Exception {
        try (Statement statement = connection.createStatement(); ResultSet resultSet = statement.executeQuery(sql)) {
            StringBuilder builder = new StringBuilder();
            while (resultSet.next()) {
                if (!builder.isEmpty()) {
                    builder.append("|");
                }
                builder.append(resultSet.getString(1));
            }
            System.out.println(label + "=" + builder);
        }
    }
}
