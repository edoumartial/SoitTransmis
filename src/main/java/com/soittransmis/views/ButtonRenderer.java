package com.soittransmis.views;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

public class ButtonRenderer extends JButton implements TableCellRenderer {
    private ImageIcon folderIcon;

    public ButtonRenderer(ImageIcon folderIcon) {
        this.folderIcon = folderIcon;
        setOpaque(true);
        setBackground(new Color(245, 245, 245));
        setHorizontalAlignment(SwingConstants.CENTER);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        if (folderIcon != null) {
            setIcon(folderIcon);
            setText(""); 
        } else {
            setText("📂"); 
        }
        return this;
    }
}