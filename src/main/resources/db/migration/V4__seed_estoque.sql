-- ==============================================================
-- V4__seed_estoque.sql
-- Carga inicial de peças, insumos e reservas.
-- Espelha o seed do monólito (fiap-mecanica, V16__seed_dados_iniciais.sql):
-- os IDs de peça e insumo são fixos e iguais nos dois bancos, e os
-- ordem_servico_id das reservas correspondem às OS 3 a 8 daquele seed
-- (IDs sequenciais num banco recém-criado).
-- ==============================================================

-- ==============================================================
-- PEÇAS
-- Quantidades já refletem as reservas abaixo:
--   OS aprovadas (OS6, OS7, OS8): reserva CONFIRMADA, estoque baixado.
--   OS abertas (OS3, OS4): reserva ATIVA, estoque intacto, conta em
--   quantidade_reservada.
--   OS5 (CANCELADA): reserva LIBERADA, estoque intacto.
--
--   Filtro de Ar:         60 iniciais; OS6 e OS8 confirmadas = 2 baixados -> 58; OS3 aberta reserva 1
--   Correia Dentada:      30 iniciais; nada baixado                       -> 30; OS4 aberta reserva 1
--   Amortecedor Traseiro: 20 iniciais; OS6 confirmada = 1 baixado          -> 19
--   Pastilha de Freio:    50 iniciais; OS7 confirmada = 4 baixados         -> 46; OS5 liberou 2
--   Disco de Freio:       25 iniciais; OS7 confirmada = 2 baixados         -> 23
-- ==============================================================

INSERT INTO `peca` (`id`, `nome`, `descricao`, `preco`, `quantidade_estoque`, `quantidade_reservada`)
VALUES
    (1, 'Pastilha de Freio Dianteira', 'Pastilha cerâmica de alta performance para eixo dianteiro',           89.90,  46, 0),
    (2, 'Correia Dentada',             'Correia dentada reforçada compatível com motores 1.0 a 2.0',         145.00,  30, 1),
    (3, 'Amortecedor Traseiro',        'Amortecedor a gás de dupla ação para eixo traseiro',                 320.00,  19, 0),
    (4, 'Vela de Ignição',             'Vela iridium de alto desempenho para motores flex',                   35.50, 100, 0),
    (5, 'Filtro de Ar',                'Filtro de ar esportivo lavável de alta filtragem',                    42.00,  58, 1),
    (6, 'Disco de Freio Dianteiro',    'Disco ventilado para eixo dianteiro, compatível com veículos 1.0-2.0', 210.00, 23, 0);

-- ==============================================================
-- INSUMOS
-- Quantidades já refletem as reservas (mesma regra das peças):
--   Óleo de Motor 5W30: 200 iniciais; OS6 (5L) e OS8 (4L) confirmadas = 9 baixados -> 191; OS3 aberta reserva 4
-- ==============================================================

INSERT INTO `insumo` (`id`, `nome`, `descricao`, `preco`, `quantidade_estoque`, `quantidade_reservada`, `unidade_medida`)
VALUES
    (1, 'Óleo de Motor 5W30',        'Óleo sintético para motor a gasolina e flex',              38.00, 191, 4, 'LITRO'),
    (2, 'Fluido de Freio DOT4',      'Fluido de alta performance para sistemas de freio a disco', 25.00, 5000, 0, 'ML'),
    (3, 'Graxa Automotiva',          'Graxa multiuso para articulações e rolamentos',             15.00,  80, 0, 'UNIDADE'),
    (4, 'Líquido de Arrefecimento',  'Líquido refrigerante orgânico para radiador e bloco',      22.00, 150, 0, 'LITRO');

-- ==============================================================
-- RESERVAS
-- ==============================================================

INSERT INTO `reserva_estoque` (`ordem_servico_id`, `tipo_item`, `item_id`, `quantidade`, `status`)
VALUES
    -- OS 3 (DIAGNOSTICO_CONCLUIDO): Filtro de Ar 1x, Óleo 4L
    (3, 'PECA',   5, 1, 'ATIVA'),
    (3, 'INSUMO', 1, 4, 'ATIVA'),
    -- OS 4 (AGUARDANDO_APROVACAO): Correia Dentada 1x
    (4, 'PECA',   2, 1, 'ATIVA'),
    -- OS 5 (CANCELADA): Pastilha 2x liberada
    (5, 'PECA',   1, 2, 'LIBERADA'),
    -- OS 6 (EM_EXECUCAO): Filtro de Ar 1x, Amortecedor 1x, Óleo 5L
    (6, 'PECA',   5, 1, 'CONFIRMADA'),
    (6, 'PECA',   3, 1, 'CONFIRMADA'),
    (6, 'INSUMO', 1, 5, 'CONFIRMADA'),
    -- OS 7 (FINALIZADA): Pastilha 4x, Disco 2x
    (7, 'PECA',   1, 4, 'CONFIRMADA'),
    (7, 'PECA',   6, 2, 'CONFIRMADA'),
    -- OS 8 (ENTREGUE): Filtro de Ar 1x, Óleo 4L
    (8, 'PECA',   5, 1, 'CONFIRMADA'),
    (8, 'INSUMO', 1, 4, 'CONFIRMADA');
