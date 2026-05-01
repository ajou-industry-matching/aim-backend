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
  description = "Service account email to use when creating the GitHub Actions JSON key."
  value       = google_service_account.deployer.email
}

output "runtime_service_account_email" {
  description = "Cloud Run runtime service account email."
  value       = google_service_account.runtime.email
}

output "firebase_credentials_secret_id" {
  description = "Secret Manager secret ID for Firebase Admin SDK credentials."
  value       = google_secret_manager_secret.firebase_credentials.secret_id
}
