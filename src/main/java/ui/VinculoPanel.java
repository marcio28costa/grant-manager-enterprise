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
/*
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

    private JComboBox<String> cbUsuario, cbIp, cbPermissao, cbTipoIp;
    private JRadioButton rbPorIp, rbPorTipo;
    private JButton btnVincular, btnExcluir, btnAtualizar;
    private JTable tabelaVinculos;
    private DefaultTableModel model;

    private Map<String, Integer> mapUsuarios = new HashMap<>();
    private Map<String, Integer> mapIps = new HashMap<>();
    private Map<String, Integer> mapPermissoes = new HashMap<>();

    public VinculoPanel() {
        setLayout(new BorderLayout(10, 10));

        // --- PAINEL DE CADASTRO (NORTE) ---
        JPanel pnlForm = new JPanel(new GridBagLayout());
        pnlForm.setBorder(BorderFactory.createTitledBorder("Configurar Vínculos (Provisionamento)"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Seleção de Usuário
        gbc.gridx = 0; gbc.gridy = 0; pnlForm.add(new JLabel("Usuário:"), gbc);
        cbUsuario = new JComboBox<>();
        gbc.gridx = 1; pnlForm.add(cbUsuario, gbc);

        // Modo de Seleção de Host
        gbc.gridx = 0; gbc.gridy = 1; pnlForm.add(new JLabel("Modo Host:"), gbc);
        JPanel pnlModo = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        rbPorIp = new JRadioButton("Por Host/Grant", true);
        rbPorTipo = new JRadioButton("Por Tipo de IP");
        ButtonGroup grupo = new ButtonGroup();
        grupo.add(rbPorIp); grupo.add(rbPorTipo);
        pnlModo.add(rbPorIp); pnlModo.add(rbPorTipo);
        gbc.gridx = 1; pnlForm.add(pnlModo, gbc);

        // Combos de IP e Tipo
        gbc.gridx = 0; gbc.gridy = 2; pnlForm.add(new JLabel("Selecione:"), gbc);
        cbIp = new JComboBox<>();
        cbTipoIp = new JComboBox<>(new String[]{"CITEL", "DNS", "INTERNO", "CLIENTE"}); // Tipos fixos ou do banco
        cbTipoIp.setEnabled(false);

        JPanel pnlIpSelect = new JPanel(new CardLayout());
        pnlIpSelect.add(cbIp, "IP");
        pnlIpSelect.add(cbTipoIp, "TIPO");
        gbc.gridx = 1; pnlForm.add(pnlIpSelect, gbc);

        // Permissão
        gbc.gridx = 0; gbc.gridy = 3; pnlForm.add(new JLabel("Permissão:"), gbc);
        cbPermissao = new JComboBox<>();
        gbc.gridx = 1; pnlForm.add(cbPermissao, gbc);

        // Botão Vincular
        btnVincular = new JButton("Vincular Acesso");
        btnVincular.setBackground(new Color(0, 102, 204));
        btnVincular.setForeground(Color.WHITE);
        gbc.gridx = 1; gbc.gridy = 4; pnlForm.add(btnVincular, gbc);

        add(pnlForm, BorderLayout.NORTH);

        // --- TABELA E EXCLUSÃO ---
        model = new DefaultTableModel(new String[]{"ID", "Usuário", "Host/Grant", "Permissão", "Escopo"}, 0);
        tabelaVinculos = new JTable(model);
        btnExcluir = new JButton("Remover Vínculo Selecionado");
        btnAtualizar = new JButton("🔄 Atualizar Listas");

        JPanel pnlSul = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        pnlSul.add(btnAtualizar);
        pnlSul.add(btnExcluir);

        add(new JScrollPane(tabelaVinculos), BorderLayout.CENTER);
        add(pnlSul, BorderLayout.SOUTH);

        configurarEventos(pnlIpSelect);
        atualizarTodosOsCombos();
        carregarTabela();
    }

    private void configurarEventos(JPanel pnlIpSelect) {
        CardLayout cl = (CardLayout) pnlIpSelect.getLayout();

        rbPorIp.addActionListener(e -> { cl.show(pnlIpSelect, "IP"); cbIp.setEnabled(true); cbTipoIp.setEnabled(false); });
        rbPorTipo.addActionListener(e -> { cl.show(pnlIpSelect, "TIPO"); cbIp.setEnabled(false); cbTipoIp.setEnabled(true); });

        btnAtualizar.addActionListener(e -> atualizarTodosOsCombos());

        btnVincular.addActionListener(e -> realizarVinculo());

        btnExcluir.addActionListener(e -> {
            int row = tabelaVinculos.getSelectedRow();
            if (row == -1) return;
            try (Connection conn = ConnectionFactory.getConnection();
                 PreparedStatement stmt = conn.prepareStatement("DELETE FROM direito_acesso WHERE ID = ?")) {
                stmt.setObject(1, model.getValueAt(row, 0));
                stmt.executeUpdate();
                carregarTabela();
            } catch (SQLException ex) { ex.printStackTrace(); }
        });
    }

    private void realizarVinculo() {
        Integer idUser = mapUsuarios.get(cbUsuario.getSelectedItem());
        Integer idPerm = mapPermissoes.get(cbPermissao.getSelectedItem());

        if (idUser == null || idPerm == null) {
            JOptionPane.showMessageDialog(this, "Selecione usuário e permissão!");
            return;
        }

        try (Connection conn = ConnectionFactory.getConnection()) {
            if (rbPorIp.isSelected()) {
                Integer idIp = mapIps.get(cbIp.getSelectedItem());
                vincular(conn, idUser, idIp, idPerm);
            } else {
                // Lógica por TIPO: busca todos os IDs que pertencem ao tipo selecionado e tem GRANT não nulo
                String sqlBusca = "SELECT ID FROM enderecosip WHERE TIPO = ? AND ENDERECO_GRANT IS NOT NULL";
                try (PreparedStatement ps = conn.prepareStatement(sqlBusca)) {
                    ps.setString(1, cbTipoIp.getSelectedItem().toString());
                    ResultSet rs = ps.executeQuery();
                    while (rs.next()) {
                        vincular(conn, idUser, rs.getInt("ID"), idPerm);
                    }
                }
            }
            carregarTabela();
            JOptionPane.showMessageDialog(this, "Vínculos processados!");
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erro: " + ex.getMessage());
        }
    }

    private void vincular(Connection conn, int user, int ip, int perm) throws SQLException {
        String sql = "INSERT IGNORE INTO direito_acesso (ID_USER, ID_IP, ID_GRANT) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, user);
            stmt.setInt(2, ip);
            stmt.setInt(3, perm);
            stmt.executeUpdate();
        }
    }

    private void atualizarTodosOsCombos() {
        carregarCombo("SELECT ID, USERNAME FROM usuarios ORDER BY USERNAME", cbUsuario, mapUsuarios, "USERNAME");
        // Filtro solicitado: Apenas onde ENDERECO_GRANT não for null
        carregarCombo("SELECT ID, CONCAT(ENDERECO_IP, ' -> ', ENDERECO_GRANT) as DISPLAY, ENDERECO_GRANT FROM enderecosip WHERE ENDERECO_GRANT IS NOT NULL", cbIp, mapIps, "DISPLAY");
        carregarCombo("SELECT ID, CONCAT(PERMISSAO, ' (', EXTRA, ')') as NOME FROM permissoes", cbPermissao, mapPermissoes, "NOME");
    }

    private void carregarCombo(String sql, JComboBox<String> combo, Map<String, Integer> mapa, String colunaNome) {
        combo.removeAllItems();
        mapa.clear();
        try (Connection conn = ConnectionFactory.getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                String nome = rs.getString(colunaNome);
                mapa.put(nome, rs.getInt("ID"));
                combo.addItem(nome);
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public void carregarTabela() {
        model.setRowCount(0);
        String sql = "SELECT da.ID, u.USERNAME, i.ENDERECO_GRANT, p.PERMISSAO, p.EXTRA " +
                "FROM direito_acesso da " +
                "JOIN usuarios u ON da.ID_USER = u.ID " +
                "JOIN enderecosip i ON da.ID_IP = i.ID " +
                "JOIN permissoes p ON da.ID_GRANT = p.ID " +
                "ORDER BY da.ID DESC";
        try (Connection conn = ConnectionFactory.getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                model.addRow(new Object[]{rs.getInt("ID"), rs.getString("USERNAME"), rs.getString("ENDERECO_GRANT"), rs.getString("PERMISSAO"), rs.getString("EXTRA")});
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }
}