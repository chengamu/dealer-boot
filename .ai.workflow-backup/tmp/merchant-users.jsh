String url = "jdbc:postgresql://192.168.120.67:5432/dealer?stringtype=unspecified";
try (java.sql.Connection c = java.sql.DriverManager.getConnection(url, "root", "123456")) {
  String sql = "select u.user_id,u.tenant_id,u.user_name,u.email,t.tenant_type,mp.merchant_id from sys_user u join sys_tenant t on t.tenant_id=u.tenant_id left join merchant_profile mp on mp.tenant_id=u.tenant_id where t.tenant_type='MERCHANT' and u.del_flag='0' order by u.create_time desc nulls last limit 5";
  try (java.sql.PreparedStatement ps = c.prepareStatement(sql); java.sql.ResultSet rs = ps.executeQuery()) {
    while (rs.next()) {
      System.out.println(rs.getLong("user_id")+"\t"+rs.getLong("tenant_id")+"\t"+rs.getString("user_name")+"\t"+rs.getString("email")+"\tmerchantId="+rs.getLong("merchant_id"));
    }
  }
}
/exit
