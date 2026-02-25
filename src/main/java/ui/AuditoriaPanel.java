package ui;

import service.AuditoriaService;
import db.ConnectionFactory;
import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AuditoriaPanel extends JPanel {
    private JTextField txtHost, txtPorta, txtUser;
    private JPasswordField txtPass;
    private JComboBox<String> cbTipoFiltro, cbOpcoes, cbUserIgnore, cbUserDrop;
    private JTextArea areaLog;
    private DefaultListModel<String> modelIgnore, modelDrop;
    private JButton btnAuditar, btnAplicar, btnLimpar;

    public AuditoriaPanel() {
        setLayout(new BorderLayout(5, 5));
        JPanel pnlNorte = new JPanel();
        pnlNorte.setLayout(new BoxLayout(pnlNorte, BoxLayout.Y_AXIS));

        // 1. CONEXÃO
        JPanel pnlCon = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pnlCon.setBorder(BorderFactory.createTitledBorder("Conexão com Servidor Remoto"));
        txtHost = new JTextField("localhost", 10);
        txtPorta = new JTextField("3306", 4);
        txtUser = new JTextField("root", 8);
        txtPass = new JPasswordField(8);
        pnlCon.add(new JLabel("Host:")); pnlCon.add(txtHost);
        pnlCon.add(new JLabel("Porta:")); pnlCon.add(txtPorta);
        pnlCon.add(new JLabel("User:")); pnlCon.add(txtUser);
        pnlCon.add(new JLabel("Pass:")); pnlCon.add(txtPass);

        // 2. FILTROS
        JPanel pnlFil = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pnlFil.setBorder(BorderFactory.createTitledBorder("Filtros da Base Local"));
        cbTipoFiltro = new JComboBox<>(new String[]{"GERAL", "POR USUARIO", "POR ENDERECO IP", "POR TIPO"});
        cbOpcoes = new JComboBox<>(); cbOpcoes.setPreferredSize(new Dimension(150, 25));
        btnAuditar = new JButton("Gerar Auditoria");
        btnLimpar = new JButton("Limpar");
        pnlFil.add(new JLabel("Filtro:")); pnlFil.add(cbTipoFiltro);
        pnlFil.add(cbOpcoes); pnlFil.add(btnAuditar); pnlFil.add(btnLimpar);

        // 3. IGNORAR
        JPanel pnlIgnore = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pnlIgnore.setBorder(BorderFactory.createTitledBorder("Ignorar na Auditoria (Não serão criados/alterados)"));
        cbUserIgnore = new JComboBox<>(); cbUserIgnore.setPreferredSize(new Dimension(150, 25));
        JButton btnAddIgnore = new JButton("Add +");
        modelIgnore = new DefaultListModel<>();
        JList<String> listIgnore = new JList<>(modelIgnore);
        JScrollPane spIgnore = new JScrollPane(listIgnore); spIgnore.setPreferredSize(new Dimension(150, 40));
        JButton btnRemIgnore = new JButton("Remover");
        pnlIgnore.add(new JLabel("Selecione:")); pnlIgnore.add(cbUserIgnore); pnlIgnore.add(btnAddIgnore); pnlIgnore.add(spIgnore); pnlIgnore.add(btnRemIgnore);

        // 4. DROP
        JPanel pnlDrop = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pnlDrop.setBorder(BorderFactory.createTitledBorder("Limpeza Prévia (Usuários para DROP - Exceto root/dbacitel)"));
        cbUserDrop = new JComboBox<>(); cbUserDrop.setPreferredSize(new Dimension(150, 25));
        JButton btnAddDrop = new JButton("Add +");
        modelDrop = new DefaultListModel<>();
        JList<String> listDrop = new JList<>(modelDrop);
        JScrollPane spDrop = new JScrollPane(listDrop); spDrop.setPreferredSize(new Dimension(150, 40));
        JButton btnRemDrop = new JButton("Remover");
        pnlDrop.add(new JLabel("Selecione:")); pnlDrop.add(cbUserDrop); pnlDrop.add(btnAddDrop); pnlDrop.add(spDrop); pnlDrop.add(btnRemDrop);

        pnlNorte.add(pnlCon); pnlNorte.add(pnlFil); pnlNorte.add(pnlIgnore); pnlNorte.add(pnlDrop);
        add(pnlNorte, BorderLayout.NORTH);

        areaLog = new JTextArea();
        areaLog.setFont(new Font("Monospaced", Font.PLAIN, 12));
        add(new JScrollPane(areaLog), BorderLayout.CENTER);

        btnAplicar = new JButton("EXECUTAR NO BANCO");
        btnAplicar.setBackground(new Color(0, 102, 0)); btnAplicar.setForeground(Color.WHITE);
        add(btnAplicar, BorderLayout.SOUTH);

        configurarAcoes(btnAddIgnore, btnRemIgnore, btnAddDrop, btnRemDrop);
        carregarCombos();
    }

    private void configurarAcoes(JButton ai, JButton ri, JButton ad, JButton rd) {
        cbTipoFiltro.addActionListener(e -> carregarOpcoesFiltro());
        ai.addActionListener(e -> { if(!modelIgnore.contains(cbUserIgnore.getSelectedItem())) modelIgnore.addElement((String)cbUserIgnore.getSelectedItem()); });
        ri.addActionListener(e -> { int i = modelIgnore.indexOf(cbUserIgnore.getSelectedItem()); if(i!=-1) modelIgnore.remove(i); });
        ad.addActionListener(e -> { if(!modelDrop.contains(cbUserDrop.getSelectedItem())) modelDrop.addElement((String)cbUserDrop.getSelectedItem()); });
        rd.addActionListener(e -> { int i = modelDrop.indexOf(cbUserDrop.getSelectedItem()); if(i!=-1) modelDrop.remove(i); });
        btnAuditar.addActionListener(e -> acaoGerar());
        btnLimpar.addActionListener(e -> areaLog.setText(""));
        btnAplicar.addActionListener(e -> {
            AuditoriaService s = new AuditoriaService();
            String res = s.aplicarScriptRemoto(txtHost.getText(), txtPorta.getText(), txtUser.getText(), new String(txtPass.getPassword()), areaLog.getText());
            JOptionPane.showMessageDialog(this, res);
        });
    }

    private void carregarCombos() {
        try (Connection c = ConnectionFactory.getConnection(); Statement s = c.createStatement(); ResultSet r = s.executeQuery("SELECT username FROM usuarios WHERE username NOT IN ('root','dbacitel') ORDER BY username")) {
            while (r.next()) {
                String u = r.getString(1);
                cbUserIgnore.addItem(u); cbUserDrop.addItem(u);
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void carregarOpcoesFiltro() {
        String sel = (String) cbTipoFiltro.getSelectedItem();
        cbOpcoes.removeAllItems();
        if (sel.equals("GERAL")) { cbOpcoes.setEnabled(false); return; }
        cbOpcoes.setEnabled(true);
        String sql = sel.equals("POR USUARIO") ? "SELECT DISTINCT USERNAME FROM usuarios ORDER BY USERNAME" :
                sel.equals("POR ENDERECO IP") ? "SELECT DISTINCT ENDERECO_GRANT FROM enderecosip" : "SELECT DISTINCT TIPO FROM enderecosip";
        try (Connection c = ConnectionFactory.getConnection(); Statement s = c.createStatement(); ResultSet r = s.executeQuery(sql)) {
            while (r.next()) cbOpcoes.addItem(r.getString(1));
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    private void acaoGerar() {
        List<String> ignoreList = new ArrayList<>(); for(int i=0; i<modelIgnore.size(); i++) ignoreList.add(modelIgnore.get(i));
        List<String> dropList = new ArrayList<>(); for(int i=0; i<modelDrop.size(); i++) dropList.add(modelDrop.get(i));

        new Thread(() -> {
            AuditoriaService s = new AuditoriaService();
            String res = s.executarAuditoria(txtHost.getText(), txtPorta.getText(), txtUser.getText(), new String(txtPass.getPassword()),
                    (String)cbTipoFiltro.getSelectedItem(), (String)cbOpcoes.getSelectedItem(), ignoreList, dropList);
            SwingUtilities.invokeLater(() -> areaLog.setText(res));
        }).start();
    }
}