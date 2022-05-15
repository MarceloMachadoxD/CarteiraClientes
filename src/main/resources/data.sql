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


INSERT INTO TB_CLIENTE (email, nome, metragem, qtd_banheiros, qtd_quartos, qtd_vagas, valor_maximo, obs)
VALUES ('cliente@email.com', 'Cliente', 45, 1, 2, 1, 150000, 'Cliente quer quarto vista mar'),
        ('feugiat.placerat@google.net','Eagan Owen',71,2,2,5,519793,'augue porttitor interdum. Sed auctor odio a purus. Duis elementum,'),
        ('euismod@outlook.ca','Hasad Burch',49,1,3,6,170316,'at pede. Cras vulputate velit eu sem. Pellentesque ut ipsum'),
        ('bibendum.ullamcorper@yahoo.edu','Chaney Scott',41,2,2,5,488059,'mi fringilla mi lacinia mattis. Integer eu lacus. Quisque imperdiet,'),
        ('feugiat.tellus@aol.couk','Hanae Wood',52,2,2,3,203873,'Lorem ipsum dolor sit amet, consectetuer adipiscing elit. Aliquam auctor,'),
        ('elit@protonmail.org','Elvis Vazquez',49,2,2,7,453924,'dictum eu, placerat eget, venenatis a, magna. Lorem ipsum dolor'),
        ('urna.ut@protonmail.org','Simon Haley',76,1,1,8,332678,'semper auctor. Mauris vel turpis. Aliquam adipiscing lobortis risus. In'),
        ('at.velit@protonmail.couk','Hamilton Howell',41,1,3,2,501262,'nec, eleifend non, dapibus rutrum, justo. Praesent luctus. Curabitur egestas'),
        ('et.ultrices.posuere@hotmail.com','Cameran Horn',51,2,2,4,566326,'amet massa. Quisque porttitor eros nec tellus. Nunc lectus pede,'),
        ('viverra@hotmail.edu','Ulysses Robles',97,1,3,4,433805,'orci, in consequat enim diam vel arcu. Curabitur ut odio'),
        ('sed.et.libero@icloud.ca','Kasimir Alford',99,1,2,9,173211,'luctus. Curabitur egestas nunc sed libero. Proin sed turpis nec'),
        ('nunc.est@protonmail.net','Zachery Walters',65,2,2,0,257769,'Nulla semper tellus id nunc interdum feugiat. Sed nec metus'),
        ('quis.tristique@aol.net','Wyoming Chambers',47,2,2,5,584205,'Praesent eu nulla at sem molestie sodales. Mauris blandit enim'),
        ('rutrum@protonmail.net','Chava Fox',56,1,3,5,286448,'ac ipsum. Phasellus vitae mauris sit amet lorem semper auctor.'),
        ('donec.tincidunt@google.ca','Sage Day',50,1,2,7,426985,'a ultricies adipiscing, enim mi tempor lorem, eget mollis lectus'),
        ('quisque.purus@hotmail.org','Jackson Mayo',44,1,2,6,398856,'Praesent eu dui. Cum sociis natoque penatibus et magnis dis'),
        ('ornare@icloud.net','Clark Taylor',76,1,2,3,404241,'nisi. Aenean eget metus. In nec orci. Donec nibh. Quisque'),
        ('auctor.vitae@aol.edu','Elvis Douglas',62,1,3,1,573497,'Quisque imperdiet, erat nonummy ultricies ornare, elit elit fermentum risus,'),
        ('montes@outlook.ca','Finn Webster',52,1,2,8,312477,'eros turpis non enim. Mauris quis turpis vitae purus gravida'),
        ('mauris.a.nunc@outlook.couk','Karleigh Carr',100,2,3,9,207358,'feugiat non, lobortis quis, pede. Suspendisse dui. Fusce diam nunc,'),
        ('varius.et@outlook.net','Delilah Mercado',52,1,3,5,452606,'Aenean sed pede nec ante blandit viverra. Donec tempus, lorem'),
        ('nec.euismod@icloud.ca','Mariam Johns',83,2,3,7,409296,'sit amet luctus vulputate, nisi sem semper erat, in consectetuer'),
        ('sem@hotmail.com','Ursa Maynard',50,1,1,9,379984,'nec luctus felis purus ac tellus. Suspendisse sed dolor. Fusce'),
        ('ut.nec@aol.com','Josephine Watkins',53,2,1,5,305260,'dignissim lacus. Aliquam rutrum lorem ac risus. Morbi metus. Vivamus'),
        ('egestas.a@aol.couk','Lydia Rogers',81,1,2,2,395569,'porttitor tellus non magna. Nam ligula elit, pretium et, rutrum'),
        ('eu.sem@protonmail.edu','Holly Pace',52,2,3,4,342694,'lorem vitae odio sagittis semper. Nam tempor diam dictum sapien.'),
        ('velit@icloud.edu','Bryar Church',47,1,3,9,200465,'est arcu ac orci. Ut semper pretium neque. Morbi quis'),
        ('ac.sem@aol.org','Dorothy Ferguson',61,2,3,3,262386,'rutrum magna. Cras convallis convallis dolor. Quisque tincidunt pede ac'),
        ('eleifend.non.dapibus@icloud.org','Glenna Buckley',85,1,2,6,398805,'tellus. Phasellus elit pede, malesuada vel, venenatis vel, faucibus id,'),
        ('orci.lobortis.augue@google.net','Merritt Edwards',97,1,2,3,236039,'tincidunt dui augue eu tellus. Phasellus elit pede, malesuada vel,'),
        ('a.tortor@protonmail.com','Charity Fuller',63,1,2,1,282172,'auctor ullamcorper, nisl arcu iaculis enim, sit amet ornare lectus'),
        ('nec.enim@hotmail.com','Neve Mcleod',82,1,2,7,447093,'Nam ligula elit, pretium et, rutrum non, hendrerit id, ante.'),
        ('euismod.in.dolor@aol.ca','Vaughan Mckinney',94,1,1,2,445199,'tempor erat neque non quam. Pellentesque habitant morbi tristique senectus'),
        ('donec.tincidunt.donec@aol.ca','Armand Wilcox',71,1,2,3,225515,'molestie pharetra nibh. Aliquam ornare, libero at auctor ullamcorper, nisl'),
        ('consectetuer.ipsum.nunc@outlook.ca','Daniel Good',72,2,1,1,526030,'imperdiet dictum magna. Ut tincidunt orci quis lectus. Nullam suscipit,'),
        ('elit.fermentum@protonmail.ca','Audrey Leach',43,1,2,8,267029,'cursus purus. Nullam scelerisque neque sed sem egestas blandit. Nam');

