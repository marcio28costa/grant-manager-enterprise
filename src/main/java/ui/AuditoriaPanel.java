package ui;

import service.AuditoriaService;
import db.ConnectionFactory;
import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;

public class AuditoriaPanel extends JPanel {

    private JTextField txtHost, txtPorta, txtUser;
    private JPasswordField txtPass;
    private JComboBox<String> cbTipoFiltro, cbOpcoes;
    private JTextArea areaLog;
    private JButton btnAuditar, btnCopiar, btnAplicar, btnSalvar, btnLimpar;

    public AuditoriaPanel() {
        setLayout(new BorderLayout(5, 5));

        // PAINEL DE CONEXÃO E FILTROS
        JPanel pnlTopo = new JPanel();
        pnlTopo.setLayout(new BoxLayout(pnlTopo, BoxLayout.Y_AXIS));

        JPanel pnlCon = new JPanel(new FlowLayout(FlowLayout.LEFT));
        txtHost = new JTextField("localhost", 10);
        txtPorta = new JTextField("3306", 4);
        txtUser = new JTextField("root", 8);
        txtPass = new JPasswordField(8);
        pnlCon.add(new JLabel("Host Remoto:")); pnlCon.add(txtHost);
        pnlCon.add(new JLabel("Porta:")); pnlCon.add(txtPorta);
        pnlCon.add(new JLabel("User:")); pnlCon.add(txtUser);
        pnlCon.add(new JLabel("Pass:")); pnlCon.add(txtPass);

        JPanel pnlFil = new JPanel(new FlowLayout(FlowLayout.LEFT));
        cbTipoFiltro = new JComboBox<>(new String[]{"GERAL", "POR USUARIO", "POR ENDERECO IP", "POR TIPO"});
        cbOpcoes = new JComboBox<>();
        cbOpcoes.setPreferredSize(new Dimension(200, 25));
        cbOpcoes.setEnabled(false);
        btnAuditar = new JButton("Auditar");
        btnLimpar = new JButton("Limpar");
        pnlFil.add(new JLabel("Filtro:")); pnlFil.add(cbTipoFiltro);
        pnlFil.add(cbOpcoes); pnlFil.add(btnAuditar); pnlFil.add(btnLimpar);

        pnlTopo.add(pnlCon); pnlTopo.add(pnlFil);
        add(pnlTopo, BorderLayout.NORTH);

        // ÁREA DE LOG (RESULTADO DA AUDITORIA)
        areaLog = new JTextArea();
        areaLog.setFont(new Font("Monospaced", Font.PLAIN, 12));
        areaLog.setEditable(false);
        add(new JScrollPane(areaLog), BorderLayout.CENTER);

        // PAINEL DE AÇÕES
        JPanel pnlAcoes = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnCopiar = new JButton("Copiar");
        btnSalvar = new JButton("Salvar .sql");
        btnAplicar = new JButton("APLICAR NO BANCO");
        btnAplicar.setBackground(new Color(153, 0, 0));
        btnAplicar.setForeground(Color.WHITE);
        btnAplicar.setEnabled(false);

        pnlAcoes.add(btnCopiar); pnlAcoes.add(btnSalvar); pnlAcoes.add(btnAplicar);
        add(pnlAcoes, BorderLayout.SOUTH);

        // EVENTOS
        cbTipoFiltro.addActionListener(e -> carregarOpcoesFiltro());
        btnAuditar.addActionListener(e -> realizarAuditoria());
        btnLimpar.addActionListener(e -> { areaLog.setText(""); btnAplicar.setEnabled(false); });

        btnCopiar.addActionListener(e -> {
            StringSelection s = new StringSelection(areaLog.getText());
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(s, s);
            JOptionPane.showMessageDialog(this, "Copiado!");
        });

        btnSalvar.addActionListener(e -> salvarArquivoSql());

        btnAplicar.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Deseja executar os comandos no servidor remoto?\nO sistema tentará executar todos, mesmo que alguns falhem.",
                    "Confirmar Execução", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) aplicarCorrecao();
        });
    }

    private void carregarOpcoesFiltro() {
        String sel = (String) cbTipoFiltro.getSelectedItem();
        cbOpcoes.removeAllItems();
        if (sel.equals("GERAL")) { cbOpcoes.setEnabled(false); return; }
        cbOpcoes.setEnabled(true);
        String sql = sel.equals("POR USUARIO") ? "SELECT DISTINCT USERNAME FROM usuarios ORDER BY USERNAME" :
                sel.equals("POR ENDERECO IP") ? "SELECT DISTINCT ENDERECO_GRANT FROM enderecosip" :
                        "SELECT DISTINCT TIPO FROM enderecosip";
        try (Connection c = ConnectionFactory.getConnection(); Statement s = c.createStatement(); ResultSet r = s.executeQuery(sql)) {
            while (r.next()) cbOpcoes.addItem(r.getString(1));
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    private void realizarAuditoria() {
        String f = (String) cbTipoFiltro.getSelectedItem();
        String col = f.equals("GERAL") ? null : f.equals("POR USUARIO") ? "u.USERNAME" : f.equals("POR TIPO") ? "i.TIPO" : "i.ENDERECO_GRANT";

        areaLog.setText("-- Processando auditoria...");
        new Thread(() -> {
            AuditoriaService s = new AuditoriaService();
            String res = s.executarAuditoria(txtHost.getText(), txtPorta.getText(), txtUser.getText(),
                    new String(txtPass.getPassword()), col, (String)cbOpcoes.getSelectedItem());
            SwingUtilities.invokeLater(() -> {
                areaLog.setText(res);
                btnAplicar.setEnabled(!res.isEmpty() && (res.contains("ALTER") || res.contains("CREATE")));
            });
        }).start();
    }

    private void aplicarCorrecao() {
        new Thread(() -> {
            AuditoriaService s = new AuditoriaService();
            String res = s.aplicarScriptRemoto(txtHost.getText(), txtPorta.getText(), txtUser.getText(),
                    new String(txtPass.getPassword()), areaLog.getText());

            SwingUtilities.invokeLater(() -> {
                // CRIANDO RELATÓRIO COM SCROLL PARA O USUÁRIO
                JTextArea txtRelatorio = new JTextArea(res);
                txtRelatorio.setEditable(false);
                txtRelatorio.setFont(new Font("Monospaced", Font.PLAIN, 12));

                JScrollPane scroll = new JScrollPane(txtRelatorio);
                scroll.setPreferredSize(new Dimension(700, 400));

                JOptionPane.showMessageDialog(this, scroll, "Relatório de Execução Remota", JOptionPane.INFORMATION_MESSAGE);

                realizarAuditoria(); // Atualiza a auditoria para ver o que foi corrigido
            });
        }).start();
    }

    private void salvarArquivoSql() {
        JFileChooser fc = new JFileChooser();
        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File f = fc.getSelectedFile();
            if (!f.getName().endsWith(".sql")) f = new File(f.getAbsolutePath() + ".sql");
            try (FileWriter fw = new FileWriter(f)) {
                fw.write(areaLog.getText());
                JOptionPane.showMessageDialog(this, "Salvo com sucesso!");
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Erro ao salvar: " + ex.getMessage());
            }
        }
    }
}