resource "google_secret_manager_secret" "firebase_credentials" {
  project   = var.project_id
  secret_id = var.firebase_credentials_secret_id
  labels    = var.labels

  replication {
    auto {}
  }

  depends_on = [google_project_service.required]
}

resource "google_secret_manager_secret" "db_password" {
  project   = var.project_id
  secret_id = var.db_password_secret_id
  labels    = var.labels

  replication {
    auto {}
  }

  depends_on = [google_project_service.required]
}
