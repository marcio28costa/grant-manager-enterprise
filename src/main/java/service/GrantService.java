/*
package service;

import db.ConnectionFactory;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class GrantService {

    public String gerarScriptGeral() {
        StringBuilder script = new StringBuilder();

        String sql =
        "SELECT DISTINCT u.USERNAME, u.TIPO, e.ENDERECO_GRANT, u.PASS_SHA1, " +
        "p.PERMISSAO, p.EXTRA " +
        "FROM direito_acesso d " +
        "JOIN usuarios u ON d.ID_USER = u.ID " +
        "JOIN enderecosip e ON d.ID_IP = e.ID " +
        "JOIN permissoes p ON d.ID_GRANT = p.ID " +
        "ORDER BY u.TIPO, u.USERNAME, e.ENDERECO_GRANT, p.PERMISSAO";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            String usuarioAtual = "";
            String hostAtual = "";

            while (rs.next()) {
                String username = rs.getString("USERNAME");
                String tipo = rs.getString("TIPO");
                String host = rs.getString("ENDERECO_GRANT");
                String sha1 = rs.getString("PASS_SHA1");
                String permissao = rs.getString("PERMISSAO");
                String extra = rs.getString("EXTRA");

                if (!username.equals(usuarioAtual) || !host.equals(hostAtual)) {

                    script.append("\n-- USER: ")
                          .append(username)
                          .append("@")
                          .append(host)
                          .append(" ;\n");

                    script.append("CREATE USER IF NOT EXISTS '")
                          .append(username).append("'@'")
                          .append(host)
                          .append("' IDENTIFIED WITH mysql_native_password AS '")
                          .append(sha1).append("';\n");

                    script.append("ALTER USER '")
                          .append(username).append("'@'")
                          .append(host)
                          .append("' IDENTIFIED WITH mysql_native_password AS '")
                          .append(sha1).append("';\n");
                }

                script.append(permissao)
                      .append(" '")
                      .append(username)
                      .append("'@'")
                      .append(host)
                      .append("' ");

                if (extra != null && !extra.trim().isEmpty()) {
                    script.append(extra);
                }

                script.append(";\n");

                usuarioAtual = username;
                hostAtual = host;
            }

        } catch (Exception e) {
            return "Erro ao gerar script: " + e.getMessage();
        }

        return script.toString();
    }
}
*/
/*
package service;

import db.ConnectionFactory;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class GrantService {

    public String gerarScriptGeral() {
        StringBuilder script = new StringBuilder();

        // --- Adicionando o cabeçalho com data e hora ---
        LocalDateTime agora = LocalDateTime.now();
        DateTimeFormatter formatador = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

        script.append("-- #gerado em ").append(agora.format(formatador)).append("\n");
        script.append("-- Provisionamento Enterprise - MySQL 8.0 -- \n");
        script.append("-- marcio28costa@hotmail.com -- \n");
        script.append("-- ---------------------------------------------- --\n");

        String sql =
                "SELECT DISTINCT u.USERNAME, u.TIPO, e.ENDERECO_GRANT, u.PASS_SHA1, " +
                        "p.PERMISSAO, p.EXTRA " +
                        "FROM direito_acesso d " +
                        "JOIN usuarios u ON d.ID_USER = u.ID " +
                        "JOIN enderecosip e ON d.ID_IP = e.ID " +
                        "JOIN permissoes p ON d.ID_GRANT = p.ID " +
                        "ORDER BY u.TIPO, u.USERNAME, e.ENDERECO_GRANT, p.PERMISSAO";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            String usuarioAtual = "";
            String hostAtual = "";

            while (rs.next()) {
                String username = rs.getString("USERNAME");
                String host = rs.getString("ENDERECO_GRANT");
                String sha1 = rs.getString("PASS_SHA1");
                String permissao = rs.getString("PERMISSAO");
                String extra = rs.getString("EXTRA");

                // Agrupamento por Usuário e Host para não repetir CREATE/ALTER
                if (!username.equals(usuarioAtual) || !host.equals(hostAtual)) {

                    script.append("\n-- USER: ")
                            .append(username)
                            .append("@")
                            .append(host)
                            .append(" ;\n");

                    script.append("CREATE USER IF NOT EXISTS '")
                            .append(username).append("'@'")
                            .append(host)
                            .append("' IDENTIFIED WITH mysql_native_password AS '")
                            .append(sha1).append("';\n");

                    script.append("ALTER USER '")
                            .append(username).append("'@'")
                            .append(host)
                            .append("' IDENTIFIED WITH mysql_native_password AS '")
                            .append(sha1).append("';\n");
                }

                // Montagem da instrução de permissão (GRANT)
                // Nota: Verifique se no seu banco o campo PERMISSAO já contém a palavra "GRANT"
                script.append(permissao)
                        .append(" ON ") // Adicionado " ON " que é padrão SQL se não estiver no banco
                        .append(extra != null && !extra.trim().isEmpty() ? extra : "*.*")
                        .append(" TO '")
                        .append(username)
                        .append("'@")
                        .append("'")
                        .append(host)
                        .append("';\n");

                usuarioAtual = username;
                hostAtual = host;
            }

            script.append("\nFLUSH PRIVILEGES;\n");

        } catch (Exception e) {
            return "Erro ao gerar script: " + e.getMessage();
        }

        return script.toString();
    }
}
 */
