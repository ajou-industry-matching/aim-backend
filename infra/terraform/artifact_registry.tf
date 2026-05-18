resource "google_artifact_registry_repository" "app" {
  project       = var.project_id
  location      = var.region
  repository_id = var.repository_id
  description   = "Docker images for ${var.service_name}."
  format        = "DOCKER"
  labels        = var.labels

  depends_on = [google_project_service.required]
}
