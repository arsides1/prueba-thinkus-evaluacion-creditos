variable "proyecto" {
  type    = string
  default = "thinkus"
}

variable "ambiente" {
  type = string
}

variable "k8s_version" {
  description = "Version de Kubernetes a desplegar"
  type        = string
  default     = "1.30"
}

variable "vpc_id" {
  type = string
}

variable "private_subnet_ids" {
  type = list(string)
}

variable "tags" {
  type    = map(string)
  default = {}
}
