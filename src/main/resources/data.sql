INSERT INTO tb_role (nome)
VALUES ('ADMIN');
INSERT INTO tb_role (nome)
VALUES ('RESPONSAVEL');

INSERT INTO tb_user (nome, email, password)
VALUES ('marcelo', 'marcelo@email.com', '123456');
INSERT INTO tb_user (nome, email, password)
VALUES ('suelen', 'suelen@email.com', '123456');

INSERT INTO tb_user_role (user_id, role_id)
VALUES (1, 1);
INSERT INTO tb_user_role (user_id, role_id)
VALUES (2, 2);


INSERT INTO TB_CLIENTE (email, nome, metragem, qtd_banheiros, qtd_quartos, qtd_vagas, valor_maximo)
VALUES ('cliente@email.com', 'Cliente', 45, 1, 2, 1, 150000);

INSERT INTO tb_visitas (obs, data_visita, satisfacao, cliente_id, responsavel_id)
VALUES ('cliente impaciente',TIMESTAMP WITH TIME ZONE '2021-11-1T10:00:00Z',TRUE,1,2);


INSERT INTO  tb_cliente_visitas (cliente_id, visitas_id) VALUES (1, 1);

INSERT INTO tb_user_visitas (user_id, visitas_id) VALUES (2, 1);