param (
    [Parameter(Mandatory=$false, Position=0)]
    [string]$VersionTag,

    [string]$StorePassword = $env:STORE_PASSWORD,
    [string]$KeyPassword = $env:KEY_PASSWORD
)

$ErrorActionPreference = "Stop"

Write-Host "=======================================" -ForegroundColor Cyan
Write-Host "       SILO LOCAL RELEASE BUILD        " -ForegroundColor Cyan
Write-Host "=======================================" -ForegroundColor Cyan

# 1. Version Tag Handling
if (-not $VersionTag) {
    $VersionTag = Read-Host "Enter release tag (e.g., v1.0.0)"
}

if (-not $VersionTag.StartsWith("v")) {
    $VersionTag = "v$VersionTag"
}

Write-Host "`n[1/5] Target Version Tag: $VersionTag" -ForegroundColor Yellow

# 2. Check Keystore
$KeystoreFile = "my-upload-key.jks"
if (-not (Test-Path $KeystoreFile)) {
    if (Test-Path "keystore_b64.txt") {
        Write-Host "[2/5] Decoding keystore_b64.txt into $KeystoreFile..." -ForegroundColor DarkGray
        $b64 = Get-Content -Path "keystore_b64.txt" -Raw
        [System.IO.File]::WriteAllBytes($KeystoreFile, [System.Convert]::FromBase64String($b64.Trim()))
    } else {
        Write-Error "Missing $KeystoreFile and keystore_b64.txt. Please create or decode your release keystore first."
        exit 1
    }
} else {
    Write-Host "[2/5] Keystore file '$KeystoreFile' found." -ForegroundColor DarkGray
}

# 3. Handle Passwords
if (-not $StorePassword) {
    $StorePassword = Read-Host -MaskInput "Enter STORE_PASSWORD"
}
if (-not $KeyPassword) {
    $KeyPassword = Read-Host -MaskInput "Enter KEY_PASSWORD"
}

$env:STORE_PASSWORD = $StorePassword
$env:KEY_PASSWORD = $KeyPassword

# 4. Run Gradle Build
Write-Host "[3/5] Building Prod Release APK & AAB with Gradle..." -ForegroundColor Green
.\gradlew.bat assembleProdRelease bundleProdRelease --no-daemon

if ($LASTEXITCODE -ne 0) {
    Write-Error "Gradle build failed!"
    exit $LASTEXITCODE
}

# 5. Output Verification
$ApkDir = "app\build\outputs\apk\prod\release"
$AabDir = "app\build\outputs\bundle\prodRelease"

$Apks = Get-ChildItem -Path $ApkDir -Filter "*.apk" -ErrorAction SilentlyContinue
$Aabs = Get-ChildItem -Path $AabDir -Filter "*.aab" -ErrorAction SilentlyContinue

Write-Host "`nBuild Artifacts Generated Successfully:" -ForegroundColor Cyan
if ($Apks) {
    foreach ($apk in $Apks) {
        Write-Host "  [APK] $($apk.FullName)" -ForegroundColor Green
    }
}
if ($Aabs) {
    foreach ($aab in $Aabs) {
        Write-Host "  [AAB] $($aab.FullName)" -ForegroundColor Green
    }
}

# 6. Create & Push Git Tag
Write-Host "`n[4/5] Creating Git Tag '$VersionTag'..." -ForegroundColor Yellow
git tag -a $VersionTag -m "Release $VersionTag" 2>$null
if ($LASTEXITCODE -ne 0) {
    Write-Host "Tag '$VersionTag' already exists locally. Overwriting..." -ForegroundColor Yellow
    git tag -f -a $VersionTag -m "Release $VersionTag"
}

Write-Host "[5/5] Pushing Tag '$VersionTag' to Remote..." -ForegroundColor Green
git push origin $VersionTag --force

# Check if GitHub CLI is installed for automated release creation
if (Get-Command gh -ErrorAction SilentlyContinue) {
    Write-Host "GitHub CLI (gh) detected. Creating GitHub Release..." -ForegroundColor Cyan
    $ArtifactPaths = ($Apks | Select-Object -ExpandProperty FullName) + ($Aabs | Select-Object -ExpandProperty FullName)
    gh release create $VersionTag $ArtifactPaths --title "Release $VersionTag" --notes "Production Release $VersionTag"
} else {
    Write-Host "Tag '$VersionTag' pushed to GitHub successfully!" -ForegroundColor Yellow
    Write-Host "Artifacts are located in: $ApkDir and $AabDir" -ForegroundColor DarkGray
}

Write-Host "`nLocal Release Pipeline Completed Successfully!" -ForegroundColor Cyan
