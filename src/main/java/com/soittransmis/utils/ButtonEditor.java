package com.soittransmis.utils;

import com.soittransmis.views.DashboardFrame;
import javax.swing.*;
import javax.swing.table.TableCellEditor;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class ButtonEditor extends AbstractCellEditor implements TableCellEditor {
    private JPanel panel;
    private JButton button;
    private DashboardFrame frame;
    private String currentNumeroAffaire;

    public ButtonEditor(DashboardFrame frame, ImageIcon folderIcon, JTable tableAffaires, DefaultTableModel tableModel) {
        this.frame = frame;
        panel = new JPanel(new GridBagLayout());
        panel.setOpaque(true);
        panel.setBackground(new Color(245, 245, 245));

        button = new JButton();
        if (folderIcon != null) {
            button.setIcon(folderIcon);
            button.setText("");
        } else {
            button.setText("📂");
        }
        button.setPreferredSize(new Dimension(30, 30));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);

        panel.add(button);

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = tableAffaires.getSelectedRow();
                if (row != -1) {
                    currentNumeroAffaire = (String) tableModel.getValueAt(row, 0);
                    
                    if (e.getClickCount() == 1) {
                        SwingUtilities.invokeLater(() -> frame.voirDocumentsAffaire(currentNumeroAffaire));
                        fireEditingCanceled();
                    } else if (e.getClickCount() == 2) {
                        SwingUtilities.invokeLater(() -> frame.gererUploadFichier(currentNumeroAffaire));
                        fireEditingCanceled();
                    }
                }
            }
        });
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        return panel;
    }

    @Override
    public Object getCellEditorValue() {
        return "";
    }
}