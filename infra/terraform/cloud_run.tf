locals {
  cloud_sql_volume_name          = "cloudsql"
  firebase_credentials_file_name = "firebase-adminsdk.json"
  firebase_credentials_mount_dir = "/secrets"
  firebase_credentials_path      = "${local.firebase_credentials_mount_dir}/${local.firebase_credentials_file_name}"
  firebase_secret_volume_name    = "firebase-credentials"
}

resource "google_cloud_run_v2_service" "app" {
  project             = var.project_id
  name                = var.service_name
  location            = var.region
  ingress             = var.ingress
  labels              = var.labels
  deletion_protection = var.deletion_protection

  template {
    service_account = google_service_account.runtime.email

    scaling {
      min_instance_count = var.min_instance_count
      max_instance_count = var.max_instance_count
    }

    containers {
      image = var.bootstrap_image

      ports {
        container_port = var.container_port
      }

      env {
        name  = "FIREBASE_CREDENTIALS_PATH"
        value = local.firebase_credentials_path
      }

      env {
        name  = "FIREBASE_STORAGE_BUCKET"
        value = var.firebase_storage_bucket
      }

      dynamic "env" {
        for_each = var.environment_variables

        content {
          name  = env.key
          value = env.value
        }
      }

      volume_mounts {
        name       = local.firebase_secret_volume_name
        mount_path = local.firebase_credentials_mount_dir
      }

      dynamic "volume_mounts" {
        for_each = length(var.cloud_sql_instances) > 0 ? [1] : []

        content {
          name       = local.cloud_sql_volume_name
          mount_path = "/cloudsql"
        }
      }
    }

    volumes {
      name = local.firebase_secret_volume_name

      secret {
        secret = google_secret_manager_secret.firebase_credentials.secret_id

        items {
          version = "latest"
          path    = local.firebase_credentials_file_name
        }
      }
    }

    dynamic "volumes" {
      for_each = length(var.cloud_sql_instances) > 0 ? [1] : []

      content {
        name = local.cloud_sql_volume_name

        cloud_sql_instance {
          instances = var.cloud_sql_instances
        }
      }
    }
  }

  lifecycle {
    ignore_changes = [
      template[0].containers[0].image,
    ]
  }

  depends_on = [
    google_artifact_registry_repository_iam_member.runtime_reader,
    google_project_service.required,
    google_secret_manager_secret_iam_member.runtime_firebase_secret_accessor,
  ]
}

resource "google_cloud_run_v2_service_iam_member" "public_invoker" {
  count = var.allow_unauthenticated ? 1 : 0

  project  = var.project_id
  location = google_cloud_run_v2_service.app.location
  name     = google_cloud_run_v2_service.app.name
  role     = "roles/run.invoker"
  member   = "allUsers"
}
