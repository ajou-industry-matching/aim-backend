resource "google_secret_manager_secret" "firebase_credentials" {
  project   = var.project_id
  secret_id = var.firebase_credentials_secret_id
  labels    = var.labels

  replication {
    auto {}
  }

  depends_on = [google_project_service.required]
}
