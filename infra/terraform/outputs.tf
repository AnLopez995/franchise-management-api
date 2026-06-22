output "ecr_repository_url" {
  description = "Push the API image here before applying App Runner."
  value       = aws_ecr_repository.api.repository_url
}

output "app_runner_service_url" {
  description = "Public HTTPS URL of the deployed API."
  value       = "https://${aws_apprunner_service.this.service_url}"
}

output "app_runner_service_arn" {
  description = "ARN of the App Runner service (used to trigger redeployments)."
  value       = aws_apprunner_service.this.arn
}

output "atlas_cluster_srv" {
  description = "Atlas SRV connection string (without credentials)."
  value       = local.cluster_srv
}

output "mongodb_uri" {
  description = "Full MONGODB_URI injected into the service (includes credentials)."
  value       = local.mongodb_uri
  sensitive   = true
}
