package service;

import db.ConnectionFactory;
import java.sql.*;
import java.util.*;

public class AuditoriaService {

    public String executarAuditoria(String host, String porta, String user, String pass, String filtroColuna, String valorFiltro) {
        StringBuilder sb = new StringBuilder();
        String urlRemoto = "jdbc:mysql://" + host + ":" + porta + "/mysql?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";

        try (Connection connAudit = DriverManager.getConnection(urlRemoto, user, pass)) {
            String versao = getMySQLVersion(connAudit);
            sb.append("-- SERVIDOR AUDITADO: ").append(host).append(" | VERSAO: ").append(versao).append("\n");

            Map<String, List<RemoteUser>> mapAuditado = carregarUsuariosRemotos(connAudit);
            List<LocalUser> baseGestao = carregarBaseGestao(filtroColuna, valorFiltro);

            sb.append("-- --------------------------------------------------------------\n");
            sb.append("-- SCRIPT DE SINCRONIZACAO (RESILIENTE A FALHAS)\n");
            sb.append("-- --------------------------------------------------------------\n\n");

            for (LocalUser local : baseGestao) {
                RemoteUser melhorMatch = null;
                List<RemoteUser> remotos = mapAuditado.get(local.user);

                if (remotos != null) {
                    for (RemoteUser r : remotos) {
                        if (local.host.equalsIgnoreCase(r.host)) {
                            melhorMatch = r;
                            break;
                        }
                    }
                    if (melhorMatch == null && !isStrictHost(local.host)) {
                        for (RemoteUser r : remotos) {
                            if (hostAbrange(r.host, local.host)) {
                                melhorMatch = r;
                                break;
                            }
                        }
                    }
                }

                if (melhorMatch != null) {
                    if (!local.pass.equalsIgnoreCase(melhorMatch.pass)) {
                        sb.append("-- Ajustando senha: ").append(local.user).append("@").append(melhorMatch.host).append("\n");
                        sb.append(String.format("ALTER USER '%s'@'%s' IDENTIFIED WITH mysql_native_password AS '%s';\n\n",
                                local.user, melhorMatch.host, local.pass));
                    } else {
                        sb.append("-- Aderente: ").append(local.user).append("@").append(local.host).append("\n");
                    }
                } else {
                    sb.append("-- Criando acesso: ").append(local.user).append("@").append(local.host).append("\n");
                    sb.append(String.format("CREATE USER IF NOT EXISTS '%s'@'%s' IDENTIFIED WITH mysql_native_password AS '%s';\n",
                            local.user, local.host, local.pass));
                    sb.append(String.format("ALTER USER '%s'@'%s' IDENTIFIED WITH mysql_native_password AS '%s';\n",
                            local.user, local.host, local.pass));
                    sb.append(String.format("%s '%s'@'%s' %s;\n\n",
                            local.perm.trim(), local.user, local.host, local.extra.trim()));
                }
            }
            sb.append("FLUSH PRIVILEGES;\n-- Fim --");
        } catch (SQLException e) {
            return "-- Erro de Conexao: " + e.getMessage();
        }
        return sb.toString();
    }

    public String aplicarScriptRemoto(String host, String porta, String user, String pass, String script) {
        String urlRemoto = "jdbc:mysql://" + host + ":" + porta + "/mysql?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
        int sucessos = 0;
        List<String> falhas = new ArrayList<>();

        try (Connection conn = DriverManager.getConnection(urlRemoto, user, pass);
             Statement stmt = conn.createStatement()) {

            String[] comandos = script.split(";");
            for (String sql : comandos) {
                String cmd = sql.replaceAll("(?m)^--.*", "").trim();
                if (!cmd.isEmpty()) {
                    try {
                        stmt.execute(cmd);
                        sucessos++;
                    } catch (SQLException e) {
                        // Se falhar, registra o erro e o comando, mas continua o loop
                        falhas.add("Erro no comando [" + cmd + "]: " + e.getMessage());
                    }
                }
            }

            StringBuilder resultado = new StringBuilder();
            resultado.append("Processamento concluido!\n");
            resultado.append("✅ Sucessos: ").append(sucessos).append("\n");

            if (!falhas.isEmpty()) {
                resultado.append("❌ Falhas: ").append(falhas.size()).append("\n\n--- Detalhes das Falhas ---\n");
                for (String f : falhas) {
                    resultado.append(f).append("\n");
                }
            } else {
                resultado.append("✨ Todos os comandos foram aplicados sem erros.");
            }

            return resultado.toString();

        } catch (SQLException e) {
            return "Erro fatal de conexao: " + e.getMessage();
        }
    }

    private boolean hostAbrange(String hostRemoto, String hostLocal) {
        if (hostRemoto.equals("%")) return true;
        try {
            String regex = "^" + hostRemoto.replace(".", "\\.").replace("%", ".*") + "$";
            return hostLocal.matches(regex);
        } catch (Exception e) { return false; }
    }

    private boolean isStrictHost(String host) {
        return host.equalsIgnoreCase("localhost") || host.equals("127.0.0.1");
    }

    private Map<String, List<RemoteUser>> carregarUsuariosRemotos(Connection conn) throws SQLException {
        Map<String, List<RemoteUser>> map = new HashMap<>();
        String sql = "SELECT user, host, authentication_string FROM mysql.user";
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                String u = rs.getString("user");
                map.computeIfAbsent(u, k -> new ArrayList<>())
                        .add(new RemoteUser(rs.getString("host"), rs.getString("authentication_string")));
            }
        }
        return map;
    }

    private List<LocalUser> carregarBaseGestao(String filtroColuna, String valorFiltro) {
        List<LocalUser> lista = new ArrayList<>();
        String sql = "SELECT u.USERNAME, u.PASS_SHA1, i.ENDERECO_GRANT, p.PERMISSAO, p.EXTRA " +
                "FROM direito_acesso da " +
                "JOIN usuarios u ON da.ID_USER = u.ID " +
                "JOIN enderecosip i ON da.ID_IP = i.ID " +
                "JOIN permissoes p ON da.ID_GRANT = p.ID";
        if (filtroColuna != null && valorFiltro != null) sql += " WHERE " + filtroColuna + " = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            if (filtroColuna != null && valorFiltro != null) stmt.setString(1, valorFiltro);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(new LocalUser(rs.getString("USERNAME"), rs.getString("ENDERECO_GRANT"),
                            rs.getString("PASS_SHA1"), rs.getString("PERMISSAO"), rs.getString("EXTRA")));
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return lista;
    }

    private String getMySQLVersion(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery("SELECT VERSION()")) {
            return rs.next() ? rs.getString(1) : "N/A";
        }
    }

    private static class LocalUser {
        String user, host, pass, perm, extra;
        LocalUser(String u, String h, String p, String pr, String ex) {
            this.user = u; this.host = h; this.pass = p; this.perm = pr; this.extra = ex;
        }
    }

    private static class RemoteUser {
        String host, pass;
        RemoteUser(String h, String p) { this.host = h; this.pass = p; }
    }
}