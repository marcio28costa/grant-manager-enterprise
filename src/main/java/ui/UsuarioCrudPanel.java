/*
package ui;
import javax.swing.*;
import java.awt.*;

public class UsuarioCrudPanel extends JPanel {
    public UsuarioCrudPanel() {
        setLayout(new BorderLayout());
        JLabel label = new JLabel("CRUD Usuarios (Integrado ao schema real)", SwingConstants.CENTER);
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
import java.security.MessageDigest;
import java.sql.*;

public class UsuarioCrudPanel extends JPanel {

    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JComboBox<String> cbTipo;
    private JButton btnSalvar, btnExcluir, btnLimpar;
    private JTable tabelaUsuarios;
    private DefaultTableModel model;

    public UsuarioCrudPanel() {
        setLayout(new BorderLayout(10, 10));

        // --- PAINEL DE FORMULÁRIO (NORTE) ---
        JPanel pnlForm = new JPanel(new GridBagLayout());
        pnlForm.setBorder(BorderFactory.createTitledBorder("Gerenciamento de Usuários"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        txtUsername = new JTextField(15);
        txtPassword = new JPasswordField(15);
        // Baseado no seu ENUM da tabela 'usuarios'
        String[] tipos = {"SISTEMA", "CONSULTA", "INTEGRACAO", "REPLICACAO", "ROBOS", "CEP", "JAVA", "APP", "CITEL", "BI"};
        cbTipo = new JComboBox<>(tipos);

        gbc.gridx = 0; gbc.gridy = 0; pnlForm.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1; pnlForm.add(txtUsername, gbc);

        gbc.gridx = 0; gbc.gridy = 1; pnlForm.add(new JLabel("Senha:"), gbc);
        gbc.gridx = 1; pnlForm.add(txtPassword, gbc);

        gbc.gridx = 0; gbc.gridy = 2; pnlForm.add(new JLabel("Tipo de Perfil:"), gbc);
        gbc.gridx = 1; pnlForm.add(cbTipo, gbc);

        // Painel de Botões
        JPanel pnlBotoes = new JPanel(new FlowLayout(FlowLayout.CENTER));
        btnSalvar = new JButton("Salvar Usuário");
        btnLimpar = new JButton("Limpar");
        btnSalvar.setBackground(new Color(46, 139, 87));
        btnSalvar.setForeground(Color.WHITE);
        pnlBotoes.add(btnSalvar);
        pnlBotoes.add(btnLimpar);

        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        pnlForm.add(pnlBotoes, gbc);

        add(pnlForm, BorderLayout.NORTH);

        // --- PAINEL DE TABELA (CENTRO) ---
        String[] colunas = {"ID", "Username", "Tipo"};
        model = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tabelaUsuarios = new JTable(model);

        JPanel pnlTabela = new JPanel(new BorderLayout());
        btnExcluir = new JButton("Excluir Usuário Selecionado");
        btnExcluir.setForeground(Color.RED);

        pnlTabela.add(new JScrollPane(tabelaUsuarios), BorderLayout.CENTER);
        pnlTabela.add(btnExcluir, BorderLayout.SOUTH);

        add(pnlTabela, BorderLayout.CENTER);

        // --- EVENTOS E CARGA INICIAL ---
        configurarEventos();
        carregarDados();
    }

    private void configurarEventos() {
        // Ação Salvar
        btnSalvar.addActionListener(e -> {
            String user = txtUsername.getText().trim();
            String pass = new String(txtPassword.getPassword());
            String tipo = (String) cbTipo.getSelectedItem();

            if (user.isEmpty() || pass.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Preencha todos os campos!");
                return;
            }

            try (Connection conn = ConnectionFactory.getConnection()) {
                String sql = "INSERT INTO usuarios (USERNAME, PASS_SHA1, TIPO) VALUES (?, SHA1(?), ?)";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setString(1, user);
                stmt.setString(2, pass);
                stmt.setString(3, tipo);

                stmt.executeUpdate();
                JOptionPane.showMessageDialog(this, "Usuário " + user + " cadastrado!");
                limparCampos();
                carregarDados();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Erro: " + ex.getMessage());
            }
        });

        // Ação Excluir
        btnExcluir.addActionListener(e -> {
            int row = tabelaUsuarios.getSelectedRow();
            if (row == -1) return;

            Object id = model.getValueAt(row, 0);
            int confirm = JOptionPane.showConfirmDialog(this, "Excluir ID " + id + "?");
            if (confirm == JOptionPane.YES_OPTION) {
                try (Connection conn = ConnectionFactory.getConnection()) {
                    PreparedStatement stmt = conn.prepareStatement("DELETE FROM usuarios WHERE ID = ?");
                    stmt.setObject(1, id);
                    stmt.executeUpdate();
                    carregarDados();
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(this, "Erro ao excluir: " + ex.getMessage());
                }
            }
        });

        btnLimpar.addActionListener(e -> limparCampos());
    }

    private void limparCampos() {
        txtUsername.setText("");
        txtPassword.setText("");
        cbTipo.setSelectedIndex(0);
        tabelaUsuarios.clearSelection();
    }

    public void carregarDados() {
        model.setRowCount(0);
        try (Connection conn = ConnectionFactory.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT ID, USERNAME, TIPO FROM usuarios ORDER BY USERNAME")) {

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("ID"),
                        rs.getString("USERNAME"),
                        rs.getString("TIPO")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
*/
package ui;

