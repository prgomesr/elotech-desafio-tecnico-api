CREATE TABLE IF NOT EXISTS `pessoa` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `nome` VARCHAR(255) NOT NULL,
  `cpf` VARCHAR(20) NOT NULL,
  `data_nascimento` DATE NOT NULL,
  `data_hora_cadastro` DATETIME NOT NULL,
  `data_hora_atualizacao` DATETIME NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `id_UNIQUE` (`id` ASC))
ENGINE = InnoDB default charset=utf8;


CREATE TABLE IF NOT EXISTS `contato` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `pessoa_id` BIGINT UNSIGNED NOT NULL,
  `nome` VARCHAR(255) NOT NULL,
  `telefone` VARCHAR(20) NOT NULL,
  `email` VARCHAR(100) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `id_UNIQUE` (`id` ASC),
  INDEX `fk_contato_pessoa_idx` (`pessoa_id` ASC),
  CONSTRAINT `fk_contato_pessoa`
    FOREIGN KEY (`pessoa_id`)
    REFERENCES `pessoa` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB default charset=utf8;