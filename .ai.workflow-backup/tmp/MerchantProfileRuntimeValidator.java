import cn.dev33.satoken.secure.BCrypt;
import java.io.*;
import java.net.*;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.sql.*;
import java.time.*;
import java.util.*;
import java.util.regex.*;

public class MerchantProfileRuntimeValidator {
  static final String API = "http://127.0.0.1:18081";
  static final String TEMP_PASSWORD = "CodexTemp12!";
  static final String TARGET_FIRST = "Runtime";
  static final String TARGET_LAST = "Validation";
  static final String INJECTED_CONTACT = "Injected Contact";
  static String originalPassword;
  static ProfileSnapshot originalProfile;
  static long userId;
  static long tenantId;

  record DbConfig(String url, String username, String password) {}
  record ProfileSnapshot(long merchantId, long tenantId, String merchantName, String companyName, String contactFirstName,
                         String contactLastName, String contactName, String primaryEmail, String officePhone,
                         String mobilePhone, String country, String state, String city, String addressLine1,
                         String addressLine2, String postalCode, String status, String auditStatus, String remark) {}

  public static void main(String[] args) throws Exception {
    DbConfig cfg = readDbConfig();
    try (Connection c = DriverManager.getConnection(cfg.url(), cfg.username(), cfg.password())) {
      c.setAutoCommit(false);
      try {
        selectMerchantUser(c);
        originalProfile = selectProfile(c, tenantId);
        originalPassword = selectPassword(c, userId);
        updatePassword(c, userId, BCrypt.hashpw(TEMP_PASSWORD));
        c.commit();

        String token = login();
        ProfileSnapshot before = getCurrent(token);
        String lockedMerchantName = before.merchantName();
        String lockedCompanyName = before.companyName();
        String lockedEmail = before.primaryEmail();
        String lockedCountry = before.country();

        putCurrent(token, lockedMerchantName, lockedCompanyName, lockedEmail, lockedCountry);
        ProfileSnapshot after = getCurrent(token);

        require(TARGET_FIRST.equals(after.contactFirstName()), "contactFirstName was not updated");
        require(TARGET_LAST.equals(after.contactLastName()), "contactLastName was not updated");
        require((TARGET_FIRST + " " + TARGET_LAST).equals(after.contactName()), "contactName was not derived from first/last");
        require("+1-555-0100".equals(after.officePhone()), "officePhone was not updated");
        require("Runtime City".equals(after.city()), "city was not updated");
        require("Runtime validation remark".equals(after.remark()), "remark was not updated");
        require(Objects.equals(lockedMerchantName, after.merchantName()), "locked merchantName changed");
        require(Objects.equals(lockedCompanyName, after.companyName()), "locked companyName changed");
        require(Objects.equals(lockedEmail, after.primaryEmail()), "locked primaryEmail changed");
        require(Objects.equals(lockedCountry, after.country()), "locked country changed");

        int listCode = getCode(token, API + "/system/merchant/profile/list?pageNum=1&pageSize=1");
        require(listCode != 200, "merchant user unexpectedly accessed platform merchant profile list");
        System.out.println("API_VALIDATION_OK tenantId=" + tenantId + " merchantId=" + after.merchantId() + " platformListCode=" + listCode);
      } finally {
        restore(c);
      }
    }
  }

  static DbConfig readDbConfig() throws IOException {
    String yml = Files.readString(Path.of("bocoo-admin/src/main/resources/application-dev.yml"));
    Matcher m = Pattern.compile("url:\\s*(jdbc:postgresql:[^\\r\\n]+).*?username:\\s*([^\\r\\n]+).*?password:\\s*([^\\r\\n]+)", Pattern.DOTALL).matcher(yml);
    if (!m.find()) throw new IllegalStateException("DB config not found");
    return new DbConfig(m.group(1).trim(), m.group(2).trim(), m.group(3).trim());
  }

  static void selectMerchantUser(Connection c) throws SQLException {
    String sql = "select u.user_id,u.tenant_id from sys_user u join sys_tenant t on t.tenant_id=u.tenant_id " +
      "join merchant_profile mp on mp.tenant_id=u.tenant_id where t.tenant_type='MERCHANT' and u.del_flag='0' and u.status='1' " +
      "order by u.create_time desc nulls last limit 1";
    try (PreparedStatement ps = c.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
      if (!rs.next()) throw new IllegalStateException("No merchant user found for validation");
      userId = rs.getLong("user_id");
      tenantId = rs.getLong("tenant_id");
    }
  }

