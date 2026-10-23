-- ==============================================================
-- V9__create_reserva_estoque.sql
-- Reserva de estoque por OS e item (peça ou insumo).
--   status: ATIVA, CONFIRMADA ou LIBERADA
--   disponível = quantidade_estoque - quantidade_reservada
-- Sem FK para ordem_servico/peca/insumo: o estoque vai virar um
-- serviço com banco próprio.
-- ==============================================================

CREATE TABLE IF NOT EXISTS `reserva_estoque` (
    `id`               bigint      NOT NULL AUTO_INCREMENT,
    `ordem_servico_id` bigint      NOT NULL,
    `tipo_item`        varchar(20) NOT NULL,
    `item_id`          bigint      NOT NULL,
    `quantidade`       int         NOT NULL,
    `status`           varchar(20) NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_reserva_os_item` (`ordem_servico_id`, `tipo_item`, `item_id`),
    KEY `idx_reserva_os_status` (`ordem_servico_id`, `status`)
);
