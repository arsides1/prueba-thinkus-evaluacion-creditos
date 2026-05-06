output "vpc_id" {
  description = "ID de la VPC creada"
  value       = module.vpc.vpc_id
}

output "private_subnet_ids" {
  description = "IDs de las subnets privadas (donde van los nodes EKS)"
  value       = module.vpc.private_subnets
}

output "public_subnet_ids" {
  description = "IDs de las subnets publicas (donde van los ALB)"
  value       = module.vpc.public_subnets
}

output "database_subnet_group_name" {
  description = "Nombre del subnet group para RDS"
  value       = module.vpc.database_subnet_group_name
}
