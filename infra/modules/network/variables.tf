variable "proyecto" {
  description = "Nombre del proyecto (prefijo de recursos)"
  type        = string
  default     = "thinkus"
}

variable "ambiente" {
  description = "Ambiente (dev, prd, etc.)"
  type        = string
}

variable "cidr" {
  description = "CIDR de la VPC"
  type        = string
  default     = "10.0.0.0/16"
}

variable "azs" {
  description = "Availability zones a usar"
  type        = list(string)
}

variable "public_subnets" {
  description = "CIDRs de subnets publicas (una por AZ)"
  type        = list(string)
}

variable "private_subnets" {
  description = "CIDRs de subnets privadas (una por AZ) - donde corren los pods"
  type        = list(string)
}

variable "database_subnets" {
  description = "CIDRs de subnets aisladas para RDS (sin NAT)"
  type        = list(string)
}

variable "tags" {
  description = "Tags comunes a aplicar"
  type        = map(string)
  default     = {}
}