import db.ConnectionFactory;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class UsuarioCrudPanel extends JPanel {

    private JTextField txtUsername;
    private JTextField txtPassword; // Alterado de JPasswordField para JTextField
    private JComboBox<String> cbTipo;
    private JButton btnSalvar, btnExcluir, btnLimpar;
    private JTable tabelaUsuarios;
    private DefaultTableModel model;

    public UsuarioCrudPanel() {
        setLayout(new BorderLayout(10, 10));

        // --- PAINEL DE FORMULÁRIO ---
        JPanel pnlForm = new JPanel(new GridBagLayout());
        pnlForm.setBorder(BorderFactory.createTitledBorder("Gerenciamento de Usuários"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        txtUsername = new JTextField(15);
        txtPassword = new JTextField(15); // Texto visível agora

        String[] tipos = {"SISTEMA", "CONSULTA", "INTEGRACAO", "REPLICACAO", "ROBOS", "CEP", "JAVA", "APP", "CITEL", "BI"};
        cbTipo = new JComboBox<>(tipos);

        gbc.gridx = 0; gbc.gridy = 0; pnlForm.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1; pnlForm.add(txtUsername, gbc);

        gbc.gridx = 0; gbc.gridy = 1; pnlForm.add(new JLabel("Senha (Texto Plano):"), gbc);
        gbc.gridx = 1; pnlForm.add(txtPassword, gbc);

        gbc.gridx = 0; gbc.gridy = 2; pnlForm.add(new JLabel("Tipo de Perfil:"), gbc);
        gbc.gridx = 1; pnlForm.add(cbTipo, gbc);

        // Painel de Botões
        JPanel pnlBotoes = new JPanel(new FlowLayout(FlowLayout.CENTER));
        btnSalvar = new JButton("Salvar Usuário");
        btnLimpar = new JButton("Limpar");
        btnSalvar.setBackground(new Color(46, 139, 87));
        btnSalvar.setForeground(Color.WHITE);
        pnlBotoes.add(btnSalvar);
        pnlBotoes.add(btnLimpar);

        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        pnlForm.add(pnlBotoes, gbc);

        add(pnlForm, BorderLayout.NORTH);

        // --- PAINEL DE TABELA ---
        String[] colunas = {"ID", "Username", "Tipo"};
        model = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tabelaUsuarios = new JTable(model);

        JPanel pnlTabela = new JPanel(new BorderLayout());
        btnExcluir = new JButton("Excluir Usuário Selecionado");
        btnExcluir.setForeground(Color.RED);

        pnlTabela.add(new JScrollPane(tabelaUsuarios), BorderLayout.CENTER);
        pnlTabela.add(btnExcluir, BorderLayout.SOUTH);

        add(pnlTabela, BorderLayout.CENTER);

        configurarEventos();
        carregarDados();
    }

    private void configurarEventos() {
        btnSalvar.addActionListener(e -> {
            String user = txtUsername.getText().trim();
            String pass = txtPassword.getText().trim(); // Pega o texto direto
            String tipo = (String) cbTipo.getSelectedItem();

            if (user.isEmpty() || pass.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Preencha todos os campos!");
                return;
            }

            // Removido o SHA1(?) para salvar a String pura conforme solicitado
            try (Connection conn = ConnectionFactory.getConnection()) {
                String sql = "INSERT INTO usuarios (USERNAME, PASS_SHA1, TIPO) VALUES (?, ?, ?)";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setString(1, user);
                stmt.setString(2, pass); // String pura
                stmt.setString(3, tipo);

                stmt.executeUpdate();
                JOptionPane.showMessageDialog(this, "Usuário " + user + " cadastrado!");
                limparCampos();
                carregarDados();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Erro: " + ex.getMessage());
            }
        });

        btnExcluir.addActionListener(e -> {
            int row = tabelaUsuarios.getSelectedRow();
            if (row == -1) return;
            Object id = model.getValueAt(row, 0);
            if (JOptionPane.showConfirmDialog(this, "Excluir ID " + id + "?") == JOptionPane.YES_OPTION) {
                try (Connection conn = ConnectionFactory.getConnection()) {
                    PreparedStatement stmt = conn.prepareStatement("DELETE FROM usuarios WHERE ID = ?");
                    stmt.setObject(1, id);
                    stmt.executeUpdate();
                    carregarDados();
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(this, "Erro: " + ex.getMessage());
                }
            }
        });

        btnLimpar.addActionListener(e -> limparCampos());
    }

    private void limparCampos() {
        txtUsername.setText("");
        txtPassword.setText("");
        cbTipo.setSelectedIndex(0);
        tabelaUsuarios.clearSelection();
    }

    public void carregarDados() {
        model.setRowCount(0);
        try (Connection conn = ConnectionFactory.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT ID, USERNAME, TIPO FROM usuarios ORDER BY USERNAME")) {
            while (rs.next()) {
                model.addRow(new Object[]{ rs.getInt("ID"), rs.getString("USERNAME"), rs.getString("TIPO") });
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }
}