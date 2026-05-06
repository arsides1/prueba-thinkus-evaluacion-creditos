// Modulo: rds
// PostgreSQL 16 con backups automaticos, multi-AZ en prd.

terraform {
  required_version = ">= 1.6"
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }
}

module "rds" {
  source  = "terraform-aws-modules/rds/aws"
  version = "~> 6.0"

  identifier = "${var.proyecto}-${var.ambiente}"

  engine               = "postgres"
  engine_version       = "16.3"
  family               = "postgres16"
  major_engine_version = "16"

  // Tipo de instancia y storage segun ambiente
  instance_class       = var.ambiente == "prd" ? "db.t4g.medium" : "db.t4g.micro"
  allocated_storage    = var.ambiente == "prd" ? 100 : 20
  max_allocated_storage = var.ambiente == "prd" ? 500 : 100
  storage_type         = "gp3"
  storage_encrypted    = true

  db_name  = "prueba_thinkus"
  username = "thinkus_app"
  // El password real se genera automaticamente y se guarda en Secrets Manager.
  manage_master_user_password = true

  // Networking
  db_subnet_group_name   = var.db_subnet_group_name
  vpc_security_group_ids = [aws_security_group.rds.id]
  publicly_accessible    = false

  // Alta disponibilidad
  multi_az = var.ambiente == "prd"

  // Backups: 7 dias en prd, 1 dia en dev
  backup_retention_period = var.ambiente == "prd" ? 7 : 1
  backup_window           = "03:00-04:00"
  maintenance_window      = "Mon:04:00-Mon:05:00"

  // Performance Insights solo en prd (cuesta extra)
  performance_insights_enabled = var.ambiente == "prd"

  deletion_protection = var.ambiente == "prd"
  skip_final_snapshot = var.ambiente != "prd"

  tags = var.tags
}

resource "aws_security_group" "rds" {
  name        = "${var.proyecto}-${var.ambiente}-rds-sg"
  description = "Permite acceso a Postgres desde los pods del cluster EKS"
  vpc_id      = var.vpc_id

  ingress {
    description     = "Postgres desde el SG del cluster EKS"
    from_port       = 5432
    to_port         = 5432
    protocol        = "tcp"
    security_groups = [var.eks_node_security_group_id]
  }

  tags = var.tags
}
