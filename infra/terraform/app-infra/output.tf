output "ecr_repository_url" {
  value = aws_ecr_repository.app.repository_url
}

output "db_endpoint" {
  value = aws_db_instance.mysql.endpoint
}

output "db_name" {
  value = aws_db_instance.mysql.db_name
}

# Passthrough do state do cluster — consumido pelo cd.yml (kubectl).
output "eks_cluster_name" {
  value = data.terraform_remote_state.k8s.outputs.eks_cluster_name
}
