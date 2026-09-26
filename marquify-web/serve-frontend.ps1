param([int]$Port = 5173)

$listener = [System.Net.HttpListener]::new()
$listener.Prefixes.Add("http://localhost:$Port/")
$listener.Start()
Write-Host "Marquify Web em http://localhost:$Port" -ForegroundColor Green
Write-Host "Pressione Ctrl+C para encerrar."

$contentTypes = @{ ".html" = "text/html; charset=utf-8"; ".css" = "text/css; charset=utf-8"; ".js" = "text/javascript; charset=utf-8" }
try {
    while ($listener.IsListening) {
        $context = $listener.GetContext()
        $relativePath = [uri]::UnescapeDataString($context.Request.Url.AbsolutePath.TrimStart('/'))
        if ([string]::IsNullOrWhiteSpace($relativePath)) { $relativePath = 'index.html' }
        $filePath = [System.IO.Path]::GetFullPath((Join-Path $PSScriptRoot $relativePath))
        if (!$filePath.StartsWith($PSScriptRoot, [System.StringComparison]::OrdinalIgnoreCase) -or !(Test-Path -LiteralPath $filePath -PathType Leaf)) {
            $context.Response.StatusCode = 404
            $context.Response.Close()
            continue
        }
        $extension = [System.IO.Path]::GetExtension($filePath)
        $context.Response.ContentType = $contentTypes[$extension]
        $content = [System.IO.File]::ReadAllBytes($filePath)
        $context.Response.ContentLength64 = $content.Length
        $context.Response.OutputStream.Write($content, 0, $content.Length)
        $context.Response.Close()
    }
} finally {
    $listener.Close()
}
