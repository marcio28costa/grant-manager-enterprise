package ui;

import db.ConnectionFactory;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ProcessaUsuariosPanel extends JPanel {
    private JTextField txtHostCentral, txtPortaCentral, txtUserCentral, txtUserAuditoria, txtPesquisa;
    private JPasswordField txtPassCentral, txtPassAuditoria;
    private JTextArea txtPreComandosArea, areaStatus;
    private JCheckBox chkPesquisaContida, chkSqlLogBin;
    private JTable tabelaLojas;
    private DefaultTableModel model;
    private JProgressBar barraProgresso;
    private JButton btnConectarFonte, btnPesquisar, btnIniciar;
    private JComboBox<String> cbFiltroAuditoria, cbOpcaoAuditoria;

    public ProcessaUsuariosPanel() {
        setLayout(new BorderLayout(5, 5));

        // --- UI: CONFIGURAÇÕES (NORTE) ---
        JPanel pnlNorte = new JPanel();
        pnlNorte.setLayout(new BoxLayout(pnlNorte, BoxLayout.Y_AXIS));

        // 1. Fonte de Dados
        JPanel pnlFonte = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pnlFonte.setBorder(BorderFactory.createTitledBorder("1. Banco Fonte (Lista de Lojas)"));
        txtHostCentral = new JTextField("localhost", 10);
        txtPortaCentral = new JTextField("3306", 4);
        txtUserCentral = new JTextField("root", 8);
        txtPassCentral = new JPasswordField(8);
        btnConectarFonte = new JButton("Conectar Fonte");
        pnlFonte.add(new JLabel("Host:")); pnlFonte.add(txtHostCentral);
        pnlFonte.add(new JLabel("User:")); pnlFonte.add(txtUserCentral);
        pnlFonte.add(new JLabel("Pass:")); pnlFonte.add(txtPassCentral);
        pnlFonte.add(btnConectarFonte);

        // 2. Pré-Ação e Filtro
        JPanel pnlPesquisa = new JPanel(new FlowLayout(FlowLayout.LEFT));
        txtPesquisa = new JTextField(12);
        chkPesquisaContida = new JCheckBox("LIKE %%");
        btnPesquisar = new JButton("Filtrar Grid");
        pnlPesquisa.add(new JLabel("Busca Loja/Empresa:")); pnlPesquisa.add(txtPesquisa);
        pnlPesquisa.add(chkPesquisaContida); pnlPesquisa.add(btnPesquisar);

        JPanel pnlPre = new JPanel(new BorderLayout());
        pnlPre.setBorder(BorderFactory.createTitledBorder("2. Comandos Pré-Auditoria (Aceita DELIMITER)"));
        chkSqlLogBin = new JCheckBox("SET sql_log_bin=0;");
        txtPreComandosArea = new JTextArea(5, 50);
        txtPreComandosArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        pnlPre.add(chkSqlLogBin, BorderLayout.NORTH);
        pnlPre.add(new JScrollPane(txtPreComandosArea), BorderLayout.CENTER);

        // 3. Regras de Auditoria
        JPanel pnlAudit = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pnlAudit.setBorder(BorderFactory.createTitledBorder("3. Regra de Comparação"));
        txtUserAuditoria = new JTextField("root", 6);
        txtPassAuditoria = new JPasswordField(6);
        cbFiltroAuditoria = new JComboBox<>(new String[]{"GERAL", "POR USUARIO", "POR ENDERECO IP"});
        cbOpcaoAuditoria = new JComboBox<>();
        cbOpcaoAuditoria.setPreferredSize(new Dimension(150, 25));
        pnlAudit.add(new JLabel("Login Loja:")); pnlAudit.add(txtUserAuditoria); pnlAudit.add(txtPassAuditoria);
        pnlAudit.add(cbFiltroAuditoria); pnlAudit.add(cbOpcaoAuditoria);

        pnlNorte.add(pnlFonte); pnlNorte.add(pnlPesquisa); pnlNorte.add(pnlPre); pnlNorte.add(pnlAudit);
        add(pnlNorte, BorderLayout.NORTH);

        // --- GRID CENTRAL ---
        model = new DefaultTableModel(new String[]{"Sel", "Empresa", "Loja", "IP", "Porta", "CNPJ"}, 0) {
            @Override public Class<?> getColumnClass(int c) { return c == 0 ? Boolean.class : String.class; }
        };
        tabelaLojas = new JTable(model);
        add(new JScrollPane(tabelaLojas), BorderLayout.CENTER);

        // --- STATUS (SUL) ---
        JPanel pnlSul = new JPanel(new BorderLayout());
        areaStatus = new JTextArea(8, 20);
        barraProgresso = new JProgressBar();
        barraProgresso.setStringPainted(true);
        btnIniciar = new JButton("EXECUTAR PROCESSAMENTO MULTI-THREAD");
        btnIniciar.setBackground(new Color(0, 102, 51));
        btnIniciar.setForeground(Color.WHITE);
        pnlSul.add(new JScrollPane(areaStatus), BorderLayout.CENTER);
        pnlSul.add(barraProgresso, BorderLayout.NORTH);
        pnlSul.add(btnIniciar, BorderLayout.SOUTH);
        add(pnlSul, BorderLayout.SOUTH);

        configurarEventos();
    }

    private void configurarEventos() {
        btnConectarFonte.addActionListener(e -> listarLojas());
        btnPesquisar.addActionListener(e -> listarLojas());
        cbFiltroAuditoria.addActionListener(e -> carregarFiltrosLocais());
        btnIniciar.addActionListener(e -> dispararThreads());
    }

    private void listarLojas() {
        model.setRowCount(0);
        String url = "jdbc:mysql://" + txtHostCentral.getText() + ":" + txtPortaCentral.getText() + "/diariomsql?useSSL=false";
        String sql = "SELECT E.EMP_NOMEMP, L.LOJ_NOMLOJ, L.LOJ_NUM_IP, L.LOJ_PORYOG, L.LOJ_C_G_C_ " +
                "FROM diariomsql.EMPLOJ L JOIN diariomsql.CADEMP E ON L.LOJ_CODEMP = E.EMP_CODEMP";
        String busca = txtPesquisa.getText().trim();
        if (!busca.isEmpty()) {
            String op = chkPesquisaContida.isSelected() ? " LIKE '%" + busca + "%'" : " = '" + busca + "'";
            sql += " WHERE E.EMP_NOMEMP " + op + " OR L.LOJ_NOMLOJ " + op;
        }
        try (Connection conn = DriverManager.getConnection(url, txtUserCentral.getText(), new String(txtPassCentral.getPassword()));
             Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) model.addRow(new Object[]{false, rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5)});
        } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Erro Fonte: " + ex.getMessage()); }
    }

    private void carregarFiltrosLocais() {
        cbOpcaoAuditoria.removeAllItems();
        if (cbFiltroAuditoria.getSelectedIndex() == 0) return;
        String sql = cbFiltroAuditoria.getSelectedIndex() == 1 ? "SELECT DISTINCT USERNAME FROM usuarios" : "SELECT DISTINCT ENDERECO_GRANT FROM enderecosip";
        try (Connection c = ConnectionFactory.getConnection(); Statement s = c.createStatement(); ResultSet r = s.executeQuery(sql)) {
            while (r.next()) cbOpcaoAuditoria.addItem(r.getString(1));
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void dispararThreads() {
        List<Integer> selecionados = new ArrayList<>();
        for (int i = 0; i < model.getRowCount(); i++) if ((Boolean) model.getValueAt(i, 0)) selecionados.add(i);
        if (selecionados.isEmpty()) { JOptionPane.showMessageDialog(this, "Selecione lojas no grid!"); return; }

        new Thread(() -> {
            btnIniciar.setEnabled(false);
            barraProgresso.setMaximum(selecionados.size());
            barraProgresso.setValue(0);

            // GARANTE QUE A TABELA DE LOG EXISTE ANTES DE COMEÇAR
            verificarCriarTabelaLog();

            ExecutorService executor = Executors.newFixedThreadPool(5);
            String col = cbFiltroAuditoria.getSelectedIndex() == 1 ? "u.USERNAME" : (cbFiltroAuditoria.getSelectedIndex() == 2 ? "i.ENDERECO_GRANT" : null);
            String val = (String) cbOpcaoAuditoria.getSelectedItem();

            for (int rowIdx : selecionados) {
                String ip = (String) model.getValueAt(rowIdx, 3);
                String porta = (String) model.getValueAt(rowIdx, 4);
                String nome = (String) model.getValueAt(rowIdx, 2);
                String cnpj = (String) model.getValueAt(rowIdx, 5);

                executor.execute(() -> {
                    processarHost(ip, porta, nome, cnpj, col, val);
                    SwingUtilities.invokeLater(() -> barraProgresso.setValue(barraProgresso.getValue() + 1));
                });
            }
            executor.shutdown();
            try { executor.awaitTermination(1, TimeUnit.HOURS); } catch (Exception e) {}
            SwingUtilities.invokeLater(() -> { btnIniciar.setEnabled(true); areaStatus.append("### PROCESSAMENTO FINALIZADO ###\n"); });
        }).start();
    }

    private void processarHost(String ip, String porta, String nome, String cnpj, String col, String val) {
        String url = "jdbc:mysql://" + ip + ":" + porta + "/mysql?useSSL=false&allowPublicKeyRetrieval=true";
        try (Connection conn = DriverManager.getConnection(url, txtUserAuditoria.getText(), new String(txtPassAuditoria.getPassword()))) {
            log("-> Host: " + nome + " (" + ip + ")");

            if (chkSqlLogBin.isSelected()) {
                try (Statement s = conn.createStatement()) { s.execute("SET sql_log_bin=0"); }
            }

            String resPre = aplicarScriptResiliente(conn, txtPreComandosArea.getText());
            String scriptAcerto = gerarScriptAuditoria(conn, col, val);
            String resAcerto = aplicarScriptResiliente(conn, scriptAcerto);

            gravarLogLocal(cnpj, nome, "SUCESSO", "PRE: " + resPre + " | ACERTO: " + resAcerto);
            log("✅ OK: " + nome);
        } catch (Exception e) {
            log("❌ ERRO: " + nome + " (" + e.getMessage() + ")");
            gravarLogLocal(cnpj, nome, "FALHA", e.getMessage());
        }
    }

    private String gerarScriptAuditoria(Connection conn, String col, String val) throws SQLException {
        StringBuilder sb = new StringBuilder();
        Map<String, List<String[]>> remotos = new HashMap<>();
        try (Statement s = conn.createStatement(); ResultSet r = s.executeQuery("SELECT user, host, authentication_string FROM mysql.user")) {
            while (r.next()) remotos.computeIfAbsent(r.getString(1), k -> new ArrayList<>()).add(new String[]{r.getString(2), r.getString(3)});
        }
        String sql = "SELECT u.USERNAME, u.PASS_SHA1, i.ENDERECO_GRANT, p.PERMISSAO, p.EXTRA FROM direito_acesso da " +
                "JOIN usuarios u ON da.ID_USER = u.ID JOIN enderecosip i ON da.ID_IP = i.ID " +
                "JOIN permissoes p ON da.ID_GRANT = p.ID";
        if (col != null) sql += " WHERE " + col + " = ?";
        try (Connection cLoc = ConnectionFactory.getConnection(); PreparedStatement ps = cLoc.prepareStatement(sql)) {
            if (col != null) ps.setString(1, val);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String u = rs.getString(1), h = rs.getString(3), p = rs.getString(2), perm = rs.getString(4), ex = rs.getString(5);
                boolean existe = false;
                if (remotos.containsKey(u)) {
                    for (String[] r : remotos.get(u)) {
                        if (r[0].equalsIgnoreCase(h)) {
                            existe = true;
                            if (!r[1].equalsIgnoreCase(p)) sb.append("ALTER USER '").append(u).append("'@'").append(h).append("' IDENTIFIED WITH mysql_native_password AS '").append(p).append("';\n");
                            break;
                        }
                    }
                }
                if (!existe) {
                    sb.append("CREATE USER IF NOT EXISTS '").append(u).append("'@'").append(h).append("' IDENTIFIED WITH mysql_native_password AS '").append(p).append("';\n");
                    sb.append("ALTER USER '").append(u).append("'@'").append(h).append("' IDENTIFIED WITH mysql_native_password AS '").append(p).append("';\n");
                    sb.append(perm).append(" '").append(u).append("'@'").append(h).append("' ").append(ex).append(";\n");
                }
            }
        }
        return sb.toString();
    }

    private String aplicarScriptResiliente(Connection conn, String script) {
        if (script == null || script.trim().isEmpty()) return "Vazio";
        int ok = 0; StringBuilder errs = new StringBuilder();
        String delim = ";";
        try (Statement stmt = conn.createStatement()) {
            String[] linhas = script.split("\n");
            StringBuilder buf = new StringBuilder();
            for (String l : linhas) {
                String trim = l.trim();
                if (trim.isEmpty() || trim.startsWith("--")) continue;
                if (trim.toUpperCase().startsWith("DELIMITER")) { delim = trim.substring(9).trim(); continue; }
                buf.append(l).append("\n");
                if (trim.endsWith(delim)) {
                    String sql = buf.toString().replace(delim, "").trim();
                    if (!sql.isEmpty()) {
                        try { stmt.execute(sql); ok++; }
                        catch (SQLException e) { errs.append("[").append(e.getErrorCode()).append("] ").append(e.getMessage()).append("; "); }
                    }
                    buf.setLength(0);
                }
            }
        } catch (Exception e) { return "Erro: " + e.getMessage(); }
        return "OK:" + ok + (errs.length() > 0 ? " ERROS: " + errs : "");
    }

    private synchronized void log(String m) { SwingUtilities.invokeLater(() -> areaStatus.append(m + "\n")); }

    private void verificarCriarTabelaLog() {
        String sql = "CREATE TABLE IF NOT EXISTS log_processamento (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "data_execucao TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "cnpj_loja VARCHAR(20), " +
                "nome_loja VARCHAR(100), " +
                "status VARCHAR(20), " +
                "erros_detalhados TEXT)";
        try (Connection c = ConnectionFactory.getConnection(); Statement s = c.createStatement()) {
            s.execute(sql);
        } catch (Exception e) { log("⚠️ Erro ao criar tabela de log: " + e.getMessage()); }
    }

    private void gravarLogLocal(String cnpj, String nome, String status, String msg) {
        String sql = "INSERT INTO log_processamento (cnpj_loja, nome_loja, status, erros_detalhados) VALUES (?,?,?,?)";
        try (Connection c = ConnectionFactory.getConnection(); PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, cnpj); p.setString(2, nome); p.setString(3, status); p.setString(4, msg);
            p.executeUpdate();
        } catch (Exception e) { e.printStackTrace(); }
    }
}