/*
package service;

import db.ConnectionFactory;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Set;

public class GrantService {

    public String gerarScript(String filtroColuna, String valorFiltro) {
        StringBuilder sb = new StringBuilder();

        String data = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
        sb.append("-- ############################################################\n");
        sb.append("-- # Script de Provisionamento Gerado em: ").append(data).append("\n");
        sb.append("-- ############################################################\n\n");

        // Usamos DISTINCT no SQL e um Set no Java para garantir unicidade absoluta
        String sql = "SELECT DISTINCT u.USERNAME, u.PASS_SHA1, i.ENDERECO_GRANT, p.PERMISSAO, p.EXTRA " +
                "FROM direito_acesso da " +
                "JOIN usuarios u ON da.ID_USER = u.ID " +
                "JOIN enderecosip i ON da.ID_IP = i.ID " +
                "JOIN permissoes p ON da.ID_GRANT = p.ID ";

        if (filtroColuna != null && valorFiltro != null) {
            sql += " WHERE " + filtroColuna + " = ? ";
        }

        // Ordenar é fundamental para o agrupamento visual no script
        sql += " ORDER BY u.USERNAME, i.ENDERECO_GRANT";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            if (filtroColuna != null && valorFiltro != null) {
                stmt.setString(1, valorFiltro);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                // Set para controlar quais usuários@hosts já foram "declarados" com CREATE USER
                Set<String> usuariosDeclarados = new HashSet<>();
                boolean temRegistros = false;

                while (rs.next()) {
                    temRegistros = true;
                    String user = rs.getString("USERNAME");
                    String host = rs.getString("ENDERECO_GRANT");
                    String pass = rs.getString("PASS_SHA1");
                    String perm = rs.getString("PERMISSAO").trim();
                    String extra = rs.getString("EXTRA").trim();

                    String chaveUsuario = user + "@" + host;

                    // Se for a primeira vez que vemos esse par usuario@host, gera o CREATE USER
                    if (!usuariosDeclarados.contains(chaveUsuario)) {
                        sb.append("\n-- ##########################################\n");
                        sb.append("--  ").append(chaveUsuario).append("\n");
                        sb.append(String.format("CREATE USER IF NOT EXISTS '%s'@'%s' IDENTIFIED WITH mysql_native_password AS '%s';\n",
                                user, host, pass));
                        usuariosDeclarados.add(chaveUsuario);
                    }

                    // Gera o GRANT (este pode se repetir com permissões diferentes, o que está correto)
                    sb.append(String.format("%s '%s'@'%s' %s;\n",
                            perm, user, host, extra));
                }

                if (!temRegistros) {
                    sb.append("-- Nenhum vínculo encontrado.\n");
                } else {
                    sb.append("\nFLUSH PRIVILEGES;\n");
                }
            }

        } catch (SQLException e) {
            sb.append("-- # ERRO: ").append(e.getMessage());
        }

        return sb.toString();
    }
}

*/
/*
package service;

import db.ConnectionFactory;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class GrantService {

    public String gerarScript(String filtroColuna, String valorFiltro) {
        StringBuilder sb = new StringBuilder();
        String dataHora = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date());

        // Cabeçalho limpo sem cerquilhas (#)
        sb.append("-- ------------------------------------------------------------\n");
        sb.append("-- Script de Provisionamento Gerado em: ").append(dataHora).append("\n");
        sb.append("-- ------------------------------------------------------------\n\n");

        String sql = "SELECT u.USERNAME, u.PASS_SHA1, i.ENDERECO_GRANT, p.PERMISSAO, p.EXTRA " +
                "FROM direito_acesso da " +
                "JOIN usuarios u ON da.ID_USER = u.ID " +
                "JOIN enderecosip i ON da.ID_IP = i.ID " +
                "JOIN permissoes p ON da.ID_GRANT = p.ID";

        if (filtroColuna != null && valorFiltro != null) {
            sql += " WHERE " + filtroColuna + " = ?";
        }

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            if (filtroColuna != null && valorFiltro != null) {
                stmt.setString(1, valorFiltro);
            }

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String user = rs.getString("USERNAME");
                String host = rs.getString("ENDERECO_GRANT");
                String pass = rs.getString("PASS_SHA1");
                String perm = rs.getString("PERMISSAO");
                String extra = rs.getString("EXTRA");

                // Bloco de Provisionamento por usuário/host
                sb.append("-- Acesso: ").append(user).append("@").append(host).append("\n");

                // 1. Cria se não existir
                sb.append(String.format("CREATE USER IF NOT EXISTS '%s'@'%s' IDENTIFIED WITH mysql_native_password AS '%s';\n",
                        user, host, pass));

                // 2. Aplica o ALTER USER com a mesma senha (conforme solicitado)
                sb.append(String.format("ALTER USER '%s'@'%s' IDENTIFIED WITH mysql_native_password AS '%s';\n",
                        user, host, pass));

                // 3. Aplica a Permissão (GRANT)
                sb.append(String.format("%s '%s'@'%s' %s;\n\n",
                        perm.trim(), user, host, extra.trim()));
            }

            sb.append("FLUSH PRIVILEGES;\n-- Fim do Script");

        } catch (SQLException e) {
            sb.append("-- Erro ao gerar script: ").append(e.getMessage());
        }

        return sb.toString();
    }
}

 */

