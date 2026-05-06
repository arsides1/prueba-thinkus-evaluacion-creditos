// Modulo: eks
// Cluster EKS gestionado con managed node groups e IRSA habilitado.

terraform {
  required_version = ">= 1.6"
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }
}

module "eks" {
  source  = "terraform-aws-modules/eks/aws"
  version = "~> 20.0"

  cluster_name    = "${var.proyecto}-${var.ambiente}"
  cluster_version = var.k8s_version

  vpc_id     = var.vpc_id
  subnet_ids = var.private_subnet_ids

  // IRSA: IAM Roles for Service Accounts
  // Permite que pods asuman roles AWS sin guardar access keys
  enable_irsa = true

  // Endpoints publicos para que el dev pueda hacer kubectl desde fuera
  // En prd se podria restringir con cluster_endpoint_public_access_cidrs
  cluster_endpoint_public_access  = true
  cluster_endpoint_private_access = true

  // Add-ons gestionados por AWS (no instalados via Helm). Mantenimiento
  // automatico de versiones compatibles con el cluster.
  cluster_addons = {
    coredns = {
      most_recent = true
    }
    kube-proxy = {
      most_recent = true
    }
    vpc-cni = {
      most_recent = true
    }
    aws-ebs-csi-driver = {
      most_recent = true
    }
    eks-pod-identity-agent = {
      most_recent = true
    }
  }

  eks_managed_node_groups = {
    app = {
      // Tipo de instancia segun ambiente
      instance_types = var.ambiente == "prd" ? ["m6i.large"] : ["t3.medium"]
      min_size       = var.ambiente == "prd" ? 2 : 1
      max_size       = var.ambiente == "prd" ? 6 : 3
      desired_size   = var.ambiente == "prd" ? 3 : 2

      // Disco SSD GP3 (mejor que GP2 default y mismo precio)
      disk_size = 50
      capacity_type = "ON_DEMAND"
    }
  }

  tags = var.tags
}
