output "endpoint" {
  description = "Endpoint Postgres (host:puerto)"
  value       = module.rds.db_instance_endpoint
}

output "secret_arn" {
  description = "ARN del Secret en Secrets Manager con el password generado"
  value       = module.rds.db_instance_master_user_secret_arn
}

output "instance_id" {
  value = module.rds.db_instance_identifier
}