  static String selectPassword(Connection c, long uid) throws SQLException {
    try (PreparedStatement ps = c.prepareStatement("select password from sys_user where user_id=?")) {
      ps.setLong(1, uid);
      try (ResultSet rs = ps.executeQuery()) {
        if (!rs.next()) throw new IllegalStateException("User disappeared");
        return rs.getString(1);
      }
    }
  }

  static void updatePassword(Connection c, long uid, String hash) throws SQLException {
    try (PreparedStatement ps = c.prepareStatement("update sys_user set password=? where user_id=?")) {
      ps.setString(1, hash);
      ps.setLong(2, uid);
      ps.executeUpdate();
    }
  }

  static ProfileSnapshot selectProfile(Connection c, long tid) throws SQLException {
    String sql = "select merchant_id,tenant_id,merchant_name,company_name,contact_first_name,contact_last_name,contact_name,primary_email,office_phone,mobile_phone,country,state,city,address_line1,address_line2,postal_code,status,audit_status,remark from merchant_profile where tenant_id=?";
    try (PreparedStatement ps = c.prepareStatement(sql)) {
      ps.setLong(1, tid);
      try (ResultSet rs = ps.executeQuery()) {
        if (!rs.next()) throw new IllegalStateException("Profile not found");
        return profileFromRs(rs);
      }
    }
  }

  static void restore(Connection c) throws SQLException {
    if (originalPassword == null || originalProfile == null) return;
    c.setAutoCommit(false);
    updatePassword(c, userId, originalPassword);
    String sql = "update merchant_profile set contact_first_name=?, contact_last_name=?, contact_name=?, office_phone=?, mobile_phone=?, state=?, city=?, address_line1=?, address_line2=?, postal_code=?, remark=? where merchant_id=?";
    try (PreparedStatement ps = c.prepareStatement(sql)) {
      ps.setString(1, originalProfile.contactFirstName());
      ps.setString(2, originalProfile.contactLastName());
      ps.setString(3, originalProfile.contactName());
      ps.setString(4, originalProfile.officePhone());
      ps.setString(5, originalProfile.mobilePhone());
      ps.setString(6, originalProfile.state());
      ps.setString(7, originalProfile.city());
      ps.setString(8, originalProfile.addressLine1());
      ps.setString(9, originalProfile.addressLine2());
      ps.setString(10, originalProfile.postalCode());
      ps.setString(11, originalProfile.remark());
      ps.setLong(12, originalProfile.merchantId());
      ps.executeUpdate();
    }
    c.commit();
  }

  static String login() throws Exception {
    String captcha = http("GET", API + "/captchaImage", null, null).body();
    String uuid = extract(captcha, "\\\"uuid\\\"\\s*:\\s*\\\"([^\\\"]+)\\\"");
    String code = redisGet("captcha_codes:" + uuid).replace("\"", "");
    String body = "{\"username\":\"" + getLoginName() + "\",\"password\":\"" + TEMP_PASSWORD + "\",\"code\":\"" + code + "\",\"uuid\":\"" + uuid + "\"}";
    String resp = http("POST", API + "/login", body, null).body();
    String token = extract(resp, "\\\"token\\\"\\s*:\\s*\\\"([^\\\"]+)\\\"");
    if (token.isBlank()) throw new IllegalStateException("Login token missing");
    return token;
  }

  static String getLoginName() throws SQLException, IOException {
    DbConfig cfg = readDbConfig();
    try (Connection c = DriverManager.getConnection(cfg.url(), cfg.username(), cfg.password()); PreparedStatement ps = c.prepareStatement("select user_name from sys_user where user_id=?")) {
      ps.setLong(1, userId);
      try (ResultSet rs = ps.executeQuery()) { rs.next(); return rs.getString(1); }
    }
  }

  static ProfileSnapshot getCurrent(String token) throws Exception {
    String resp = http("GET", API + "/system/merchant/profile/current", null, token).body();
    require(resp.contains("\"code\":200"), "current profile API did not return code 200");
    return new ProfileSnapshot(longVal(resp,"merchantId"), longVal(resp,"tenantId"), str(resp,"merchantName"), str(resp,"companyName"), str(resp,"contactFirstName"), str(resp,"contactLastName"), str(resp,"contactName"), str(resp,"primaryEmail"), str(resp,"officePhone"), str(resp,"mobilePhone"), str(resp,"country"), str(resp,"state"), str(resp,"city"), str(resp,"addressLine1"), str(resp,"addressLine2"), str(resp,"postalCode"), str(resp,"status"), str(resp,"auditStatus"), str(resp,"remark"));
  }

