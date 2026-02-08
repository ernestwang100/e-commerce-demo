$ErrorActionPreference = "Stop"

# 1. Login
$loginBody = @{
    username = "admin"
    password = "123"
} | ConvertTo-Json

$loginResponse = Invoke-RestMethod -Uri "http://localhost:7070/login" -Method Post -Body $loginBody -ContentType "application/json"
$token = $loginResponse.token
Write-Host "Logged in. Token: $token"

# 2. Add Product (Triggers Sync)
$productName = "ElasticTestProduct"
$productBody = @{
    name = $productName
    description = "A product to test Elasticsearch sync"
    wholesalePrice = 10.0
    retailPrice = 20.0
    quantity = 100
} | ConvertTo-Json

$headers = @{
    Authorization = "Bearer $token"
}

try {
    $addProductResponse = Invoke-RestMethod -Uri "http://localhost:7070/products" -Method Post -Body $productBody -ContentType "application/json" -Headers $headers
    Write-Host "Product Added: $($addProductResponse.name)"
} catch {
    Write-Host "Failed to add product: $_"
    exit 1
}

# 3. Wait for Sync (Near Real-time)
Start-Sleep -Seconds 2

# 4. Search Product (Uses ES)
try {
    $searchResponse = Invoke-RestMethod -Uri "http://localhost:7070/products/search?query=$productName" -Method Get -Headers $headers
    Write-Host "Search Results: $($searchResponse.Count)"
    
    if ($searchResponse.Count -gt 0 -and $searchResponse[0].name -eq $productName) {
        Write-Host "SUCCESS: Product found in Elasticsearch!"
    } else {
        Write-Host "FAILURE: Product NOT found in Elasticsearch."
        exit 1
    }
} catch {
    Write-Host "Failed to search product: $_"
    exit 1
}
