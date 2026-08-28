package com.soittransmis.views;

import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class NouvelOpposantDialog extends JDialog {

    private JTextField txtNomOpposant, txtPrenomOpposant, txtVille, txtSection, txtRefDossier;
    private JTextArea txtMotifs;
    private String numeroAffaireCible;

    private static final String DB_URL = "jdbc:postgresql://localhost:5432/soit_transmis_db";
    private static final String DB_USER = "postgres";
    private static final String DB_PASSWORD = "postgres";

    public NouvelOpposantDialog(Frame parent, String numeroAffaire) {
        super(parent, "Ajouter un opposant à l'affaire : " + numeroAffaire, true);
        this.numeroAffaireCible = numeroAffaire;

        setSize(480, 580); // Légèrement agrandi pour le nouveau champ
        setLocationRelativeTo(parent);
        setResizable(false);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.setBackground(new Color(245, 247, 250));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 0, 5, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.weightx = 1.0;

        int row = 0;

        gbc.gridy = row++;
        panel.add(new JLabel("Nom de l'opposant :"), gbc);
        gbc.gridy = row++;
        txtNomOpposant = new JTextField();
        txtNomOpposant.setPreferredSize(new Dimension(0, 30));
        panel.add(txtNomOpposant, gbc);

        gbc.gridy = row++;
        panel.add(new JLabel("Prénom de l'opposant :"), gbc);
        gbc.gridy = row++;
        txtPrenomOpposant = new JTextField();
        txtPrenomOpposant.setPreferredSize(new Dimension(0, 30));
        panel.add(txtPrenomOpposant, gbc);

        // Champ Référence du dossier opposant
        gbc.gridy = row++;
        panel.add(new JLabel("Référence du dossier (ex: REF-007) :"), gbc);
        gbc.gridy = row++;
        txtRefDossier = new JTextField();
        txtRefDossier.setPreferredSize(new Dimension(0, 30));
        panel.add(txtRefDossier, gbc);

        gbc.gridy = row++;
        panel.add(new JLabel("Ville / Commune :"), gbc);
        gbc.gridy = row++;
        txtVille = new JTextField();
        txtVille.setPreferredSize(new Dimension(0, 30));
        panel.add(txtVille, gbc);

        gbc.gridy = row++;
        panel.add(new JLabel("Section / Parcelle :"), gbc);
        gbc.gridy = row++;
        txtSection = new JTextField();
        txtSection.setPreferredSize(new Dimension(0, 30));
        panel.add(txtSection, gbc);

        gbc.gridy = row++;
        panel.add(new JLabel("Motifs du litige :"), gbc);
        gbc.gridy = row++;
        txtMotifs = new JTextArea(2, 20);
        txtMotifs.setLineWrap(true);
        panel.add(new JScrollPane(txtMotifs), gbc);

        // Boutons
        gbc.gridy = row++;
        JPanel panelBoutons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panelBoutons.setOpaque(false);

        JButton btnFermer = new JButton("Terminer");
        JButton btnAjouter = new JButton("Enregistrer l'opposant");
        btnAjouter.setBackground(new Color(0, 123, 255));
        btnAjouter.setForeground(Color.WHITE);
        btnAjouter.setOpaque(true);
        btnAjouter.setBorderPainted(false);

        btnFermer.addActionListener(e -> dispose());
        btnAjouter.addActionListener(e -> enregistrerOpposantEtContinuer());

        panelBoutons.add(btnFermer);
        panelBoutons.add(btnAjouter);
        panel.add(panelBoutons, gbc);

        add(panel);
    }

    private void enregistrerOpposantEtContinuer() {
        String nom = txtNomOpposant.getText().trim();
        String prenom = txtPrenomOpposant.getText().trim();
        String refDossier = txtRefDossier.getText().trim();
        String ville = txtVille.getText().trim();
        String section = txtSection.getText().trim();
        String motifs = txtMotifs.getText().trim();

        if (nom.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Le nom de l'opposant est obligatoire.", "Champ requis", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String nomComplet = nom + (prenom.isEmpty() ? "" : " " + prenom);

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String getAffaireId = "SELECT id FROM affaires WHERE numero_affaire = ?";
            int affaireId = -1;
            try (PreparedStatement pstmt = conn.prepareStatement(getAffaireId)) {
                pstmt.setString(1, numeroAffaireCible);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        affaireId = rs.getInt("id");
                    }
                }
            }

            if (affaireId != -1) {
                // Insertion incluant la colonne ref_dossier
                String insertOpposant = "INSERT INTO opposants (affaire_id, nom_prenom_ou_raison_sociale, ref_dossier, ville, section) VALUES (?, ?, ?, ?, ?)";
                try (PreparedStatement pstmt = conn.prepareStatement(insertOpposant)) {
                    pstmt.setInt(1, affaireId);
                    pstmt.setString(2, nomComplet);
                    pstmt.setString(3, refDossier.isEmpty() ? null : refDossier);
                    pstmt.setString(4, ville);
                    pstmt.setString(5, section);
                    pstmt.executeUpdate();
                }

                JOptionPane.showMessageDialog(this, "Opposant ajouté avec succès !", "Succès", JOptionPane.INFORMATION_MESSAGE);

                int reponse = JOptionPane.showConfirmDialog(
                    this, 
                    "Voulez-vous ajouter un autre opposant pour cette affaire ?", 
                    "Continuer", 
                    JOptionPane.YES_NO_OPTION, 
                    JOptionPane.QUESTION_MESSAGE
                );

                if (reponse == JOptionPane.YES_OPTION) {
                    txtNomOpposant.setText("");
                    txtPrenomOpposant.setText("");
                    txtRefDossier.setText("");
                    txtVille.setText("");
                    txtSection.setText("");
                    txtMotifs.setText("");
                    txtNomOpposant.requestFocusInWindow();
                } else {
                    dispose();
                }
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erreur SQL : " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }
}