package service;

import db.ConnectionFactory;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class GrantService {

    public String gerarScript(String filtroColuna, String valorFiltro) {
        StringBuilder sb = new StringBuilder();
        String dataHora = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date());

        sb.append("-- ------------------------------------------------------------\n");
        sb.append("-- Script de Provisionamento Gerado em: ").append(dataHora).append("\n");
        sb.append("-- ------------------------------------------------------------\n\n");

        // Ordenar por Usuário e Host é essencial para a lógica de agrupamento funcionar
        String sql = "SELECT u.USERNAME, u.PASS_SHA1, i.ENDERECO_GRANT, p.PERMISSAO, p.EXTRA " +
                "FROM direito_acesso da " +
                "JOIN usuarios u ON da.ID_USER = u.ID " +
                "JOIN enderecosip i ON da.ID_IP = i.ID " +
                "JOIN permissoes p ON da.ID_GRANT = p.ID";

        if (filtroColuna != null && valorFiltro != null) {
            sql += " WHERE " + filtroColuna + " = ?";
        }

        sql += " ORDER BY u.USERNAME, i.ENDERECO_GRANT";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            if (filtroColuna != null && valorFiltro != null) {
                stmt.setString(1, valorFiltro);
            }

            ResultSet rs = stmt.executeQuery();

            String ultimoUsuarioHost = ""; // Variável de controle para agrupamento

            while (rs.next()) {
                String user = rs.getString("USERNAME");
                String host = rs.getString("ENDERECO_GRANT");
                String pass = rs.getString("PASS_SHA1");
                String perm = rs.getString("PERMISSAO");
                String extra = rs.getString("EXTRA");

                String usuarioHostAtual = user + "@" + host;

                // Se mudou o Usuário ou o Host, cria o cabeçalho e os comandos de USER
                if (!usuarioHostAtual.equals(ultimoUsuarioHost)) {
                    if (!ultimoUsuarioHost.equals("")) sb.append("\n"); // Espaço entre blocos de usuários diferentes

                    sb.append("-- Acesso: ").append(user).append("@").append(host).append("\n");

                    // CREATE + ALTER (Executados apenas UMA vez por par usuário/host)
                    sb.append(String.format("CREATE USER IF NOT EXISTS '%s'@'%s' IDENTIFIED WITH mysql_native_password AS '%s';\n",
                            user, host, pass));

                    sb.append(String.format("ALTER USER '%s'@'%s' IDENTIFIED WITH mysql_native_password AS '%s';\n",
                            user, host, pass));

                    ultimoUsuarioHost = usuarioHostAtual;
                }

                // O GRANT sempre será gerado, acumulando se houver mais de um para o mesmo usuário
                sb.append(String.format("%s '%s'@'%s' %s;\n",
                        perm.trim(), user, host, extra.trim()));
            }

            sb.append("\nFLUSH PRIVILEGES;\n-- Fim do Script");

        } catch (SQLException e) {
            sb.append("-- Erro ao gerar script: ").append(e.getMessage());
        }

        return sb.toString();
    }
}