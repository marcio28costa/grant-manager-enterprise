package ui;

import db.ConnectionFactory;
import service.AuditoriaService; // Reutilizando a lógica do Service
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
    private JTextField txtPesquisa, txtUserAuditoria;
    private JPasswordField txtPassAuditoria;
    private JTextArea txtPreComandosArea, areaStatus;
    private JCheckBox chkPesquisaContida, chkSqlLogBin;
    private JTable tabelaLojas;
    private DefaultTableModel model;
    private JProgressBar barraProgresso;
    private JButton btnCarregarLojas, btnPesquisar, btnIniciar, btnAddIgnore, btnAddDrop;
    private JComboBox<String> cbFiltroAuditoria, cbOpcaoAuditoria, cbUserIgnore, cbUserDrop;
    private DefaultListModel<String> modelIgnore, modelDrop;

    public ProcessaUsuariosPanel() {
        setLayout(new BorderLayout(5, 5));

        // --- PAINEL NORTE: CONFIGURAÇÕES ---
        JPanel pnlNorte = new JPanel();
        pnlNorte.setLayout(new BoxLayout(pnlNorte, BoxLayout.Y_AXIS));

        // 1. Fonte de Lojas (Configurado via App)
        JPanel pnlFonte = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pnlFonte.setBorder(BorderFactory.createTitledBorder("1. Fonte de Lojas (Configuração Central)"));
        btnCarregarLojas = new JButton("Carregar Lojas da Base Central");
        txtPesquisa = new JTextField(15);
        chkPesquisaContida = new JCheckBox("LIKE %%");
        btnPesquisar = new JButton("Filtrar Grid");
        pnlFonte.add(btnCarregarLojas);
        pnlFonte.add(new JLabel(" | Busca:")); pnlFonte.add(txtPesquisa);
        pnlFonte.add(chkPesquisaContida); pnlFonte.add(btnPesquisar);

        // 2. Comandos Pré-Auditoria
        JPanel pnlPre = new JPanel(new BorderLayout());
        pnlPre.setBorder(BorderFactory.createTitledBorder("2. Comandos Pré-Auditoria (Executados em cada Loja)"));
        chkSqlLogBin = new JCheckBox("SET sql_log_bin=0;");
        txtPreComandosArea = new JTextArea(4, 50);
        pnlPre.add(chkSqlLogBin, BorderLayout.NORTH);
        pnlPre.add(new JScrollPane(txtPreComandosArea), BorderLayout.CENTER);

        // 3. Regras e Listas (Ignorar/Drop)
        JPanel pnlRegras = new JPanel(new GridLayout(1, 2, 5, 5));

        // Coluna Esquerda: Regra de Comparação
        JPanel pnlAudit = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pnlAudit.setBorder(BorderFactory.createTitledBorder("3. Regra de Comparação"));
        txtUserAuditoria = new JTextField("root", 6);
        txtPassAuditoria = new JPasswordField(6);
        cbFiltroAuditoria = new JComboBox<>(new String[]{"GERAL", "POR USUARIO", "POR ENDERECO IP"});
        cbOpcaoAuditoria = new JComboBox<>(); cbOpcaoAuditoria.setPreferredSize(new Dimension(120, 25));
        pnlAudit.add(new JLabel("Login Loja:")); pnlAudit.add(txtUserAuditoria); pnlAudit.add(txtPassAuditoria);
        pnlAudit.add(cbFiltroAuditoria); pnlAudit.add(cbOpcaoAuditoria);

        // Coluna Direita: Listas Dinâmicas
        JPanel pnlListas = new JPanel(new GridLayout(2, 1));

        // Sub-Painel Ignore
        JPanel pnlI = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pnlI.setBorder(BorderFactory.createTitledBorder("Ignorar na Auditoria"));
        cbUserIgnore = new JComboBox<>(); modelIgnore = new DefaultListModel<>();
        btnAddIgnore = new JButton("+");
        JList<String> listI = new JList<>(modelIgnore); JScrollPane spI = new JScrollPane(listI); spI.setPreferredSize(new Dimension(100, 35));
        pnlI.add(cbUserIgnore); pnlI.add(btnAddIgnore); pnlI.add(spI);

        // Sub-Painel Drop
        JPanel pnlD = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pnlD.setBorder(BorderFactory.createTitledBorder("Limpeza (DROP)"));
        cbUserDrop = new JComboBox<>(); modelDrop = new DefaultListModel<>();
        btnAddDrop = new JButton("+");
        JList<String> listD = new JList<>(modelDrop); JScrollPane spD = new JScrollPane(listD); spD.setPreferredSize(new Dimension(100, 35));
        pnlD.add(cbUserDrop); pnlD.add(btnAddDrop); pnlD.add(spD);

        pnlListas.add(pnlI); pnlListas.add(pnlD);

        JPanel pnlConfigsInferior = new JPanel(new BorderLayout());
        pnlConfigsInferior.add(pnlAudit, BorderLayout.WEST);
        pnlConfigsInferior.add(pnlListas, BorderLayout.CENTER);

        pnlNorte.add(pnlFonte); pnlNorte.add(pnlPre); pnlNorte.add(pnlConfigsInferior);
        add(pnlNorte, BorderLayout.NORTH);

        // --- GRID CENTRAL ---
        model = new DefaultTableModel(new String[]{"Sel", "Empresa", "Loja", "IP", "Porta", "CNPJ"}, 0) {
            @Override public Class<?> getColumnClass(int c) { return c == 0 ? Boolean.class : String.class; }
        };
        tabelaLojas = new JTable(model);
        add(new JScrollPane(tabelaLojas), BorderLayout.CENTER);

        // --- STATUS E EXECUÇÃO (SUL) ---
        JPanel pnlSul = new JPanel(new BorderLayout());
        areaStatus = new JTextArea(6, 20);
        barraProgresso = new JProgressBar(); barraProgresso.setStringPainted(true);
        btnIniciar = new JButton("EXECUTAR PROCESSAMENTO EM MASSA (MULTI-THREAD)");
        btnIniciar.setBackground(new Color(0, 102, 51)); btnIniciar.setForeground(Color.WHITE);
        pnlSul.add(new JScrollPane(areaStatus), BorderLayout.CENTER);
        pnlSul.add(barraProgresso, BorderLayout.NORTH);
        pnlSul.add(btnIniciar, BorderLayout.SOUTH);
        add(pnlSul, BorderLayout.SOUTH);

        configurarEventos();
        carregarUsuariosCombos();
    }

    private void configurarEventos() {
        btnCarregarLojas.addActionListener(e -> listarLojas());
        btnPesquisar.addActionListener(e -> listarLojas());
        cbFiltroAuditoria.addActionListener(e -> carregarFiltrosLocais());
        btnAddIgnore.addActionListener(e -> { if(!modelIgnore.contains(cbUserIgnore.getSelectedItem())) modelIgnore.addElement((String)cbUserIgnore.getSelectedItem()); });
        btnAddDrop.addActionListener(e -> { if(!modelDrop.contains(cbUserDrop.getSelectedItem())) modelDrop.addElement((String)cbUserDrop.getSelectedItem()); });
        btnIniciar.addActionListener(e -> dispararThreads());
    }

    private void carregarUsuariosCombos() {
        try (Connection c = ConnectionFactory.getConnection(); Statement s = c.createStatement();
             ResultSet r = s.executeQuery("SELECT username FROM usuarios WHERE username NOT IN ('root','dbacitel') ORDER BY username")) {
            while (r.next()) {
                cbUserIgnore.addItem(r.getString(1));
                cbUserDrop.addItem(r.getString(1));
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void carregarFiltrosLocais() {
        cbOpcaoAuditoria.removeAllItems();
        if (cbFiltroAuditoria.getSelectedIndex() == 0) return;
        String sql = cbFiltroAuditoria.getSelectedIndex() == 1 ? "SELECT DISTINCT USERNAME FROM usuarios" : "SELECT DISTINCT ENDERECO_GRANT FROM enderecosip";
        try (Connection c = ConnectionFactory.getConnection(); Statement s = c.createStatement(); ResultSet r = s.executeQuery(sql)) {
            while (r.next()) cbOpcaoAuditoria.addItem(r.getString(1));
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void listarLojas() {
        model.setRowCount(0);
        // Busca conexão central do factory (que lê do arquivo app)
        try (Connection conn = ConnectionFactory.getConnection(); Statement st = conn.createStatement()) {
            String sql = "SELECT E.EMP_NOMEMP, L.LOJ_NOMLOJ, L.LOJ_NUM_IP, L.LOJ_PORYOG, L.LOJ_C_G_C_ " +
                    "FROM diariomsql.EMPLOJ L JOIN diariomsql.CADEMP E ON L.LOJ_CODEMP = E.EMP_CODEMP";
            String busca = txtPesquisa.getText().trim();
            if (!busca.isEmpty()) {
                String op = chkPesquisaContida.isSelected() ? " LIKE '%" + busca + "%'" : " = '" + busca + "'";
                sql += " WHERE E.EMP_NOMEMP " + op + " OR L.LOJ_NOMLOJ " + op;
            }
            ResultSet rs = st.executeQuery(sql);
            while (rs.next()) model.addRow(new Object[]{false, rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5)});
        } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Erro ao carregar lojas: " + ex.getMessage()); }
    }

    private void dispararThreads() {
        List<Integer> selecionados = new ArrayList<>();
        for (int i = 0; i < model.getRowCount(); i++) if ((Boolean) model.getValueAt(i, 0)) selecionados.add(i);
        if (selecionados.isEmpty()) { JOptionPane.showMessageDialog(this, "Selecione lojas!"); return; }

        List<String> ignoreList = new ArrayList<>(); for(int i=0; i<modelIgnore.size(); i++) ignoreList.add(modelIgnore.get(i));
        List<String> dropList = new ArrayList<>(); for(int i=0; i<modelDrop.size(); i++) dropList.add(modelDrop.get(i));

        new Thread(() -> {
            btnIniciar.setEnabled(false);
            barraProgresso.setMaximum(selecionados.size());
            barraProgresso.setValue(0);

            ExecutorService executor = Executors.newFixedThreadPool(8); // Aumentado para 8 threads
            String col = cbFiltroAuditoria.getSelectedIndex() == 1 ? "POR USUARIO" : (cbFiltroAuditoria.getSelectedIndex() == 2 ? "POR ENDERECO IP" : "GERAL");
            String val = (String) cbOpcaoAuditoria.getSelectedItem();

            for (int rowIdx : selecionados) {
                String ip = (String) model.getValueAt(rowIdx, 3);
                String porta = (String) model.getValueAt(rowIdx, 4);
                String nome = (String) model.getValueAt(rowIdx, 2);
                String cnpj = (String) model.getValueAt(rowIdx, 5);

                executor.execute(() -> {
                    processarLoja(ip, porta, nome, cnpj, col, val, ignoreList, dropList);
                    SwingUtilities.invokeLater(() -> barraProgresso.setValue(barraProgresso.getValue() + 1));
                });
            }
            executor.shutdown();
            try { executor.awaitTermination(2, TimeUnit.HOURS); } catch (Exception e) {}
            SwingUtilities.invokeLater(() -> { btnIniciar.setEnabled(true); areaStatus.append("### FIM DO PROCESSAMENTO ###\n"); });
        }).start();
    }

    private void processarLoja(String ip, String porta, String nome, String cnpj, String filtro, String valor, List<String> ignore, List<String> drop) {
        String user = txtUserAuditoria.getText();
        String pass = new String(txtPassAuditoria.getPassword());

        try {
            log("Conectando: " + nome + " (" + ip + ")");
            AuditoriaService service = new AuditoriaService();

            // 1. Gerar o script (já inclui Drop e pula Ignorados)
            String script = service.executarAuditoria(ip, porta, user, pass, filtro, valor, ignore, drop);

            // 2. Adicionar pré-comandos se houver
            if (chkSqlLogBin.isSelected()) script = "SET sql_log_bin=0;\n" + script;
            if (!txtPreComandosArea.getText().trim().isEmpty()) script = txtPreComandosArea.getText() + "\n" + script;

            // 3. Aplicar
            String resultado = service.aplicarScriptRemoto(ip, porta, user, pass, script);

            log("✅ " + nome + ": " + resultado);
            gravarLogLocal(cnpj, nome, "SUCESSO", resultado);
        } catch (Exception e) {
            log("❌ " + nome + ": " + e.getMessage());
            gravarLogLocal(cnpj, nome, "ERRO", e.getMessage());
        }
    }

    private synchronized void log(String m) { SwingUtilities.invokeLater(() -> areaStatus.append(m + "\n")); }

    private void gravarLogLocal(String cnpj, String nome, String status, String msg) {
        String sql = "INSERT INTO log_processamento (cnpj_loja, nome_loja, status, erros_detalhados) VALUES (?,?,?,?)";
        try (Connection c = ConnectionFactory.getConnection(); PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, cnpj); p.setString(2, nome); p.setString(3, status); p.setString(4, msg);
            p.executeUpdate();
        } catch (Exception e) {}
    }
}