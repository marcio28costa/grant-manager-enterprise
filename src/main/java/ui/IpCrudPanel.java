/*
package ui;

import javax.swing.*;
import java.awt.*;

public class IpCrudPanel extends JPanel {
    public IpCrudPanel() {
        setLayout(new BorderLayout());
        JLabel label = new JLabel("CRUD Enderecos IP (ENDERECO_GRANT) (Integrado ao schema real)", SwingConstants.CENTER);
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

public class IpCrudPanel extends JPanel {

    private JTextField txtIp, txtGrant;
    private JComboBox<String> cbTipo;
    private JButton btnSalvar, btnExcluir;
    private JTable tabelaIps;
    private DefaultTableModel model;

    public IpCrudPanel() {
        setLayout(new BorderLayout(10, 10));

        // --- Painel de Formulário ---
        JPanel pnlForm = new JPanel(new GridLayout(4, 2, 5, 5));
        pnlForm.setBorder(BorderFactory.createTitledBorder("Gerenciar IPs/Hosts"));

        txtIp = new JTextField();
        txtGrant = new JTextField();
        cbTipo = new JComboBox<>(new String[]{"DNS", "LOCAL", "CITEL"});

        pnlForm.add(new JLabel("Endereço IP/Host (Ex: 192.168.0.1):"));
        pnlForm.add(txtIp);
        pnlForm.add(new JLabel("Tipo:"));
        pnlForm.add(cbTipo);
        pnlForm.add(new JLabel("Endereço Grant (Ex: % ou localhost):"));
        pnlForm.add(txtGrant);

        btnSalvar = new JButton("Salvar IP");
        btnExcluir = new JButton("Excluir Selecionado");

        JPanel pnlBotoes = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        pnlBotoes.add(btnExcluir);
        pnlBotoes.add(btnSalvar);

        add(pnlForm, BorderLayout.NORTH);
        add(pnlBotoes, BorderLayout.SOUTH); // Adicionado painel de botões abaixo do formulário

        // --- Tabela ---
        String[] colunas = {"ID", "IP/Host", "Tipo", "Grant"};
        model = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tabelaIps = new JTable(model);
        add(new JScrollPane(tabelaIps), BorderLayout.CENTER);

        // --- Eventos ---
        configurarEventos();
        carregarDados();
    }

    private void configurarEventos() {
        // Botão Salvar
        btnSalvar.addActionListener(e -> {
            String ip = txtIp.getText().trim();
            String tipo = (String) cbTipo.getSelectedItem();
            String grant = txtGrant.getText().trim();

            if (ip.isEmpty()) {
                JOptionPane.showMessageDialog(this, "O campo IP é obrigatório!");
                return;
            }

            String sql = "INSERT INTO enderecosip (ENDERECO_IP, TIPO, ENDERECO_GRANT) VALUES (?, ?, ?)";

            try (Connection conn = ConnectionFactory.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setString(1, ip);
                stmt.setString(2, tipo);
                stmt.setString(3, grant.isEmpty() ? null : grant);

                stmt.executeUpdate();
                JOptionPane.showMessageDialog(this, "IP salvo com sucesso!");

                txtIp.setText("");
                txtGrant.setText("");
                carregarDados();

            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Erro ao salvar: " + ex.getMessage());
            }
        });

        // Botão Excluir
        btnExcluir.addActionListener(e -> {
            int row = tabelaIps.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Selecione um item na tabela para excluir.");
                return;
            }

            Object id = model.getValueAt(row, 0);
            int confirm = JOptionPane.showConfirmDialog(this, "Deseja excluir o IP ID: " + id + "?");

            if (confirm == JOptionPane.YES_OPTION) {
                try (Connection conn = ConnectionFactory.getConnection();
                     PreparedStatement stmt = conn.prepareStatement("DELETE FROM enderecosip WHERE ID = ?")) {

                    stmt.setObject(1, id);
                    stmt.executeUpdate();
                    carregarDados();

                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(this, "Erro ao excluir: " + ex.getMessage());
                }
            }
        });
    }

    public void carregarDados() {
        model.setRowCount(0);
        String sql = "SELECT ID, ENDERECO_IP, TIPO, ENDERECO_GRANT FROM enderecosip ORDER BY ID DESC";

        try (Connection conn = ConnectionFactory.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("ID"),
                        rs.getString("ENDERECO_IP"),
                        rs.getString("TIPO"),
                        rs.getString("ENDERECO_GRANT")
                });
            }
        } catch (SQLException e) {
            System.err.println("Erro ao carregar IPs: " + e.getMessage());
        }
    }
}