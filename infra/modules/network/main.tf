// Modulo: network
// Crea una VPC con subnets publicas/privadas/aisladas en 3 AZ y NAT Gateway.
// Usa el modulo oficial terraform-aws-modules/vpc para no reinventar la rueda.

terraform {
  required_version = ">= 1.6"
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }
}

module "vpc" {
  source  = "terraform-aws-modules/vpc/aws"
  version = "~> 5.0"

  name = "${var.proyecto}-${var.ambiente}"
  cidr = var.cidr

  azs              = var.azs
  public_subnets   = var.public_subnets
  private_subnets  = var.private_subnets
  database_subnets = var.database_subnets

  // En prd una NAT por AZ (alta disponibilidad). En dev una sola para ahorrar costos.
  enable_nat_gateway   = true
  single_nat_gateway   = var.ambiente != "prd"
  one_nat_gateway_per_az = var.ambiente == "prd"

  // DNS imprescindible para que EKS resuelva nombres dentro del cluster
  enable_dns_hostnames = true
  enable_dns_support   = true

  // Tags requeridos por el AWS Load Balancer Controller para identificar
  // las subnets donde puede crear ALBs.
  public_subnet_tags = {
    "kubernetes.io/role/elb" = 1
  }
  private_subnet_tags = {
    "kubernetes.io/role/internal-elb" = 1
  }

  tags = var.tags
}