  static void putCurrent(String token, String merchantName, String companyName, String email, String country) throws Exception {
    String body = "{"
      + "\"merchantName\":\"LOCKED_NAME_SHOULD_NOT_SAVE\","
      + "\"companyName\":\"LOCKED_COMPANY_SHOULD_NOT_SAVE\","
      + "\"primaryEmail\":\"locked@example.invalid\","
      + "\"country\":\"ZZ\","
      + "\"status\":\"0\","
      + "\"auditStatus\":\"REJECTED\","
      + "\"contactFirstName\":\"" + TARGET_FIRST + "\","
      + "\"contactLastName\":\"" + TARGET_LAST + "\","
      + "\"contactName\":\"" + INJECTED_CONTACT + "\","
      + "\"officePhone\":\"+1-555-0100\","
      + "\"mobilePhone\":\"+1-555-0101\","
      + "\"state\":\"Runtime State\","
      + "\"city\":\"Runtime City\","
      + "\"addressLine1\":\"Runtime Address 1\","
      + "\"addressLine2\":\"Runtime Address 2\","
      + "\"postalCode\":\"90001\","
      + "\"remark\":\"Runtime validation remark\"} specific";
    body = body.replace("} specific", "}");
    String resp = http("PUT", API + "/system/merchant/profile/current", body, token).body();
    require(resp.contains("\"code\":200"), "update current profile API did not return code 200");
  }

  static int getCode(String token, String url) throws Exception {
    String resp = http("GET", url, null, token).body();
    Matcher m = Pattern.compile("\\\"code\\\"\\s*:\\s*(\\d+)").matcher(resp);
    return m.find() ? Integer.parseInt(m.group(1)) : 200;
  }

  static HttpResponse<String> http(String method, String url, String body, String token) throws Exception {
    HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    HttpRequest.Builder b = HttpRequest.newBuilder(URI.create(url)).timeout(Duration.ofSeconds(20)).header("Content-Language", "zh_CN");
    if (token != null) b.header("Authorization", "Bearer " + token);
    if (body != null) b.header("Content-Type", "application/json").method(method, HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8));
    else b.method(method, HttpRequest.BodyPublishers.noBody());
    return client.send(b.build(), HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
  }

  static String redisGet(String key) throws Exception {
    try (Socket s = new Socket("127.0.0.1", 6379)) {
      s.setSoTimeout(3000);
      OutputStream out = s.getOutputStream();
      out.write(("*2\r\n$3\r\nGET\r\n$" + key.getBytes(StandardCharsets.UTF_8).length + "\r\n" + key + "\r\n").getBytes(StandardCharsets.UTF_8));
      out.flush();
      BufferedReader br = new BufferedReader(new InputStreamReader(s.getInputStream(), StandardCharsets.UTF_8));
      String header = br.readLine();
      if (header == null || header.startsWith("$-1")) throw new IllegalStateException("Redis key missing");
      String value = br.readLine();
      return value == null ? "" : value;
    }
  }

  static ProfileSnapshot profileFromRs(ResultSet rs) throws SQLException {
    return new ProfileSnapshot(rs.getLong("merchant_id"), rs.getLong("tenant_id"), rs.getString("merchant_name"), rs.getString("company_name"), rs.getString("contact_first_name"), rs.getString("contact_last_name"), rs.getString("contact_name"), rs.getString("primary_email"), rs.getString("office_phone"), rs.getString("mobile_phone"), rs.getString("country"), rs.getString("state"), rs.getString("city"), rs.getString("address_line1"), rs.getString("address_line2"), rs.getString("postal_code"), rs.getString("status"), rs.getString("audit_status"), rs.getString("remark"));
  }

  static String extract(String text, String regex) {
    Matcher m = Pattern.compile(regex).matcher(text);
    if (!m.find()) throw new IllegalStateException("Pattern not found");
    return m.group(1);
  }
  static String str(String json, String key) {
    Matcher m = Pattern.compile("\\\"" + key + "\\\"\\s*:\\s*(null|\\\"([^\\\"]*)\\\")").matcher(json);
    if (!m.find() || "null".equals(m.group(1))) return null;
    return m.group(2);
  }
  static long longVal(String json, String key) {
    Matcher m = Pattern.compile("\\\"" + key + "\\\"\\s*:\\s*(\\d+)").matcher(json);
    if (!m.find()) return 0;
    return Long.parseLong(m.group(1));
  }
  static void require(boolean ok, String msg) {
    if (!ok) throw new IllegalStateException(msg);
  }
}

