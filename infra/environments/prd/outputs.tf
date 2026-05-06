output "vpc_id"          { value = module.network.vpc_id }
output "cluster_name"    { value = module.eks.cluster_name }
output "cluster_endpoint"{ value = module.eks.cluster_endpoint }
output "rds_endpoint"    { value = module.rds.endpoint }
output "ecr_repos"       { value = module.ecr.repository_urls }
output "sqs_url"         { value = module.sqs_evaluaciones.queue_url }
