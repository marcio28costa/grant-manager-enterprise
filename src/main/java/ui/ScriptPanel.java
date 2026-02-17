/*
package ui;

import service.GrantService;
import javax.swing.*;
import java.awt.*;

public class ScriptPanel extends JPanel {

    private JTextArea area;

    public ScriptPanel() {
        setLayout(new BorderLayout());

        JButton gerar = new JButton("Gerar Script Geral (Provisionamento)");
        area = new JTextArea();
        area.setFont(new Font("Monospaced", Font.PLAIN, 13));

        add(gerar, BorderLayout.NORTH);
        add(new JScrollPane(area), BorderLayout.CENTER);

        gerar.addActionListener(e -> {
            GrantService service = new GrantService();
            area.setText(service.gerarScriptGeral());
            area.setCaretPosition(0);
        });
    }
}
*/
/*
package ui;

import service.GrantService;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class ScriptPanel extends JPanel {

    private JTextArea area;

    public ScriptPanel() {
        setLayout(new BorderLayout());

        // Painel de botões no topo
        JPanel pnlBotoes = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton gerar = new JButton("Gerar Script Geral (Provisionamento)");
        JButton exportar = new JButton("Salvar como .sql");

        pnlBotoes.add(gerar);
        pnlBotoes.add(exportar);

        area = new JTextArea();
        area.setFont(new Font("Monospaced", Font.PLAIN, 13));

        add(pnlBotoes, BorderLayout.NORTH);
        add(new JScrollPane(area), BorderLayout.CENTER);

        // Ação de Gerar
        gerar.addActionListener(e -> {
            GrantService service = new GrantService();
            area.setText(service.gerarScriptGeral());
            area.setCaretPosition(0);
        });

        // Ação de Exportar
        exportar.addActionListener(e -> {
            String conteudo = area.getText();
            if (conteudo.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Gere o script primeiro!", "Aviso", JOptionPane.WARNING_MESSAGE);
                return;
            }
            salvarArquivo(conteudo);
        });
    }

    private void salvarArquivo(String texto) {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Salvar Script SQL");
        chooser.setSelectedFile(new File("provisionamento.sql"));

        int userSelection = chooser.showSaveDialog(this);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File arquivoParaSalvar = chooser.getSelectedFile();

            // Garante a extensão .sql
            String caminho = arquivoParaSalvar.getAbsolutePath();
            if (!caminho.toLowerCase().endsWith(".sql")) {
                arquivoParaSalvar = new File(caminho + ".sql");
            }

            try (FileWriter fw = new FileWriter(arquivoParaSalvar)) {
                fw.write(texto);
                JOptionPane.showMessageDialog(this, "Arquivo salvo com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Erro ao salvar arquivo: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}

 */

package ui;

import service.GrantService;
import db.ConnectionFactory;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;

public class ScriptPanel extends JPanel {

    private JTextArea area;
    private JComboBox<String> cbFiltroTipo, cbOpcoes;
    private GrantService service = new GrantService();

    public ScriptPanel() {
        setLayout(new BorderLayout());

        // Painel de Controles
        JPanel pnlControles = new JPanel(new FlowLayout(FlowLayout.LEFT));

        cbFiltroTipo = new JComboBox<>(new String[]{"GERAL", "POR USUARIO", "POR IP", "POR TIPO"});
        cbOpcoes = new JComboBox<>();
        cbOpcoes.setPreferredSize(new Dimension(150, 25));
        cbOpcoes.setEnabled(false);

        JButton btnGerar = new JButton("Gerar Script");
        JButton btnExportar = new JButton("Salvar .sql");

        pnlControles.add(new JLabel("Filtro:"));
        pnlControles.add(cbFiltroTipo);
        pnlControles.add(cbOpcoes);
        pnlControles.add(btnGerar);
        pnlControles.add(btnExportar);

        area = new JTextArea();
        area.setFont(new Font("Monospaced", Font.PLAIN, 13));

        add(pnlControles, BorderLayout.NORTH);
        add(new JScrollPane(area), BorderLayout.CENTER);

        // Lógica para carregar opções conforme o tipo de filtro
        cbFiltroTipo.addActionListener(e -> carregarOpcoesFiltro());

        btnGerar.addActionListener(e -> {
            String filtro = (String) cbFiltroTipo.getSelectedItem();
            String valor = (String) cbOpcoes.getSelectedItem();

            if (filtro.equals("GERAL")) {
                area.setText(service.gerarScript(null, null));
            } else {
                String coluna = filtro.equals("POR USUARIO") ? "u.USERNAME" :
                        filtro.equals("POR IP") ? "i.ENDERECO_GRANT" : "i.TIPO";
                area.setText(service.gerarScript(coluna, valor));
            }
            area.setCaretPosition(0);
        });

        btnExportar.addActionListener(e -> exportar());
    }

    private void carregarOpcoesFiltro() {
        String selecionado = (String) cbFiltroTipo.getSelectedItem();
        cbOpcoes.removeAllItems();

        if (selecionado.equals("GERAL")) {
            cbOpcoes.setEnabled(false);
            return;
        }

        cbOpcoes.setEnabled(true);
        String sql = "";
        if (selecionado.equals("POR USUARIO")) sql = "SELECT DISTINCT USERNAME FROM usuarios";
        else if (selecionado.equals("POR IP")) sql = "SELECT DISTINCT ENDERECO_GRANT FROM enderecosip WHERE ENDERECO_GRANT IS NOT NULL";
        else if (selecionado.equals("POR TIPO")) sql = "SELECT DISTINCT TIPO FROM enderecosip";

        try (Connection conn = ConnectionFactory.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                cbOpcoes.addItem(rs.getString(1));
            }
        } catch (SQLException ex) { ex.printStackTrace(); }
    }

    private void exportar() {
        if (area.getText().isEmpty()) return;
        JFileChooser chooser = new JFileChooser();
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try (FileWriter fw = new FileWriter(chooser.getSelectedFile() + ".sql")) {
                fw.write(area.getText());
                JOptionPane.showMessageDialog(this, "Salvo!");
            } catch (IOException ex) { ex.printStackTrace(); }
        }
    }
}



