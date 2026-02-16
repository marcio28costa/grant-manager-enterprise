# Grant Manager Enterprise 🚀

Sistema de gerenciamento de permissões (Grants) para MySQL 8.0. Este software automatiza o provisionamento de acessos, gerando scripts de segurança baseados em vínculos de usuários, IPs e perfis, garantindo a rastreabilidade através de carimbos de data/hora (`-- #gerado`).



## 🛠️ Tecnologias Utilizadas
* **Java 17+**
* **Swing** (Interface Gráfica)
* **JDBC** (Conexão com Banco de Dados)
* **MySQL 8.0**
* **Maven/IntelliJ** (Gerenciamento de artefatos)

---

## 🗄️ Esquema do Banco de Dados

Para o funcionamento correto do sistema, execute o script abaixo no seu servidor MySQL para criar o banco `DIREITOS` e as tabelas relacionais.



```sql
CREATE DATABASE IF NOT EXISTS DIREITOS;
USE DIREITOS;

-- 1. Tabela de Usuários
CREATE TABLE IF NOT EXISTS usuarios (
    ID INT AUTO_INCREMENT PRIMARY KEY,
    USERNAME VARCHAR(50) NOT NULL UNIQUE,
    PASS_SHA1 VARCHAR(100),
    TIPO ENUM('SISTEMA', 'CONSULTA', 'INTEGRACAO', 'REPLICACAO', 'ROBOS', 'CEP', 'JAVA', 'APP', 'CITEL', 'BI')
) ENGINE=InnoDB;

-- 2. Tabela de Endereços IP / Hosts
CREATE TABLE IF NOT EXISTS enderecosip (
    ID INT AUTO_INCREMENT PRIMARY KEY,
    ENDERECO_IP VARCHAR(50) NOT NULL,
    ENDERECO_GRANT VARCHAR(50) NOT NULL DEFAULT '%'
) ENGINE=InnoDB;

-- 3. Tabela de Permissões (Grants)
CREATE TABLE IF NOT EXISTS permissoes (
    ID INT AUTO_INCREMENT PRIMARY KEY,
    PERMISSAO VARCHAR(100) NOT NULL, -- Ex: 'GRANT SELECT'
    EXTRA VARCHAR(100) DEFAULT '*.*' -- Escopo: banco.tabela
) ENGINE=InnoDB;

-- 4. Tabela de Vínculos (Direito de Acesso)
CREATE TABLE IF NOT EXISTS direito_acesso (
    ID INT AUTO_INCREMENT PRIMARY KEY,
    ID_USER INT NOT NULL,
    ID_IP INT NOT NULL,
    ID_GRANT INT NOT NULL,
    UNIQUE KEY (ID_USER, ID_IP, ID_GRANT),
    FOREIGN KEY (ID_USER) REFERENCES usuarios(ID) ON DELETE CASCADE,
    FOREIGN KEY (ID_IP) REFERENCES enderecosip(ID) ON DELETE CASCADE,
    FOREIGN KEY (ID_GRANT) REFERENCES permissoes(ID) ON DELETE CASCADE
) ENGINE=InnoDB;

-- Carga inicial sugerida
INSERT INTO permissoes (PERMISSAO, EXTRA) VALUES 
('GRANT SELECT', '*.*'),
('GRANT ALL PRIVILEGES', '*.*'),
('GRANT SELECT, INSERT, UPDATE', 'citel.*');
🚀 Como Executar
1. Configuração do Ambiente
O sistema utiliza um arquivo externo para conexão. Crie um arquivo chamado opcoes.app na mesma pasta onde o arquivo .jar será executado:

Properties
db.host=localhost
db.port=3306
db.name=DIREITOS
db.user=seu_usuario
db.password=sua_senha
2. Execução via JAR
Certifique-se de que o artefato foi gerado com a classe principal definida no Manifesto. Execute:

Bash
java -jar grant-manager-enterprise.jar
📋 Funcionalidades
Gestão de Usuários: Cadastro com senha em texto plano (visual) e armazenamento seguro.

Vínculos Dinâmicos: Interface para associar usuários, hosts e níveis de acesso.

Gerador de Script: Lógica via GrantService para criar scripts compatíveis com MySQL 8.0 utilizando IDENTIFIED WITH mysql_native_password.

Sincronização: Botão "Atualizar" no VinculoPanel para carregar dados recém-cadastrados sem reiniciar a aplicação.

Logging: Todo script gerado contém o cabeçalho -- #gerado em [DATA HORA] para controle de versão.

⚠️ Observações Técnicas
Cascata: A exclusão de um registro mestre (Usuário ou IP) remove automaticamente seus vínculos de acesso.

MySQL 8.0: O sistema separa as responsabilidades de criação de conta (CREATE USER) e atribuição de privilégios (GRANT).