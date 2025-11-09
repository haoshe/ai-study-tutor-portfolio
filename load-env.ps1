Get-Content .env | ForEach-Object {
    if ($_ -match '^\s*([^#][^=]*?)\s*=\s*(.*?)\s*$') {
        $name = $matches[1]
        $value = $matches[2]
        Set-Item -Path "env:$name" -Value $value
        Write-Host "Set $name" -ForegroundColor Green
    }
}