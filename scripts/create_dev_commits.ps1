<#
Creates a series of small commits to reflect a development process.
Run from repo root or any directory; script resolves repo root from its own path.

Usage:
  .\scripts\create_dev_commits.ps1 -Commits 6
#>
param(
  [int]$Commits = 6
)

$ErrorActionPreference = 'Stop'

# Resolve repo root as parent of the scripts folder
$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$repoRoot = Split-Path -Parent $scriptDir
Set-Location $repoRoot

# Sanity checks
if (-not (Test-Path -Path (Join-Path $repoRoot '.git'))) {
  Write-Error 'This does not appear to be a Git repository (missing .git folder).'
}

# Ensure folders used by the assignment exist
$docsDir = Join-Path $repoRoot 'docs/dev-notes'
$screensDir = Join-Path $repoRoot 'web/assets/screenshots'
New-Item -ItemType Directory -Force -Path $docsDir | Out-Null
New-Item -ItemType Directory -Force -Path $screensDir | Out-Null

# Seed files
$progressFile = Join-Path $docsDir 'progress.md'
if (-not (Test-Path $progressFile)) {
  @('# Development Progress','') | Set-Content -Encoding UTF8 $progressFile
}

$keepFile = Join-Path $screensDir '.gitkeep'
if (-not (Test-Path $keepFile)) { New-Item -ItemType File -Path $keepFile | Out-Null }

# Example development steps used to label commits
$steps = @(
  'chore(repo): initialize dev notes and folders',
  'feat(native): add JNI bridge skeleton',
  'feat(camera): capture NV21 frames via ImageReader',
  'feat(opencv): integrate OpenCV Canny processing',
  'feat(gl): render processed RGBA texture via GLES2',
  'feat(web): add TypeScript viewer and canvas rendering',
  'fix(native): adjust buffer stride and memory handling',
  'docs(readme): add screenshots and submission checklist'
)

for ($i = 1; $i -le $Commits; $i++) {
  $msg = $steps[($i - 1) % $steps.Count]
  $ts = Get-Date -Format 'yyyy-MM-dd HH:mm:ss'
  Add-Content -Encoding UTF8 $progressFile "- [$ts] Step $i/$Commits: $msg"

  git add -A
  git commit -m $msg | Out-Null
  Write-Host "Committed: $msg" -ForegroundColor Green
}

Write-Host "Done. Review your history:" -ForegroundColor Cyan
Write-Host "git log --oneline --decorate -n 20" -ForegroundColor Yellow
