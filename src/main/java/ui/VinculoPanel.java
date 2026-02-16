/*
package ui;

import javax.swing.*;
import java.awt.*;

public class VinculoPanel extends JPanel {
    public VinculoPanel() {
        setLayout(new BorderLayout());
        JLabel label = new JLabel("Vinculos direito_acesso (Integrado ao schema real)", SwingConstants.CENTER);
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
import java.util.HashMap;
import java.util.Map;

public class VinculoPanel extends JPanel {

    private JComboBox<String> cbUsuario, cbIp, cbPermissao;
    private JButton btnVincular, btnExcluir;
    private JTable tabelaVinculos;
    private DefaultTableModel model;

    // Mapas para armazenar o ID real de cada nome exibido no ComboBox
    private Map<String, Integer> mapUsuarios = new HashMap<>();
    private Map<String, Integer> mapIps = new HashMap<>();
    private Map<String, Integer> mapPermissoes = new HashMap<>();

    public VinculoPanel() {
        setLayout(new BorderLayout(10, 10));

        // --- PAINEL DE CADASTRO (NORTE) ---
        JPanel pnlForm = new JPanel(new GridLayout(4, 2, 5, 5));
        pnlForm.setBorder(BorderFactory.createTitledBorder("Novo Vínculo de Acesso"));

        cbUsuario = new JComboBox<>();
        cbIp = new JComboBox<>();
        cbPermissao = new JComboBox<>();
        btnVincular = new JButton("Vincular Acesso");
        btnVincular.setBackground(new Color(0, 102, 204));
        btnVincular.setForeground(Color.WHITE);

        pnlForm.add(new JLabel("Selecione o Usuário:"));
        pnlForm.add(cbUsuario);
        pnlForm.add(new JLabel("Selecione o IP/Host:"));
        pnlForm.add(cbIp);
        pnlForm.add(new JLabel("Selecione a Permissão:"));
        pnlForm.add(cbPermissao);
        pnlForm.add(new JLabel(""));
        pnlForm.add(btnVincular);

        add(pnlForm, BorderLayout.NORTH);

        // --- PAINEL DE LISTAGEM (CENTRO) ---
        String[] colunas = {"ID", "Usuário", "IP/Host", "Permissão", "Escopo"};
        model = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tabelaVinculos = new JTable(model);

        JPanel pnlCentro = new JPanel(new BorderLayout());
        btnExcluir = new JButton("Remover Vínculo Selecionado");
        pnlCentro.add(new JScrollPane(tabelaVinculos), BorderLayout.CENTER);
        pnlCentro.add(btnExcluir, BorderLayout.SOUTH);

        add(pnlCentro, BorderLayout.CENTER);

        configurarEventos();
        atualizarTodosOsCombos();
        carregarTabela();
    }

    private void atualizarTodosOsCombos() {
        carregarCombo("SELECT ID, USERNAME FROM usuarios", cbUsuario, mapUsuarios, "USERNAME");
        carregarCombo("SELECT ID, ENDERECO_IP FROM enderecosip", cbIp, mapIps, "ENDERECO_IP");
        carregarCombo("SELECT ID, CONCAT(PERMISSAO, ' (', EXTRA, ')') as NOME FROM permissoes", cbPermissao, mapPermissoes, "NOME");
    }

    private void carregarCombo(String sql, JComboBox<String> combo, Map<String, Integer> mapa, String colunaNome) {
        combo.removeAllItems();
        mapa.clear();
        try (Connection conn = ConnectionFactory.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                String nome = rs.getString(colunaNome);
                int id = rs.getInt("ID");
                combo.addItem(nome);
                mapa.put(nome, id);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void configurarEventos() {
        btnVincular.addActionListener(e -> {
            Integer idUser = mapUsuarios.get(cbUsuario.getSelectedItem());
            Integer idIp = mapIps.get(cbIp.getSelectedItem());
            Integer idPerm = mapPermissoes.get(cbPermissao.getSelectedItem());

            if (idUser == null || idIp == null || idPerm == null) {
                JOptionPane.showMessageDialog(this, "Selecione todos os campos!");
                return;
            }

            String sql = "INSERT INTO direito_acesso (ID_USER, ID_IP, ID_GRANT) VALUES (?, ?, ?)";
            try (Connection conn = ConnectionFactory.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, idUser);
                stmt.setInt(2, idIp);
                stmt.setInt(3, idPerm);
                stmt.executeUpdate();
                carregarTabela();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Vínculo já existe ou erro no banco!");
            }
        });

        btnExcluir.addActionListener(e -> {
            int row = tabelaVinculos.getSelectedRow();
            if (row == -1) return;
            Object id = model.getValueAt(row, 0);
            try (Connection conn = ConnectionFactory.getConnection();
                 PreparedStatement stmt = conn.prepareStatement("DELETE FROM direito_acesso WHERE ID = ?")) {
                stmt.setObject(1, id);
                stmt.executeUpdate();
                carregarTabela();
            } catch (SQLException ex) { ex.printStackTrace(); }
        });
    }

    public void carregarTabela() {
        model.setRowCount(0);
        String sql = "SELECT da.ID, u.USERNAME, i.ENDERECO_IP, p.PERMISSAO, p.EXTRA " +
                "FROM direito_acesso da " +
                "JOIN usuarios u ON da.ID_USER = u.ID " +
                "JOIN enderecosip i ON da.ID_IP = i.ID " +
                "JOIN permissoes p ON da.ID_GRANT = p.ID";

        try (Connection conn = ConnectionFactory.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("ID"), rs.getString("USERNAME"),
                        rs.getString("ENDERECO_IP"), rs.getString("PERMISSAO"), rs.getString("EXTRA")
                });
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }
}

