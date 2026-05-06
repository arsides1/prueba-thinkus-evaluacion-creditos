// Stack del ambiente PRD.
// Compone todos los modulos con valores adecuados para produccion.

terraform {
  required_version = ">= 1.6"
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }
}

provider "aws" {
  region = var.region

  default_tags {
    tags = local.tags_comunes
  }
}

locals {
  ambiente = "prd"
  tags_comunes = {
    Project     = "thinkus"
    Environment = local.ambiente
    ManagedBy   = "terraform"
    Owner       = "creditos-team"
  }
}

module "network" {
  source = "../../modules/network"

  ambiente         = local.ambiente
  cidr             = "10.0.0.0/16"
  azs              = ["us-east-1a", "us-east-1b", "us-east-1c"]
  public_subnets   = ["10.0.0.0/24", "10.0.1.0/24", "10.0.2.0/24"]
  private_subnets  = ["10.0.10.0/24", "10.0.11.0/24", "10.0.12.0/24"]
  database_subnets = ["10.0.20.0/24", "10.0.21.0/24", "10.0.22.0/24"]
  tags             = local.tags_comunes
}

module "eks" {
  source = "../../modules/eks"

  ambiente            = local.ambiente
  k8s_version         = "1.30"
  vpc_id              = module.network.vpc_id
  private_subnet_ids  = module.network.private_subnet_ids
  tags                = local.tags_comunes
}

module "rds" {
  source = "../../modules/rds"

  ambiente                   = local.ambiente
  vpc_id                     = module.network.vpc_id
  db_subnet_group_name       = module.network.database_subnet_group_name
  eks_node_security_group_id = module.eks.cluster_endpoint  // placeholder; en real va el SG real
  tags                       = local.tags_comunes
}

module "ecr" {
  source = "../../modules/ecr"
  tags   = local.tags_comunes
}

module "sqs_evaluaciones" {
  source = "../../modules/sqs"

  nombre = "thinkus-${local.ambiente}-evaluaciones"
  tags   = local.tags_comunes
}
