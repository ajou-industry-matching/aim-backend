locals {
  cloud_sql_volume_name = "cloudsql"
}

resource "google_cloud_run_v2_service" "app" {
  project             = var.project_id
  name                = var.service_name
  location            = var.cloud_run_region
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
        name  = "SPRING_PROFILES_ACTIVE"
        value = var.spring_profiles_active
      }

      env {
        name  = "FIREBASE_STORAGE_BUCKET"
        value = var.firebase_storage_bucket
      }

      env {
        name = "DB_URL"

        value_source {
          secret_key_ref {
            secret  = google_secret_manager_secret.db_url.secret_id
            version = "latest"
          }
        }
      }

      env {
        name  = "DB_USER"
        value = var.db_user
      }

      env {
        name  = "DDL_AUTO"
        value = var.ddl_auto
      }

      env {
        name  = "SPRING_JPA_HIBERNATE_DDL_AUTO"
        value = var.ddl_auto
      }

      env {
        name = "DB_PASSWORD"

        value_source {
          secret_key_ref {
            secret  = google_secret_manager_secret.db_password.secret_id
            version = "latest"
          }
        }
      }

      env {
        name = "FIREBASE_CREDENTIALS_JSON"

        value_source {
          secret_key_ref {
            secret  = google_secret_manager_secret.firebase_credentials.secret_id
            version = "latest"
          }
        }
      }

      env {
        name = "CRAWLER_API_KEY"

        value_source {
          secret_key_ref {
            secret  = google_secret_manager_secret.crawler_api_key.secret_id
            version = "latest"
          }
        }
      }

      env {
        name  = "SPRING_FLYWAY_ENABLED"
        value = "true"
      }

      env {
        name  = "SPRING_FLYWAY_BASELINE_ON_MIGRATE"
        value = "true"
      }

      env {
        name  = "SPRING_FLYWAY_BASELINE_VERSION"
        value = "1"
      }

      dynamic "env" {
        for_each = var.environment_variables

        content {
          name  = env.key
          value = env.value
        }
      }

      dynamic "volume_mounts" {
        for_each = length(var.cloud_sql_instances) > 0 ? [1] : []

        content {
          name       = local.cloud_sql_volume_name
          mount_path = "/cloudsql"
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
    google_secret_manager_secret_iam_member.runtime_crawler_api_key_secret_accessor,
    google_secret_manager_secret_iam_member.runtime_db_password_secret_accessor,
    google_secret_manager_secret_iam_member.runtime_db_url_secret_accessor,
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
