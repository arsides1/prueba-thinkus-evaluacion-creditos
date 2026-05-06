output "repository_urls" {
  description = "Mapa nombre -> URL de cada repositorio creado"
  value       = { for k, v in aws_ecr_repository.este : k => v.repository_url }
}
