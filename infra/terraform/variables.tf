variable "project_name" {
  description = "Logical name used to prefix the created resources."
  type        = string
  default     = "franchises-api"
}

# --- AWS ---
variable "aws_region" {
  description = "AWS region where ECR and App Runner are created."
  type        = string
  default     = "us-east-1"
}

variable "app_cpu" {
  description = "App Runner vCPU units (e.g. 1024 = 1 vCPU)."
  type        = string
  default     = "1024"
}

variable "app_memory" {
  description = "App Runner memory in MB (e.g. 2048 = 2 GB)."
  type        = string
  default     = "2048"
}

variable "image_tag" {
  description = "Container image tag deployed to App Runner (must already be pushed to ECR)."
  type        = string
  default     = "latest"
}

# --- MongoDB Atlas ---
variable "atlas_public_key" {
  description = "MongoDB Atlas API public key."
  type        = string
  sensitive   = true
}

variable "atlas_private_key" {
  description = "MongoDB Atlas API private key."
  type        = string
  sensitive   = true
}

variable "atlas_org_id" {
  description = "MongoDB Atlas organization ID where the project is created."
  type        = string
}

variable "atlas_region" {
  description = "Atlas region name for the M0 cluster (AWS-backed)."
  type        = string
  default     = "US_EAST_1"
}

variable "db_username" {
  description = "MongoDB application database username."
  type        = string
  default     = "franchises_app"
}

variable "db_password" {
  description = "MongoDB application database password."
  type        = string
  sensitive   = true
}

variable "database_name" {
  description = "Application database name."
  type        = string
  default     = "franchises_db"
}

variable "allowed_cidr" {
  description = "CIDR allowed to reach the Atlas cluster. App Runner egress IPs are dynamic, so 0.0.0.0/0 is the simplest option; restrict it in production."
  type        = string
  default     = "0.0.0.0/0"
}
