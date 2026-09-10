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
    private JButton btnAffaireDoc;
    private JButton btnOpp1Doc;
    private JButton btnOpp2Doc;
    private DashboardFrame frame;
    private String currentNumeroAffaire;

    public ButtonEditor(DashboardFrame frame, ImageIcon folderIcon, JTable tableAffaires, DefaultTableModel tableModel) {
        this.frame = frame;
        panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
        panel.setOpaque(true);
        panel.setBackground(new Color(245, 245, 245));

        // 1. Dossier de l'Affaire (Lettre d'opposition & Dossier de litige)
        btnAffaireDoc = createFolderButton(folderIcon, "Documents de l'Affaire");
        // 2. Dossier de l'Opposant 1
        btnOpp1Doc = createFolderButton(folderIcon, "Dossier Opposant 1");
        // 3. Dossier de l'Opposant 2
        btnOpp2Doc = createFolderButton(folderIcon, "Dossier Opposant 2");

        panel.add(btnAffaireDoc);
        panel.add(btnOpp1Doc);
        panel.add(btnOpp2Doc);

        // Actions au clic
        btnAffaireDoc.addMouseListener(new MouseAdapter() {
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

        btnOpp1Doc.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = tableAffaires.getSelectedRow();
                if (row != -1) {
                    currentNumeroAffaire = (String) tableModel.getValueAt(row, 0);
                    SwingUtilities.invokeLater(() -> frame.gererDossierProtagoniste(currentNumeroAffaire, 1));
                    fireEditingCanceled();
                }
            }
        });

        btnOpp2Doc.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = tableAffaires.getSelectedRow();
                if (row != -1) {
                    currentNumeroAffaire = (String) tableModel.getValueAt(row, 0);
                    SwingUtilities.invokeLater(() -> frame.gererDossierProtagoniste(currentNumeroAffaire, 2));
                    fireEditingCanceled();
                }
            }
        });
    }

    private JButton createFolderButton(ImageIcon icon, String tooltip) {
        JButton btn = new JButton();
        if (icon != null) {
            btn.setIcon(icon);
            btn.setText("");
        } else {
            btn.setText("📁");
        }
        btn.setPreferredSize(new Dimension(26, 26));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setToolTipText(tooltip);
        return btn;
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