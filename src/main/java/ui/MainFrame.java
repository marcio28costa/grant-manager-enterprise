package ui;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {

    public MainFrame() {
        setTitle("Grant Manager Enterprise - marcio28costa 1.0 ");
        setSize(1100, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Usuários", new UsuarioCrudPanel());
        tabs.addTab("IPs/Hosts", new IpCrudPanel());
        tabs.addTab("Permissões", new PermissaoCrudPanel());
        tabs.addTab("Vínculos", new VinculoPanel());
        tabs.addTab("Gerar Script", new ScriptPanel());

        add(tabs, BorderLayout.CENTER);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainFrame().setVisible(true));
    }
}
