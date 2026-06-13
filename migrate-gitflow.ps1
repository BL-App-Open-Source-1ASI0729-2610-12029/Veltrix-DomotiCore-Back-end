$ErrorActionPreference = "Stop"
$old = "C:\Users\User\Documents\GitHub\Veltrix-DomotiCore-Back-end"
$new = "C:\Users\User\Documents\GitHub\DomotiCore_Back_End"
$tmpRoot = Join-Path $env:TEMP "domoticore-migrate"

if (Test-Path $tmpRoot) { Remove-Item $tmpRoot -Recurse -Force }
New-Item -ItemType Directory -Path $tmpRoot | Out-Null

Set-Location $new

function Copy-TreeFromCommit {
    param([string]$Commit)
    $zip = Join-Path $tmpRoot "$Commit.zip"
    $dir = Join-Path $tmpRoot $Commit
    if (Test-Path $dir) { Remove-Item $dir -Recurse -Force }
    New-Item -ItemType Directory -Path $dir | Out-Null
    git -C $old archive $Commit -o $zip
    Expand-Archive -Path $zip -DestinationPath $dir -Force
    robocopy $dir $new /E /NFL /NDL /NJH /NJS /nc /ns /np | Out-Null
}

function Apply-DiffRange {
    param([string]$From, [string]$To)
    git -C $old diff $From $To | git -C $new apply --ignore-whitespace
}

git checkout main
if (git show-ref --verify --quiet refs/heads/develop) { git branch -D develop }
git checkout -B develop main

$features = @(
    @{ branch = "feature/project-bootstrap"; mode = "tree"; commit = "4496f79"; msg = "feat: add Spring Boot backend scaffold with IAM and phase 1 resources" },
    @{ branch = "feature/phase2-sme-automation"; mode = "diff"; from = "4496f79"; to = "6ef0d15"; msg = "feat: add SME automation operations and phase 2 demo data" },
    @{ branch = "feature/integrations-business-profile"; mode = "diff"; from = "6ef0d15"; to = "004d1f9"; msg = "feat: add business profile service and integration tests" },
    @{ branch = "feature/postgresql-flyway-phase3"; mode = "diff"; from = "004d1f9"; to = "c11abf5"; msg = "feat: add PostgreSQL profile, Flyway migrations and phase 3 modules" },
    @{ branch = "feature/user-profile-settings"; mode = "diff"; from = "c11abf5"; to = "165b210"; msg = "feat: add authenticated user profile and self-scoped user endpoints" },
    @{ branch = "fix/environment-configuration"; mode = "diff"; from = "165b210"; to = "7a051b7"; msg = "fix: align datasource defaults for local development profiles" },
    @{ branch = "feature/user-scoped-sme-resources"; mode = "diff"; from = "7a051b7"; to = "73a273f"; msg = "feat: scope SME resources and operations hub to authenticated users" },
    @{ branch = "docs/readme-update"; mode = "diff"; from = "73a273f"; to = "e87beb9"; msg = "docs: expand README with architecture, endpoints and deployment guide" },
    @{ branch = "refactor/ddd-architecture"; mode = "diff"; from = "e87beb9"; to = "269d84e"; msg = "refactor: align IAM bounded context with DDD and CQRS architecture" }
)

foreach ($f in $features) {
    Write-Host "==> $($f.branch)"
    git checkout develop
    git checkout -B $f.branch
    if ($f.mode -eq "tree") {
        Copy-TreeFromCommit -Commit $f.commit
    } else {
        Apply-DiffRange -From $f.from -To $f.to
    }
    git add -A
    git commit -m $f.msg
    git checkout develop
    git merge --no-ff $f.branch -m "merge: $($f.branch) into develop"
}

git checkout main
git merge --no-ff develop -m "release: v1.0.0 stable DomotiCore backend"
if (git show-ref --tags v1.0.0 2>$null) { git tag -d v1.0.0 | Out-Null }
git tag -a v1.0.0 -m "v1.0.0: first stable DomotiCore backend release"

Write-Host "Migration complete."
git log --oneline --graph --all -25
