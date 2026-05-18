variable "project_id" {
  description = "Google Cloud project ID."
  type        = string
}

variable "region" {
  description = "Google Cloud region for Artifact Registry and Cloud Run."
  type        = string
  default     = "asia-northeast3"
}

variable "repository_id" {
  description = "Artifact Registry Docker repository ID."
  type        = string
  default     = "aim"
}

variable "service_name" {
  description = "Cloud Run service name."
  type        = string
  default     = "aim-backend"
}

variable "image_name" {
  description = "Container image name inside Artifact Registry."
  type        = string
  default     = "aim-backend"
}

variable "bootstrap_image" {
  description = "Initial container image used when Terraform creates/imports the Cloud Run service. GitHub Actions owns subsequent image revisions."
  type        = string
}

variable "deployer_service_account_id" {
  description = "Service account ID used by GitHub Actions deployment."
  type        = string
  default     = "github-actions-deployer"
}

variable "runtime_service_account_id" {
  description = "Service account ID used by the Cloud Run runtime."
  type        = string
  default     = "aim-backend-runtime"
}

variable "github_repository" {
  description = "GitHub repository allowed to impersonate the deployer service account through Workload Identity Federation."
  type        = string
  default     = "ajou-industry-matching/aim-backend"
}

variable "github_deploy_ref" {
  description = "Git ref allowed to deploy through Workload Identity Federation."
  type        = string
  default     = "refs/heads/main"
}

variable "github_workload_identity_pool_id" {
  description = "Workload Identity Pool ID used for GitHub Actions OIDC."
  type        = string
  default     = "aim-backend-github"
}

variable "github_workload_identity_provider_id" {
  description = "Workload Identity Pool Provider ID used for GitHub Actions OIDC."
  type        = string
  default     = "github"
}

variable "firebase_credentials_secret_id" {
  description = "Secret Manager secret ID for the Firebase Admin SDK JSON credential."
  type        = string
  default     = "aim-backend-firebase-adminsdk"
}

variable "firebase_storage_bucket" {
  description = "Firebase Storage bucket name used by the backend."
  type        = string
  default     = "ajou-project-cafd9.firebasestorage.app"
}

variable "container_port" {
  description = "Container port exposed by the Spring Boot application."
  type        = number
  default     = 8080
}

variable "allow_unauthenticated" {
  description = "Whether to grant public invoker access to the Cloud Run service."
  type        = bool
  default     = true
}

variable "ingress" {
  description = "Cloud Run ingress mode."
  type        = string
  default     = "INGRESS_TRAFFIC_ALL"
}

variable "min_instance_count" {
  description = "Minimum Cloud Run instance count."
  type        = number
  default     = 0
}

variable "max_instance_count" {
  description = "Maximum Cloud Run instance count."
  type        = number
  default     = 2
}

variable "cloud_sql_instances" {
  description = "Optional Cloud SQL connection names to mount at /cloudsql."
  type        = list(string)
  default     = []
}

variable "environment_variables" {
  description = "Additional non-secret environment variables for the Cloud Run service."
  type        = map(string)
  default     = {}

  validation {
    condition = length([
      for key in keys(var.environment_variables) : key
      if contains(["FIREBASE_CREDENTIALS_PATH", "FIREBASE_STORAGE_BUCKET"], key)
    ]) == 0
    error_message = "environment_variables must not contain FIREBASE_CREDENTIALS_PATH or FIREBASE_STORAGE_BUCKET."
  }
}

variable "labels" {
  description = "Labels applied to managed Google Cloud resources."
  type        = map(string)
  default = {
    app     = "aim-backend"
    managed = "terraform"
  }
}

variable "deletion_protection" {
  description = "Whether Terraform should protect the Cloud Run service from deletion."
  type        = bool
  default     = false
}
