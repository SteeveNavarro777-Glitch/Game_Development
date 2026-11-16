# FILE PURPOSE: Compile and launch the gameplay, test or artwork-tool target.
param(
    [ValidateSet('game','test','tools')][string]$Target = 'game',
    [switch]$Run,
    [ValidatePattern('^[A-Za-z][A-Za-z0-9]*$')][string]$EntryPoint = 'RpgVillagePreviews'
)
$ErrorActionPreference = 'Stop'
$projectRoot = $PSScriptRoot
Set-Location -LiteralPath $projectRoot
if (-not (Get-Command javac -ErrorAction SilentlyContinue)) { throw 'Install JDK 17 or newer and add its bin folder to PATH.' }

# BUILD INPUTS: gameplay compiles independently; tests and import tools are separate targets.
$sourceFolders = @('src')
if ($Target -eq 'test') { $sourceFolders += 'tests' }
if ($Target -eq 'tools') { $sourceFolders += 'tools' }
$javaFiles = @($sourceFolders | ForEach-Object { Get-ChildItem -LiteralPath (Join-Path $projectRoot $_) -Filter '*.java' -Recurse -File } | Sort-Object FullName)
$outputName = switch ($Target) { 'test' { 'test-classes' } 'tools' { 'tool-classes' } default { 'classes' } }
$outputFolder = Join-Path (Join-Path $projectRoot 'build') $outputName
New-Item -ItemType Directory -Path $outputFolder -Force | Out-Null
if ((Get-Item -LiteralPath $outputFolder).Attributes -band [IO.FileAttributes]::ReparsePoint) { throw 'Build output cannot be a reparse point.' }

# BUILD HYGIENE: discard only generated class files from this target, preventing stale-code success.
foreach ($compiledFile in Get-ChildItem -LiteralPath $outputFolder -Filter '*.class' -Recurse -File) {
    if (-not $compiledFile.FullName.StartsWith($outputFolder + '\',[StringComparison]::OrdinalIgnoreCase)) { throw 'Class file escaped the output folder.' }
    Remove-Item -LiteralPath $compiledFile.FullName -Force
}
$sourceList = Join-Path (Join-Path $projectRoot 'build') ($Target + '-sources.txt')
$compilerInputs = @($javaFiles | ForEach-Object { '"' + $_.FullName.Replace('\','/') + '"' })
[IO.File]::WriteAllLines($sourceList,$compilerInputs,[Text.UTF8Encoding]::new($false))
& javac -encoding UTF-8 -Xlint:all -d $outputFolder ('@' + $sourceList)
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
Write-Host ('Compiled ' + $javaFiles.Count + ' Java files for ' + $Target + '.')

# ENTRY POINTS: the current resource root is included; old root/build classes are never loaded.
if ($Run) {
    $mainClass = switch ($Target) { 'test' { 'GameTests' } 'tools' { $EntryPoint } default { 'Main' } }
    $javaOptions = @('-cp',($outputFolder + ';' + $projectRoot),$mainClass)
    if ($Target -ne 'game') { $javaOptions = @('-Djava.awt.headless=true') + $javaOptions }
    & java @javaOptions
    exit $LASTEXITCODE
}
