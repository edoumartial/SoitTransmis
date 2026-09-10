package com.soittransmis.views;

import javax.swing.*;
import java.awt.*;

public class AffaireCardPanel extends JPanel {
    
    public AffaireCardPanel(String commune, String section, String parcelle, String lieuDit, String date,
                            String nomOpp1, String contactOpp1, String refOpp1,
                            String nomOpp2, String contactOpp2, String refOpp2,
                            ImageIcon folderIcon, DashboardFrame frame, String numeroAffaire) {
        
        setLayout(new BorderLayout(0, 0));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(8, 12, 8, 12),
            BorderFactory.createLineBorder(new Color(218, 224, 233), 1, true)
        ));

        // --- EN-TÊTE DE LA CARTE (Commune, Section, Parcelle, Lieu-dit, Date, Documents) ---
        JPanel headerPanel = new JPanel(new GridLayout(1, 6, 5, 0));
        headerPanel.setBackground(new Color(24, 43, 73)); // Bleu foncé
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

        headerPanel.add(createHeaderCell("Commune", commune));
        headerPanel.add(createHeaderCell("Section", section));
        headerPanel.add(createHeaderCell("Parcelle", parcelle));
        headerPanel.add(createHeaderCell("Lieu-dit", lieuDit));
        headerPanel.add(createHeaderCell("Date", date));
        
        // Panneau des boutons d'action (Documents)
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        actionPanel.setOpaque(false);
        JButton btnAffaire = createFolderButton(folderIcon, "Documents de l'Affaire");
        JButton btnOpp1 = createFolderButton(folderIcon, "Dossier Opposant 1");
        JButton btnOpp2 = createFolderButton(folderIcon, "Dossier Opposant 2");
        
        actionPanel.add(btnAffaire);
        actionPanel.add(btnOpp1);
        actionPanel.add(btnOpp2);
        headerPanel.add(actionPanel);

        add(headerPanel, BorderLayout.NORTH);

        // --- CORPS DE LA CARTE (Opposant 1 & Opposant 2 en deux colonnes) ---
        JPanel bodyPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        bodyPanel.setBackground(Color.WHITE);
        bodyPanel.setBorder(BorderFactory.createEmptyBorder(12, 15, 12, 15));

        // Bloc Opposant 1 (Plaignant)
        JPanel opp1Panel = createProtagonistPanel("Opposant 1 - Plaignant(e)", nomOpp1, contactOpp1, refOpp1);
        // Bloc Opposant 2
        JPanel opp2Panel = createProtagonistPanel("Opposant 2", nomOpp2, contactOpp2, refOpp2);

        bodyPanel.add(opp1Panel);
        bodyPanel.add(opp2Panel);

        add(bodyPanel, BorderLayout.CENTER);

        // --- ACTIONS DES BOUTONS ---
        btnAffaire.addActionListener(e -> frame.voirDocumentsAffaire(numeroAffaire));
        btnOpp1.addActionListener(e -> frame.gererDossierProtagoniste(numeroAffaire, 1));
        btnOpp2.addActionListener(e -> frame.gererDossierProtagoniste(numeroAffaire, 2));
    }

    private JPanel createHeaderCell(String title, String value) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        
        JLabel lblTitle = new JLabel(title);
        lblTitle.setForeground(new Color(170, 185, 205));
        lblTitle.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        
        JLabel lblValue = new JLabel(value != null ? value : "-");
        lblValue.setForeground(Color.WHITE);
        lblValue.setFont(new Font("Segoe UI", Font.BOLD, 13));
        
        panel.add(lblTitle);
        panel.add(lblValue);
        return panel;
    }

    private JPanel createProtagonistPanel(String title, String nom, String contact, String refDossier) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(248, 250, 252));
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(226, 232, 240), 1, true),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTitle.setForeground(new Color(30, 41, 59));
        panel.add(lblTitle);
        panel.add(Box.createVerticalStrut(8));

        panel.add(createFieldRow("Nom Complet", nom));
        panel.add(Box.createVerticalStrut(5));
        panel.add(createFieldRow("Contact", contact));
        panel.add(Box.createVerticalStrut(5));
        panel.add(createFieldRow("Référence Dossier", refDossier));

        return panel;
    }

    private JPanel createFieldRow(String label, String value) {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setOpaque(false);
        
        JLabel lbl = new JLabel(label + " :");
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lbl.setForeground(new Color(100, 116, 139));
        lbl.setPreferredSize(new Dimension(100, 20));
        
        JTextField txtVal = new JTextField(value != null ? value : "");
        txtVal.setEditable(false);
        txtVal.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        txtVal.setBackground(Color.WHITE);
        txtVal.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)));
        
        row.add(lbl, BorderLayout.WEST);
        row.add(txtVal, BorderLayout.CENTER);
        return row;
    }

    private JButton createFolderButton(ImageIcon icon, String tooltip) {
        JButton btn = new JButton();
        if (icon != null) {
            btn.setIcon(icon);
        } else {
            btn.setText("📁");
        }
        btn.setPreferredSize(new Dimension(28, 28));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setToolTipText(tooltip);
        return btn;
    }
}