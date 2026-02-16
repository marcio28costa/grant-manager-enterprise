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