output "vpc_id"          { value = module.network.vpc_id }
output "cluster_name"    { value = module.eks.cluster_name }
output "rds_endpoint"    { value = module.rds.endpoint }
output "ecr_repos"       { value = module.ecr.repository_urls }
