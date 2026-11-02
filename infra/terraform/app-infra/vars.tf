variable "project_name" {
  default = "fiap-estoque"
}

variable "region_default" {
  default = "us-east-1"
}

variable "tags" {
  default = {
    Name = "fiap-estoque-terraform"
  }
}

variable "db_name" {
  description = "Nome do banco criado na instancia RDS do estoque"
  default     = "estoque"
}

variable "db_instance_class" {
  description = "Classe da instancia RDS"
  default     = "db.t3.micro"
}

variable "db_username" {
  description = "Usuario master do RDS MySQL do estoque"
  type        = string
  sensitive   = true
}

variable "db_password" {
  description = "Senha master do RDS MySQL do estoque"
  type        = string
  sensitive   = true
}
