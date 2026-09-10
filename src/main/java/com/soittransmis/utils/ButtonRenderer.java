package com.soittransmis.utils;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

public class ButtonRenderer extends JPanel implements TableCellRenderer {
    private ImageIcon folderIcon;

    public ButtonRenderer(ImageIcon folderIcon) {
        this.folderIcon = folderIcon;
        setLayout(new FlowLayout(FlowLayout.CENTER, 4, 0));
        setOpaque(true);
        setBackground(new Color(245, 245, 245));

        add(createMiniButton(folderIcon));
        add(createMiniButton(folderIcon));
        add(createMiniButton(folderIcon));
    }

    private JButton createMiniButton(ImageIcon icon) {
        JButton btn = new JButton();
        if (icon != null) {
            btn.setIcon(icon);
        } else {
            btn.setText("📁");
        }
        btn.setPreferredSize(new Dimension(26, 26));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        return btn;
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        return this;
    }
}