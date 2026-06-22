# MongoDB Atlas persistence provisioned as code (the "persistencia como IaC" requirement).

resource "mongodbatlas_project" "this" {
  name   = var.project_name
  org_id = var.atlas_org_id
}

# Free-tier (M0) shared cluster backed by AWS.
resource "mongodbatlas_cluster" "this" {
  project_id = mongodbatlas_project.this.id
  name       = "${var.project_name}-cluster"

  provider_name               = "TENANT"
  backing_provider_name       = "AWS"
  provider_region_name        = var.atlas_region
  provider_instance_size_name = "M0"
}

resource "mongodbatlas_database_user" "this" {
  project_id         = mongodbatlas_project.this.id
  username           = var.db_username
  password           = var.db_password
  auth_database_name = "admin"

  roles {
    role_name     = "readWrite"
    database_name = var.database_name
  }

  scopes {
    name = mongodbatlas_cluster.this.name
    type = "CLUSTER"
  }
}

resource "mongodbatlas_project_ip_access_list" "this" {
  project_id = mongodbatlas_project.this.id
  cidr_block = var.allowed_cidr
  comment    = "Allowed access for ${var.project_name}"
}

locals {
  cluster_srv = mongodbatlas_cluster.this.connection_strings[0].standard_srv

  # Inject credentials and database into the SRV connection string consumed via MONGODB_URI.
  mongodb_uri = format(
    "%s/%s?retryWrites=true&w=majority",
    replace(local.cluster_srv, "mongodb+srv://", "mongodb+srv://${var.db_username}:${var.db_password}@"),
    var.database_name
  )
}
