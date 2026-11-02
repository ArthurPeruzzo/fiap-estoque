# ADR-0001 — Infraestrutura própria do serviço (banco, imagem, manifestos e pipelines no repositório)

## Contexto

A Fase 4 exige microsserviços "**cada um com seu próprio repositório, infraestrutura e banco de dados**", com
"**pipeline independente** de CI/CD" (build, testes, qualidade e deploy em Kubernetes) e a regra de que "**nenhum
serviço pode acessar diretamente o banco de outro**". O entregável por repositório inclui Dockerfile, manifestos
Kubernetes, pipelines e evidência de cobertura de testes (mínimo de 80%).

O ambiente é uma conta AWS Academy Lab: só existe a `LabRole` (não dá para criar IAM roles) e há restrições de
serviços. Verificado nesta conta: **todas as operações `mq:*` (Amazon MQ) são negadas**, enquanto EC2/EBS, SQS e SNS
estão liberados.

## Decisão

**1. O que é compartilhado e o que é do serviço.** O cluster EKS e a rede (VPC, subnets) continuam no repositório
`fiap-mecanica-infra-k8s`: um cluster por serviço seria custo sem ganho, e o enunciado não o exige. Tudo o que é
específico do estoque mora **neste repositório**: Terraform (`infra/terraform/app-infra`), manifestos (`k8s/`),
Dockerfile e pipelines (`.github/workflows`). O módulo lê VPC, subnets, security group e nome do cluster por
`terraform_remote_state`, somente leitura.

**2. Banco próprio.** Instância RDS MySQL 8.4 dedicada (`fiap-estoque-db`, a mesma versão dos testes e do compose),
com subnet group, security group e credenciais próprios — secrets distintos dos do `fiap-mecanica`. State próprio
(`tfstate/estoque.tfstate`). O security group libera a porta 3306 só ao cluster.

**3. Exposição.** O Service do estoque é `LoadBalancer` para que o API Gateway alcance as rotas administrativas
(`/peca/**`, `/insumo/**`). O `fiap-mecanica` **não** usa o ELB: chama o endereço interno
`http://fiap-estoque.fiap-estoque.svc.cluster.local`, então reserva e catálogo não saem para a internet. Como o ELB é
público, `/reserva/**` e `/catalogo/**` ficam alcançáveis diretamente; o que as protege é a autenticação serviço a
serviço do [ADR-0007 do fiap-mecanica](https://github.com/ArthurPeruzzo/fiap-mecanica/blob/main/docs/adr/0007-token-de-servico-entre-microsservicos.md)
(sem token de serviço com `aud` e `scope` corretos a resposta é 401/403) e o JWT de administrador nas demais.

**4. Broker.** RabbitMQ **no cluster**, no namespace `mensageria`: StatefulSet de uma réplica com disco EBS gp3
(driver CSI instalado pelo CD), Services `ClusterIP` (o broker e o painel não são expostos) e um Job idempotente que
cria o vhost `fiap` e os usuários de menor privilégio (`mecanica` só publica no exchange `estoque.comandos`;
`estoque` é dono dos recursos `estoque.*`). O Amazon MQ foi descartado por estar bloqueado na conta. Como o broker é
compartilhado (o `fiap-mecanica` publica nele e o serviço de pagamento também), ele está aqui só por enquanto.

**5. Pipelines.** `build-test` (testes + relatório JaCoCo + **gate de 80%** de linhas), `ci` (pull request: build-test,
build da imagem, `terraform fmt/validate/plan`) e `cd` (main: build-test → infra → imagem no ECR → broker → deploy). Nada é
aplicado se testes ou cobertura falharem.

## Consequências

**Positivas**
- O serviço é implantável de forma independente, com o próprio banco, imagem e pipeline, como o enunciado pede.
- Serviços novos (pagamento) copiam este molde sem mexer em repositório central.
- O isolamento de dados é real: instância, credencial e security group próprios.

**Negativas / trade-offs**
- **Terraform de RDS repetido por serviço.** O `fiap-mecanica` mantém o banco no repositório separado herdado da
  Fase 3 (`fiap-mecanica-infra-db`); o estoque o traz para dentro. É uma inconsistência pequena e conhecida.
- **ELB público** para as rotas administrativas: a "entrada única" é o API Gateway por convenção, não por imposição.
  Impô-la exigiria NLB interno + VPC Link.
- **Broker acoplado ao deploy do estoque** enquanto não migrar para a plataforma; um deploy do estoque reaplica o
  broker (idempotente, mas é acoplamento).
- **EBS CSI depende do hop limit do IMDS dos nós.** O driver usa as credenciais da `LabRole` (a Academy não permite
  criar IAM role dedicado) pelo serviço de metadados da instância. Os nós nascem com hop limit 1, e nesse caso o
  controller, que roda em pod, não alcança o IMDS: fica em `CrashLoopBackOff` e o add-on trava em `CREATING` (observado
  na primeira execução do CD nesta conta). O CD passou a ajustar o hop limit para 2 antes de instalar o add-on
  (idempotente). É uma correção que vale enquanto os nós existirem; a definitiva é um launch template no node group do
  repositório do cluster. Plano B se o disco não funcionar: RabbitMQ numa EC2 com disco EBS. Com broker sem disco
  persistente, uma queda perderia mensagens já marcadas como enviadas pela outbox.
- Credenciais da Academy expiram (~4 h): o `scripts/refresh-aws-secrets.sh` as republica nos cinco repositórios.
- Ainda **sem SonarQube** neste serviço (ver README).

## Alternativas consideradas

- **Banco em repositório central de plataforma:** isolamento de dados equivalente, mas o enunciado fala em
  infraestrutura própria por serviço e o acoplamento de ciclo de vida de todos os bancos pesaria.
- **Amazon MQ:** bloqueado na conta (`mq:*` negado).
- **SQS/SNS:** liberados e aceitos pelo enunciado, porém exigiriam reescrever relay e consumidor (e os testes) feitos
  para RabbitMQ.
- **RabbitMQ em EC2:** independe do cluster e tem disco persistente, mas é mais Terraform e uma máquina para cuidar;
  fica como plano B.
- **Service `ClusterIP` + NLB interno + VPC Link:** impõe o API Gateway como entrada única, com mais trabalho.
  Registrado como evolução.
