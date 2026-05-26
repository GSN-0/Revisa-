CREATE TABLE users (
    id BIGINT NOT NULL AUTO_INCREMENT,
    nome VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    senha VARCHAR(255) NOT NULL,
    role VARCHAR(255) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_users_email UNIQUE (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE materias (
    id BIGINT NOT NULL AUTO_INCREMENT,
    nome VARCHAR(100) NOT NULL,
    descricao VARCHAR(500),
    cor VARCHAR(20),
    user_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_materias_users
        FOREIGN KEY (user_id)
        REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE conteudos_estudados (
    id BIGINT NOT NULL AUTO_INCREMENT,
    titulo VARCHAR(150) NOT NULL,
    descricao VARCHAR(1000),
    data_estudo DATE NOT NULL,
    materia_id BIGINT NOT NULL,
    proxima_revisao DATE NOT NULL,
    quantidade_revisoes INT NOT NULL DEFAULT 0,
    nivel_dominio INT NOT NULL DEFAULT 1,
    observacao_evolucao VARCHAR(1000),
    concluido BIT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    CONSTRAINT fk_conteudos_estudados_materias
        FOREIGN KEY (materia_id)
        REFERENCES materias (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_materias_user_id ON materias (user_id);
CREATE INDEX idx_conteudos_materia_id ON conteudos_estudados (materia_id);
CREATE INDEX idx_conteudos_proxima_revisao ON conteudos_estudados (proxima_revisao);
CREATE INDEX idx_conteudos_nivel_dominio ON conteudos_estudados (nivel_dominio);
