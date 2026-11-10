# 시크릿 이름은 배포 워크플로(.github/workflows/deploy-cloud-run.yml)가 소유한다.
# 워크플로가 이 이름으로 생성·갱신하고 같은 이름으로 서비스에 주입하므로 변수로 두지 않는다.

resource "google_secret_manager_secret" "firebase_credentials" {
  project   = var.project_id
  secret_id = "FIREBASE_CREDENTIALS_JSON"
  labels    = var.labels

  replication {
    auto {}
  }

  depends_on = [google_project_service.required]
}

resource "google_secret_manager_secret" "db_password" {
  project   = var.project_id
  secret_id = "DB_PASSWORD"
  labels    = var.labels

  replication {
    auto {}
  }

  depends_on = [google_project_service.required]
}

resource "google_secret_manager_secret" "db_url" {
  project   = var.project_id
  secret_id = "DB_URL"
  labels    = var.labels

  replication {
    auto {}
  }

  depends_on = [google_project_service.required]
}

resource "google_secret_manager_secret" "crawler_api_key" {
  project   = var.project_id
  secret_id = "CRAWLER_API_KEY"
  labels    = var.labels

  replication {
    auto {}
  }

  depends_on = [google_project_service.required]
}
