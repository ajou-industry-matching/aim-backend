output "artifact_registry_repository" {
  description = "Artifact Registry repository name."
  value       = google_artifact_registry_repository.app.name
}

output "cloud_run_service" {
  description = "Cloud Run service name."
  value       = google_cloud_run_v2_service.app.name
}

output "cloud_run_uri" {
  description = "Cloud Run service URI."
  value       = google_cloud_run_v2_service.app.uri
}

output "deployer_service_account_email" {
  description = "Service account email to set as the GitHub Actions GCP_SERVICE_ACCOUNT variable."
  value       = google_service_account.deployer.email
}

output "github_workload_identity_provider" {
  description = "Workload Identity Provider resource name to set as the GitHub Actions GCP_WORKLOAD_IDENTITY_PROVIDER variable."
  value       = "projects/${data.google_project.current.number}/locations/global/workloadIdentityPools/${google_iam_workload_identity_pool.github.workload_identity_pool_id}/providers/${google_iam_workload_identity_pool_provider.github.workload_identity_pool_provider_id}"
}

output "runtime_service_account_email" {
  description = "Cloud Run runtime service account email."
  value       = google_service_account.runtime.email
}

output "firebase_credentials_secret_id" {
  description = "Secret Manager secret ID for Firebase Admin SDK credentials."
  value       = google_secret_manager_secret.firebase_credentials.secret_id
}
