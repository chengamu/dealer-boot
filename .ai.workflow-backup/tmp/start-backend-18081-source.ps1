Set-Location 'D:\work\base-boot'
$env:SA_TOKEN_JWT_SECRET='bocoo-dev-jwt-secret-change-before-prod-2026'
$env:CRYPTO_AES_KEY='DevCryptoKey2026'
java '@.ai/tmp/backend-18081-source.args'
