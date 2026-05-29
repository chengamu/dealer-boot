import java.nio.file.*;
import java.sql.*;
import java.util.*;
import java.util.regex.*;

public class MerchantProfilePermissionValidator {
  record DbConfig(String url, String username, String password) {}
  public static void main(String[] args) throws Exception {
    DbConfig cfg = readDbConfig();
    try (Connection c = DriverManager.getConnection(cfg.url(), cfg.username(), cfg.password())) {
      long userId = latestMerchantUser(c);
      Set<String> perms = new TreeSet<>();
      try (PreparedStatement ps = c.prepareStatement("select distinct m.perms from sys_user_role ur join sys_role_menu rm on rm.role_id=ur.role_id join sys_menu m on m.menu_id=rm.menu_id where ur.user_id=? and m.perms in ('merchant:profile:query','merchant:profile:edit')")) {
        ps.setLong(1, userId);
        try (ResultSet rs = ps.executeQuery()) { while (rs.next()) perms.add(rs.getString(1)); }
      }
      int routeCount;
      try (PreparedStatement ps = c.prepareStatement("select count(*) from sys_user_role ur join sys_role_menu rm on rm.role_id=ur.role_id join sys_menu m on m.menu_id=rm.menu_id where ur.user_id=? and m.path='profile' and lower(m.component)='merchant/profile'")) {
        ps.setLong(1, userId);
        try (ResultSet rs = ps.executeQuery()) { rs.next(); routeCount = rs.getInt(1); }
      }
      if (!perms.contains("merchant:profile:query") || !perms.contains("merchant:profile:edit") || routeCount <= 0) {
        throw new IllegalStateException("Merchant profile menu/permissions missing");
      }
      System.out.println("PERMISSION_VALIDATION_OK perms=" + perms + " routeCount=" + routeCount);
    }
  }
  static long latestMerchantUser(Connection c) throws SQLException {
    String sql = "select u.user_id from sys_user u join sys_tenant t on t.tenant_id=u.tenant_id join merchant_profile mp on mp.tenant_id=u.tenant_id where t.tenant_type='MERCHANT' and u.del_flag='0' and u.status='1' order by u.create_time desc nulls last limit 1";
    try (PreparedStatement ps = c.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
      if (!rs.next()) throw new IllegalStateException("No merchant user found");
      return rs.getLong(1);
    }
  }
  static DbConfig readDbConfig() throws Exception {
    String yml = Files.readString(Path.of("bocoo-admin/src/main/resources/application-dev.yml"));
    Matcher m = Pattern.compile("url:\\s*(jdbc:postgresql:[^\\r\\n]+).*?username:\\s*([^\\r\\n]+).*?password:\\s*([^\\r\\n]+)", Pattern.DOTALL).matcher(yml);
    if (!m.find()) throw new IllegalStateException("DB config not found");
    return new DbConfig(m.group(1).trim(), m.group(2).trim(), m.group(3).trim());
  }
}
