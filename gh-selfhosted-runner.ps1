param (
    [Parameter(Position=0)]
    [ValidateSet("start", "stop", "status", "logs", "restart")]
    [string]$Action = "start",

    [string]$Token = "AOA52M7FI32L2YNQHPZREILKOTRXW",
    [string]$RepoUrl = "https://github.com/saifsahil3/silo-launcher-android",
    [string]$ContainerName = "silo-github-runner",
    [string]$RunnerName = "silo-local-runner"
)

switch ($Action.ToLower()) {
    "start" {
        Write-Host "Starting GitHub Self-Hosted Runner container ($ContainerName)..." -ForegroundColor Green
        
        # Remove old container if exists
        docker rm -f $ContainerName 2>$null | Out-Null

        docker run -d --name $ContainerName --restart always `
            -e REPO_URL="$RepoUrl" `
            -e RUNNER_TOKEN="$Token" `
            -e RUNNER_NAME="$RunnerName" `
            -e RUNNER_WORKDIR="/tmp/github-runner" `
            -e EPHEMERAL="false" `
            -v /var/run/docker.sock:/var/run/docker.sock `
            myoung34/github-runner:latest

        if ($LASTEXITCODE -eq 0) {
            Write-Host "Runner container launched successfully!" -ForegroundColor Cyan
            Write-Host "Fetching initial logs..." -ForegroundColor DarkGray
            Start-Sleep -Seconds 3
            docker logs --tail 15 $ContainerName
        } else {
            Write-Host "Failed to launch Docker runner container." -ForegroundColor Red
        }
    }

    "stop" {
        Write-Host "Terminating GitHub Self-Hosted Runner container ($ContainerName)..." -ForegroundColor Yellow
        docker stop $ContainerName 2>$null
        docker rm $ContainerName 2>$null
        Write-Host "Runner container stopped and removed successfully." -ForegroundColor Green
    }

    "status" {
        Write-Host "GitHub Self-Hosted Runner Container Status:" -ForegroundColor Cyan
        docker ps -a --filter "name=$ContainerName"
    }

    "logs" {
        docker logs -f $ContainerName
    }

    "restart" {
        Write-Host "Restarting GitHub Self-Hosted Runner..." -ForegroundColor Yellow
        docker restart $ContainerName
        docker logs --tail 15 $ContainerName
    }
}
