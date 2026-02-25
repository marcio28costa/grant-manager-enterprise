package service;

import db.ConnectionFactory;
import java.sql.*;
import java.util.*;

public class AuditoriaService {

    public String executarAuditoria(String host, String porta, String user, String pass,
                                    String filtroTipo, String valorFiltro,
                                    List<String> ignorar, List<String> dropar) {

        StringBuilder sb = new StringBuilder();
        String urlRemoto = "jdbc:mysql://" + host + ":" + porta + "/mysql?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";

        String colunaSQL = null;
        if (filtroTipo != null) {
            if (filtroTipo.equals("POR USUARIO")) colunaSQL = "u.USERNAME";
            else if (filtroTipo.equals("POR ENDERECO IP")) colunaSQL = "i.ENDERECO_GRANT";
            else if (filtroTipo.equals("POR TIPO")) colunaSQL = "i.TIPO";
        }

        try (Connection connAudit = DriverManager.getConnection(urlRemoto, user, pass)) {
            sb.append("-- SCRIPT GERADO PARA O SERVIDOR: ").append(host).append("\n");

            // --- FASE 1: DROP DE USUÁRIOS (CONSULTANDO mysql.user REMOTO) ---
            if (!dropar.isEmpty()) {
                sb.append("\n-- --------------------------------------------------------------\n");
                sb.append("-- FASE 1: LIMPEZA (DROP USERS ENCONTRADOS NO REMOTO)\n");
                sb.append("-- --------------------------------------------------------------\n");

                // Monta o IN (?, ?, ?) para a query
                String placeholders = String.join(",", Collections.nCopies(dropar.size(), "?"));
                String sqlDrop = "SELECT user, host FROM mysql.user WHERE user IN (" + placeholders + ") " +
                        "AND user NOT IN ('root', 'dbacitel')";

                try (PreparedStatement ps = connAudit.prepareStatement(sqlDrop)) {
                    for (int i = 0; i < dropar.size(); i++) {
                        ps.setString(i + 1, dropar.get(i));
                    }

                    try (ResultSet rs = ps.executeQuery()) {
                        boolean encontrou = false;
                        while (rs.next()) {
                            encontrou = true;
                            sb.append(String.format("DROP USER '%s'@'%s';\n", rs.getString("user"), rs.getString("host")));
                        }
                        if (!encontrou) sb.append("-- Nenhum usuario da lista de DROP encontrado no servidor remoto.\n");
                    }
                }
                sb.append("FLUSH PRIVILEGES;\n");
            }

            // --- FASE 2: AUDITORIA / SINCRONIZACAO ---
            sb.append("\n-- --------------------------------------------------------------\n");
            sb.append("-- FASE 2: SINCRONIZACAO (IGNORANDO SELECIONADOS)\n");
            sb.append("-- --------------------------------------------------------------\n");

            Map<String, List<RemoteUser>> mapRemoto = carregarUsuariosRemotos(connAudit);
            List<LocalUser> baseLocal = carregarBaseGestao(colunaSQL, valorFiltro);

            for (LocalUser local : baseLocal) {
                // Pula se estiver na lista de ignorar ou se acabou de ser dropado
                if (ignorar.stream().anyMatch(u -> u.equalsIgnoreCase(local.user)) ||
                        dropar.stream().anyMatch(u -> u.equalsIgnoreCase(local.user))) {
                    continue;
                }

                RemoteUser match = null;
                List<RemoteUser> remotos = mapRemoto.get(local.user);
                if (remotos != null) {
                    for (RemoteUser r : remotos) {
                        if (local.host.equalsIgnoreCase(r.host)) { match = r; break; }
                    }
                }

                if (match != null) {
                    if (!local.pass.equalsIgnoreCase(match.pass)) {
                        sb.append(String.format("ALTER USER '%s'@'%s' IDENTIFIED WITH mysql_native_password AS '%s';\n", local.user, match.host, local.pass));
                    }
                } else {
                    sb.append(String.format("CREATE USER IF NOT EXISTS '%s'@'%s' IDENTIFIED WITH mysql_native_password AS '%s';\n", local.user, local.host, local.pass));
                    sb.append(String.format("ALTER USER '%s'@'%s' IDENTIFIED WITH mysql_native_password AS '%s';\n", local.user, local.host, local.pass));
                    sb.append(String.format("%s '%s'@'%s' %s;\n", local.perm.trim(), local.user, local.host, local.extra.trim()));
                }
            }
            sb.append("\nFLUSH PRIVILEGES;\n-- FIM --");

        } catch (SQLException e) {
            return "-- Erro de Conexao Remota: " + e.getMessage();
        }
        return sb.toString();
    }

    // [Os demais métodos carregarUsuariosRemotos, carregarBaseGestao e aplicarScript permanecem os mesmos]
    private Map<String, List<RemoteUser>> carregarUsuariosRemotos(Connection conn) throws SQLException {
        Map<String, List<RemoteUser>> map = new HashMap<>();
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery("SELECT user, host, authentication_string FROM mysql.user")) {
            while (rs.next()) {
                String u = rs.getString("user");
                map.computeIfAbsent(u, k -> new ArrayList<>()).add(new RemoteUser(rs.getString("host"), rs.getString("authentication_string")));
            }
        }
        return map;
    }

    private List<LocalUser> carregarBaseGestao(String colunaSQL, String valorFiltro) {
        List<LocalUser> lista = new ArrayList<>();
        String sql = "SELECT u.USERNAME, u.PASS_SHA1, i.ENDERECO_GRANT, p.PERMISSAO, p.EXTRA " +
                "FROM direito_acesso da JOIN usuarios u ON da.ID_USER = u.ID " +
                "JOIN enderecosip i ON da.ID_IP = i.ID JOIN permissoes p ON da.ID_GRANT = p.ID";
        if (colunaSQL != null) sql += " WHERE " + colunaSQL + " = ?";
        try (Connection c = ConnectionFactory.getConnection(); PreparedStatement p = c.prepareStatement(sql)) {
            if (colunaSQL != null) p.setString(1, valorFiltro);
            ResultSet rs = p.executeQuery();
            while (rs.next()) {
                lista.add(new LocalUser(rs.getString(1), rs.getString(3), rs.getString(2), rs.getString(4), rs.getString(5)));
            }
        } catch (Exception e) { e.printStackTrace(); }
        return lista;
    }

    public String aplicarScriptRemoto(String host, String porta, String user, String pass, String script) {
        String url = "jdbc:mysql://" + host + ":" + porta + "/mysql?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
        try (Connection conn = DriverManager.getConnection(url, user, pass); Statement stmt = conn.createStatement()) {
            for (String cmd : script.split(";")) {
                if (!cmd.trim().isEmpty() && !cmd.trim().startsWith("--")) stmt.execute(cmd.trim());
            }
            return "Script aplicado com sucesso!";
        } catch (Exception e) { return "Erro: " + e.getMessage(); }
    }

    private static class LocalUser { String user, host, pass, perm, extra; LocalUser(String u, String h, String p, String pr, String ex) { this.user = u; this.host = h; this.pass = p; this.perm = pr; this.extra = ex; } }
    private static class RemoteUser { String host, pass; RemoteUser(String h, String p) { this.host = h; this.pass = (p != null) ? p : ""; } }
}