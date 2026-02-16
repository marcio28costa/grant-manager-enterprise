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
