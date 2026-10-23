CREATE TABLE IF NOT EXISTS `insumo` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `nome` varchar(255) NOT NULL,
    `descricao` varchar(255) NOT NULL,
    `preco` decimal(10,2) NOT NULL,
    `quantidade_estoque` int NOT NULL,
    `quantidade_reservada` int NOT NULL DEFAULT 0,
    `unidade_medida` varchar(255) NOT NULL,
    PRIMARY KEY (`id`)
);
