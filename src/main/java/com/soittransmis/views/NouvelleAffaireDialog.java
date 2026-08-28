package com.soittransmis.views;

import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class NouvelleAffaireDialog extends JDialog {

    private JTextField txtNumeroAffaire, txtCommune, txtSection, txtParcelle;
    private JTextArea txtDescription;
    private JComboBox<String> comboStatut;
    
    public boolean affaireCreee = false;
    private String nomUtilisateurConnecte;

    private static final String DB_URL = "jdbc:postgresql://localhost:5432/soit_transmis_db";
    private static final String DB_USER = "postgres";
    private static final String DB_PASSWORD = "postgres";

    public NouvelleAffaireDialog(Frame parent, String nomUtilisateur) {
        super(parent, "Créer une nouvelle affaire", true);
        this.nomUtilisateurConnecte = nomUtilisateur;

        setSize(500, 550);
        setLocationRelativeTo(parent);
        setResizable(false);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.setBackground(new Color(245, 247, 250));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 0, 8, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.weightx = 1.0;

        int row = 0;

        gbc.gridy = row++;
        panel.add(new JLabel("Numéro d'affaire (Généré automatiquement) :"), gbc);
        gbc.gridy = row++;
        txtNumeroAffaire = new JTextField();
        txtNumeroAffaire.setPreferredSize(new Dimension(0, 30));
        txtNumeroAffaire.setEditable(false); // Empêche la saisie manuelle
        txtNumeroAffaire.setBackground(new Color(230, 230, 230)); // Grise le champ pour indiquer qu'il est automatique
        panel.add(txtNumeroAffaire, gbc);

        // Génération et affichage du prochain numéro d'affaire
        genererProchainNumeroAffaire();

        gbc.gridy = row++;
        panel.add(new JLabel("Commune :"), gbc);
        gbc.gridy = row++;
        txtCommune = new JTextField();
        txtCommune.setPreferredSize(new Dimension(0, 30));
        panel.add(txtCommune, gbc);

        gbc.gridy = row++;
        panel.add(new JLabel("Section :"), gbc);
        gbc.gridy = row++;
        txtSection = new JTextField();
        txtSection.setPreferredSize(new Dimension(0, 30));
        panel.add(txtSection, gbc);

        gbc.gridy = row++;
        panel.add(new JLabel("Parcelle :"), gbc);
        gbc.gridy = row++;
        txtParcelle = new JTextField();
        txtParcelle.setPreferredSize(new Dimension(0, 30));
        panel.add(txtParcelle, gbc);

        gbc.gridy = row++;
        panel.add(new JLabel("Statut initial :"), gbc);
        gbc.gridy = row++;
        comboStatut = new JComboBox<>(new String[]{"En cours", "Traité et classé"});
        comboStatut.setPreferredSize(new Dimension(0, 30));
        panel.add(comboStatut, gbc);

        gbc.gridy = row++;
        panel.add(new JLabel("Description / Observations :"), gbc);
        gbc.gridy = row++;
        txtDescription = new JTextArea(3, 20);
        txtDescription.setLineWrap(true);
        panel.add(new JScrollPane(txtDescription), gbc);

        // Boutons
        gbc.gridy = row++;
        JPanel panelBoutons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panelBoutons.setOpaque(false);

        JButton btnAnnuler = new JButton("Annuler");
        JButton btnEnregistrer = new JButton("Enregistrer l'affaire");
        btnEnregistrer.setBackground(new Color(40, 167, 69));
        btnEnregistrer.setForeground(Color.WHITE);
        btnEnregistrer.setOpaque(true);
        btnEnregistrer.setBorderPainted(false);

        btnAnnuler.addActionListener(e -> dispose());
        btnEnregistrer.addActionListener(e -> enregistrerAffaire());

        panelBoutons.add(btnAnnuler);
        panelBoutons.add(btnEnregistrer);
        panel.add(panelBoutons, gbc);

        add(panel);
    }

    /**
     * Méthode pour générer automatiquement le prochain numéro d'affaire.
     * Suppose un format basé sur un entier ou une séquence (ex: convertit le max en entier et ajoute 1, ou utilise une valeur par défaut "AFF-001").
     */
    private void genererProchainNumeroAffaire() {
        String query = "SELECT numero_affaire FROM affaires ORDER BY id DESC LIMIT 1";
        
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            if (rs.next()) {
                String dernierNum = rs.getString("numero_affaire");
                try {
                    // Si le numéro est purement numérique ou contient un nombre extractible
                    int prochainNum = Integer.parseInt(dernierNum.replaceAll("\\D+", "")) + 1;
                    txtNumeroAffaire.setText(String.format("AFF-%03d", prochainNum)); // Format ex: AFF-005
                } catch (NumberFormatException e) {
                    // Fallback si le format textuel est complexe
                    txtNumeroAffaire.setText("AFF-" + System.currentTimeMillis());
                }
            } else {
                // Première affaire de la base de données
                txtNumeroAffaire.setText("AFF-001");
            }
        } catch (SQLException e) {
            // En cas d'erreur SQL, valeur de secours
            txtNumeroAffaire.setText("AFF-001");
            e.printStackTrace();
        }
    }

    private void enregistrerAffaire() {
        String numAffaire = txtNumeroAffaire.getText().trim();
        String commune = txtCommune.getText().trim();
        String section = txtSection.getText().trim();
        String parcelle = txtParcelle.getText().trim();
        String statut = (String) comboStatut.getSelectedItem();
        String description = txtDescription.getText().trim();

        if (numAffaire.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Le numéro d'affaire est obligatoire.", "Champ requis", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            
            // 1. Récupérer l'ID numérique de l'utilisateur connecté (basé sur son nom ou son identifiant)
            int utilisateurId = -1;
            String queryUser = "SELECT id FROM utilisateurs WHERE nom = ? OR identifiant = ?";
            try (PreparedStatement pstmtUser = conn.prepareStatement(queryUser)) {
                pstmtUser.setString(1, nomUtilisateurConnecte);
                pstmtUser.setString(2, nomUtilisateurConnecte);
                try (ResultSet rsUser = pstmtUser.executeQuery()) {
                    if (rsUser.next()) {
                        utilisateurId = rsUser.getInt("id");
                    }
                }
            }

            // Si l'utilisateur n'est pas trouvé, on bloque l'enregistrement par sécurité
            if (utilisateurId == -1) {
                JOptionPane.showMessageDialog(this, "Erreur : Impossible d'identifier l'utilisateur connecté dans la base de données.", "Erreur d'authentification", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // 2. Insérer l'affaire avec l'ID de l'utilisateur (entier) dans 'cree_par'
            String queryAffaire = "INSERT INTO affaires (numero_affaire, statut, description, cree_par) VALUES (?, ?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(queryAffaire)) {
                pstmt.setString(1, numAffaire);
                pstmt.setString(2, statut);
                pstmt.setString(3, description);
                pstmt.setInt(4, utilisateurId);
                pstmt.executeUpdate();
            }

            // 3. Insérer le requérant initial dans la table 'opposants' si des champs sont remplis
            if (!commune.isEmpty() || !section.isEmpty() || !parcelle.isEmpty()) {
                String getIdAffaire = "SELECT id FROM affaires WHERE numero_affaire = ?";
                int affaireId = -1;
                try (PreparedStatement pstmtId = conn.prepareStatement(getIdAffaire)) {
                    pstmtId.setString(1, numAffaire);
                    try (ResultSet rs = pstmtId.executeQuery()) {
                        if (rs.next()) {
                            affaireId = rs.getInt("id");
                        }
                    }
                }

                if (affaireId != -1) {
                    // Note: Dans votre structure SQL, la colonne 'cree_par' de 'opposants' est de type VARCHAR. 
                    // Vous pouvez y insérer le nom ou l'ID sous forme de texte selon votre préférence.
                    String queryOpposantInitial = "INSERT INTO opposants (affaire_id, ville, section, parcelle, nom_prenom_ou_raison_sociale, cree_par) VALUES (?, ?, ?, ?, ?, ?)";
                    try (PreparedStatement pstmtOpp = conn.prepareStatement(queryOpposantInitial)) {
                        pstmtOpp.setInt(1, affaireId);
                        pstmtOpp.setString(2, commune);
                        pstmtOpp.setString(3, section);
                        pstmtOpp.setString(4, parcelle);
                        pstmtOpp.setString(5, "Dossier Principal / Requérant initial");
                        pstmtOpp.setString(6, nomUtilisateurConnecte);
                        pstmtOpp.executeUpdate();
                    }
                }
            }

            affaireCreee = true;
            JOptionPane.showMessageDialog(this, "Affaire créée avec succès !", "Succès", JOptionPane.INFORMATION_MESSAGE);
            dispose();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erreur lors de l'enregistrement de l'affaire :\n" + e.getMessage(), "Erreur SQL", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
}