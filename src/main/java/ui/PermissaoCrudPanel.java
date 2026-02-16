/*
package ui;

import javax.swing.*;
import java.awt.*;

public class PermissaoCrudPanel extends JPanel {
    public PermissaoCrudPanel() {
        setLayout(new BorderLayout());
        JLabel label = new JLabel("CRUD Permissoes (Integrado ao schema real)", SwingConstants.CENTER);
        label.setFont(new Font("Arial", Font.BOLD, 16));
        add(label, BorderLayout.CENTER);
    }
}
*/

package ui;

import db.ConnectionFactory;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class PermissaoCrudPanel extends JPanel {

    private JTextField txtPermissao, txtExtra;
    private JButton btnSalvar, btnExcluir;
    private JTable tabelaPermissoes;
    private DefaultTableModel model;

    public PermissaoCrudPanel() {
        setLayout(new BorderLayout(10, 10));

        // --- Painel de Formulário ---
        JPanel pnlForm = new JPanel(new GridBagLayout());
        pnlForm.setBorder(BorderFactory.createTitledBorder("Gerenciar Permissões (Grants)"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        txtPermissao = new JTextField(20);
        txtExtra = new JTextField(20);

        gbc.gridx = 0; gbc.gridy = 0;
        pnlForm.add(new JLabel("Permissão (ex: SELECT, ALL PRIVILEGES):"), gbc);
        gbc.gridx = 1;
        pnlForm.add(txtPermissao, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        pnlForm.add(new JLabel("Escopo/Extra (ex: *.* ou db.*):"), gbc);
        gbc.gridx = 1;
        pnlForm.add(txtExtra, gbc);

        btnSalvar = new JButton("Adicionar Permissão");
        btnSalvar.setBackground(new Color(70, 130, 180));
        btnSalvar.setForeground(Color.WHITE);

        gbc.gridx = 1; gbc.gridy = 2;
        pnlForm.add(btnSalvar, gbc);

        add(pnlForm, BorderLayout.NORTH);

        // --- Tabela ---
        String[] colunas = {"ID", "Permissão", "Extra (Escopo)"};
        model = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tabelaPermissoes = new JTable(model);

        JPanel pnlTabela = new JPanel(new BorderLayout());
        btnExcluir = new JButton("Remover Permissão Selecionada");

        pnlTabela.add(new JScrollPane(tabelaPermissoes), BorderLayout.CENTER);
        pnlTabela.add(btnExcluir, BorderLayout.SOUTH);

        add(pnlTabela, BorderLayout.CENTER);

        // --- Eventos ---
        configurarEventos();
        carregarDados();
    }

    private void configurarEventos() {
        // Salvar
        btnSalvar.addActionListener(e -> {
            String perm = txtPermissao.getText().trim().toUpperCase();
            String extra = txtExtra.getText().trim();

            if (perm.isEmpty()) {
                JOptionPane.showMessageDialog(this, "A permissão não pode ser vazia!");
                return;
            }

            String sql = "INSERT INTO permissoes (PERMISSAO, EXTRA) VALUES (?, ?)";

            try (Connection conn = ConnectionFactory.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setString(1, perm);
                stmt.setString(2, extra.isEmpty() ? "" : extra);

                stmt.executeUpdate();
                JOptionPane.showMessageDialog(this, "Permissão registrada!");

                txtPermissao.setText("");
                txtExtra.setText("");
                carregarDados();

            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Erro: Talvez essa combinação já exista.\n" + ex.getMessage());
            }
        });

        // Excluir
        btnExcluir.addActionListener(e -> {
            int row = tabelaPermissoes.getSelectedRow();
            if (row == -1) return;

            Object id = model.getValueAt(row, 0);
            try (Connection conn = ConnectionFactory.getConnection();
                 PreparedStatement stmt = conn.prepareStatement("DELETE FROM permissoes WHERE ID = ?")) {
                stmt.setObject(1, id);
                stmt.executeUpdate();
                carregarDados();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Erro ao excluir: " + ex.getMessage());
            }
        });
    }

    public void carregarDados() {
        model.setRowCount(0);
        try (Connection conn = ConnectionFactory.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM permissoes ORDER BY PERMISSAO")) {

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("ID"),
                        rs.getString("PERMISSAO"),
                        rs.getString("EXTRA")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}