INSERT INTO tb_visitas (obs, data_visita, satisfacao, cliente_id, responsavel_id)
VALUES ('cliente impaciente',TIMESTAMP WITH TIME ZONE '2021-11-1T10:00:00Z',TRUE,1,2),
       ('turpis egestas.',TIMESTAMP WITH TIME ZONE '2021-11-1T10:00:00Z',FALSE,18,2),
       ('metus',TIMESTAMP WITH TIME ZONE '2021-11-1T10:00:00Z',TRUE,10,2),
       ('fringilla cursus',TIMESTAMP WITH TIME ZONE '2021-11-1T10:00:00Z',TRUE,25,2),
       ('amet, risus. Donec nibh enim, gravida sit amet,',TIMESTAMP WITH TIME ZONE '2021-11-1T10:00:00Z',TRUE,17,2),
       ('habitant morbi tristique senectus et netus et',TIMESTAMP WITH TIME ZONE '2021-11-1T10:00:00Z',TRUE,23,2),
       ('non, hendrerit id, ante. Nunc mauris sapien, cursus',TIMESTAMP WITH TIME ZONE '2021-11-1T10:00:00Z',FALSE,14,2),
       ('dis parturient montes, nascetur ridiculus mus. Aenean',TIMESTAMP WITH TIME ZONE '2021-11-1T10:00:00Z',TRUE,11,2),
       ('auctor, nunc nulla vulputate dui, nec tempus mauris erat',TIMESTAMP WITH TIME ZONE '2021-11-1T10:00:00Z',TRUE,13,2),
       ('sem eget massa. Suspendisse eleifend. Cras',TIMESTAMP WITH TIME ZONE '2021-11-1T10:00:00Z',TRUE,3,2),
       ('lobortis quis, pede. Suspendisse dui. Fusce diam nunc, ullamcorper eu,',TIMESTAMP WITH TIME ZONE '2021-11-1T10:00:00Z',TRUE,11,2),
       ('sollicitudin orci sem eget',TIMESTAMP WITH TIME ZONE '2021-11-1T10:00:00Z',TRUE,19,2),
       ('eros turpis',TIMESTAMP WITH TIME ZONE '2021-11-1T10:00:00Z',TRUE,29,2),
       ('metus urna',TIMESTAMP WITH TIME ZONE '2021-11-1T10:00:00Z',TRUE,22,2),
       ('commodo tincidunt nibh.',TIMESTAMP WITH TIME ZONE '2021-11-1T10:00:00Z',FALSE,13,2),
       ('molestie arcu. Sed eu nibh vulputate mauris sagittis placerat.',TIMESTAMP WITH TIME ZONE '2021-11-1T10:00:00Z',FALSE,15,2),
       ('Nunc ullamcorper, velit in aliquet',TIMESTAMP WITH TIME ZONE '2021-11-1T10:00:00Z',FALSE,3,2),
       ('massa. Integer vitae nibh. Donec est mauris,',TIMESTAMP WITH TIME ZONE '2021-11-1T10:00:00Z',TRUE,18,2),
       ('Cras dolor dolor, tempus non, lacinia at, iaculis quis,',TIMESTAMP WITH TIME ZONE '2021-11-1T10:00:00Z',TRUE,11,2),
       ('purus. Maecenas',TIMESTAMP WITH TIME ZONE '2021-11-1T10:00:00Z',TRUE,13,2),
       ('vitae mauris sit amet lorem semper auctor. Mauris',TIMESTAMP WITH TIME ZONE '2021-11-1T10:00:00Z',FALSE,13,2),
       ('mauris, rhoncus id, mollis nec, cursus a, enim.',TIMESTAMP WITH TIME ZONE '2021-11-1T10:00:00Z',TRUE,26,2),
       ('ullamcorper, velit in aliquet lobortis,',TIMESTAMP WITH TIME ZONE '2021-11-1T10:00:00Z',TRUE,29,2),
       ('Donec feugiat',TIMESTAMP WITH TIME ZONE '2021-11-1T10:00:00Z',TRUE,12,2),
       ('torquent per conubia nostra, per inceptos hymenaeos.',TIMESTAMP WITH TIME ZONE '2021-11-1T10:00:00Z',TRUE,4,2),
       ('Duis elementum, dui quis accumsan',TIMESTAMP WITH TIME ZONE '2021-11-1T10:00:00Z',FALSE,2,2),
       ('pharetra, felis eget varius ultrices,',TIMESTAMP WITH TIME ZONE '2021-11-1T10:00:00Z',TRUE,28,2),
       ('molestie arcu. Sed eu',TIMESTAMP WITH TIME ZONE '2021-11-1T10:00:00Z',FALSE,11,2),
       ('in, tempus eu, ligula. Aenean euismod mauris eu',TIMESTAMP WITH TIME ZONE '2021-11-1T10:00:00Z',FALSE,27,2),
       ('dis parturient montes, nascetur ridiculus mus. Proin vel arcu',TIMESTAMP WITH TIME ZONE '2021-11-1T10:00:00Z',FALSE,26,2),
       ('luctus, ipsum leo elementum sem,',TIMESTAMP WITH TIME ZONE '2021-11-1T10:00:00Z',FALSE,4,1);


INSERT INTO  tb_cliente_visitas (cliente_id, visitas_id) VALUES (1, 1);

INSERT INTO tb_user_visitas (user_id, visitas_id) VALUES (2, 1);