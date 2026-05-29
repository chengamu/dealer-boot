Set-Location 'D:\work\base-boot'
$env:SA_TOKEN_JWT_SECRET='bocoo-dev-jwt-secret-change-before-prod-2026'
$env:CRYPTO_AES_KEY='DevCryptoKey2026'
$env:SPRING_PROFILES_ACTIVE='dev'
mvn -f bocoo-admin/pom.xml spring-boot:run "-Dspring-boot.run.arguments=--server.port=18081 --websocket.port=2832"