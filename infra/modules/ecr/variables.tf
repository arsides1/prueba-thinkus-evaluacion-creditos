variable "repositorios" {
  description = "Lista de repositorios a crear (ej. thinkus/ms-orquestador)"
  type        = list(string)
  default     = ["thinkus/ms-orquestador", "thinkus/ms-riesgos", "thinkus/frontend"]
}

variable "tags" {
  type    = map(string)
  default = {}
}
