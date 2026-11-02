# Leitura somente-leitura do state do repositorio fiap-mecanica-infra-k8s — este modulo nunca escreve la.
# VPC, subnets e o security group do cluster sao recursos que o estoque nao possui: le-los por remote
# state mantem o RDS na mesma rede do cluster sem duplicar rede aqui. O cluster EKS e compartilhado
# pelos servicos; banco, imagem e manifestos sao de cada servico.
data "terraform_remote_state" "k8s" {
  backend = "s3"
  config = {
    bucket = "fiap-mecanica"
    key    = "tfstate/terraform.tfstate"
    region = "us-east-1"
  }
}
