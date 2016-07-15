insert into email_killer_user (id, p_email_address, hashed_password, salt) values (1, 'm.meyer@telaside.com', 'titi', 'toto');
insert into email_account
(valid, acc_type, acc_uid, id, acc_login, acc_password, acc_remote_server, user_id)
VALUES
('T', 'POP3', '93C33401-91A7-4DF4-9561-EAC45D10EB11', 1, 'm.meyer@telaside.com', 'ju4m86p2', 'mail.nfrance.com', 1);
insert into email_account
(valid, acc_type, acc_uid, id, acc_login, acc_password, acc_remote_server, user_id)
VALUES
('T', 'POP3', 'A3F61819-65F7-4035-9C93-5EF3C6C9026D', 2, 'info@telaside.com', 'oytkrm3q', 'mail.nfrance.com', 1);
