variable "proyecto" {
  type    = string
  default = "thinkus"
}

variable "ambiente" {
  type = string
}

variable "vpc_id" {
  type = string
}

variable "db_subnet_group_name" {
  type = string
}

variable "eks_node_security_group_id" {
  description = "SG de los nodes EKS para permitir acceso a la BD"
  type        = string
}

variable "tags" {
  type    = map(string)
  default = {}
}
