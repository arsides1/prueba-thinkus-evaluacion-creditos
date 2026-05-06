variable "nombre" {
  description = "Nombre de la cola"
  type        = string
}

variable "visibility_timeout" {
  type    = number
  default = 60
}

variable "tags" {
  type    = map(string)
  default = {}
}
