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