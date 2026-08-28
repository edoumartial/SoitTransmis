package com.soittransmis.views;

import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class ModifierAffaireDialog extends JDialog {

    private JTextField txtCommune, txtLieuDit, txtSection, txtParcelle;
    private JTextArea txtDescription;
    private JComboBox<String> comboStatut;
    
    public boolean affaireModifiee = false;
    private String numeroAffaireCible;
    private String nomUtilisateurConnecte;

    private static final String DB_URL = "jdbc:postgresql://localhost:5432/soit_transmis_db";
    private static final String DB_USER = "postgres";
    private static final String DB_PASSWORD = "postgres";

    public ModifierAffaireDialog(Frame parent, String numeroAffaire, String nomUtilisateur) {
        super(parent, "Modifier l'affaire : " + numeroAffaire, true);
        this.numeroAffaireCible = numeroAffaire;
        this.nomUtilisateurConnecte = nomUtilisateur;

        setSize(500, 580);
        setLocationRelativeTo(parent);
        setResizable(false);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.setBackground(new Color(245, 247, 250));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 0, 6, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.weightx = 1.0;

        int row = 0;

        // Affichage du numéro (non modifiable)
        gbc.gridy = row++;
        panel.add(new JLabel("Numéro d'affaire (Fixe) :"), gbc);
        gbc.gridy = row++;
        JTextField txtNumFixe = new JTextField(numeroAffaire);
        txtNumFixe.setEditable(false);
        txtNumFixe.setBackground(new Color(230, 230, 230));
        txtNumFixe.setPreferredSize(new Dimension(0, 30));
        panel.add(txtNumFixe, gbc);

        gbc.gridy = row++;
        panel.add(new JLabel("Commune :"), gbc);
        gbc.gridy = row++;
        txtCommune = new JTextField();
        txtCommune.setPreferredSize(new Dimension(0, 30));
        panel.add(txtCommune, gbc);

        // Ajout du champ Lieu-dit
        gbc.gridy = row++;
        panel.add(new JLabel("Lieu-dit :"), gbc);
        gbc.gridy = row++;
        txtLieuDit = new JTextField();
        txtLieuDit.setPreferredSize(new Dimension(0, 30));
        panel.add(txtLieuDit, gbc);

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
        panel.add(new JLabel("Statut :"), gbc);
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

        // Charger les données actuelles depuis la base
        chargerDonneesActuelles();

        // Boutons
        gbc.gridy = row++;
        JPanel panelBoutons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panelBoutons.setOpaque(false);

        JButton btnAnnuler = new JButton("Annuler");
        JButton btnEnregistrer = new JButton("Mettre à jour");
        btnEnregistrer.setBackground(new Color(0, 123, 255));
        btnEnregistrer.setForeground(Color.WHITE);
        btnEnregistrer.setOpaque(true);
        btnEnregistrer.setBorderPainted(false);

        btnAnnuler.addActionListener(e -> dispose());
        btnEnregistrer.addActionListener(e -> mettreAJourAffaire());

        panelBoutons.add(btnAnnuler);
        panelBoutons.add(btnEnregistrer);
        panel.add(panelBoutons, gbc);

        add(panel);
    }

    private void chargerDonneesActuelles() {
        String query = "SELECT a.statut, a.description, o.ville, o.lieu_dit, o.section, o.parcelle " +
                       "FROM affaires a " +
                       "LEFT JOIN opposants o ON a.id = o.affaire_id " +
                       "WHERE a.numero_affaire = ? LIMIT 1";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, numeroAffaireCible);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    comboStatut.setSelectedItem(rs.getString("statut"));
                    txtDescription.setText(rs.getString("description"));
                    txtCommune.setText(rs.getString("ville"));
                    txtLieuDit.setText(rs.getString("lieu_dit"));
                    txtSection.setText(rs.getString("section"));
                    txtParcelle.setText(rs.getString("parcelle"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void mettreAJourAffaire() {
        String commune = txtCommune.getText().trim();
        String lieuDit = txtLieuDit.getText().trim();
        String section = txtSection.getText().trim();
        String parcelle = txtParcelle.getText().trim();
        String statut = (String) comboStatut.getSelectedItem();
        String description = txtDescription.getText().trim();

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            // 1. Mise à jour de la table affaires
            String updateAffaire = "UPDATE affaires SET statut = ?, description = ?, modifie_par = ? WHERE numero_affaire = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(updateAffaire)) {
                pstmt.setString(1, statut);
                pstmt.setString(2, description);
                pstmt.setString(3, nomUtilisateurConnecte);
                pstmt.setString(4, numeroAffaireCible);
                pstmt.executeUpdate();
            }

            // 2. Mise à jour des infos géographiques (table opposants) incluant le lieu_dit
            String updateOpposant = "UPDATE opposants SET ville = ?, lieu_dit = ?, section = ?, parcelle = ?, modifie_par = ? " +
                                     "WHERE affaire_id = (SELECT id FROM affaires WHERE numero_affaire = ?) " +
                                     "AND (nom_prenom_ou_raison_sociale = 'Dossier Principal / Requérant initial' OR nom_prenom_ou_raison_sociale IS NULL)";
            try (PreparedStatement pstmtOpp = conn.prepareStatement(updateOpposant)) {
                pstmtOpp.setString(1, commune);
                pstmtOpp.setString(2, lieuDit.isEmpty() ? null : lieuDit);
                pstmtOpp.setString(3, section);
                pstmtOpp.setString(4, parcelle);
                pstmtOpp.setString(5, nomUtilisateurConnecte);
                pstmtOpp.setString(6, numeroAffaireCible);
                pstmtOpp.executeUpdate();
            }

            affaireModifiee = true;
            JOptionPane.showMessageDialog(this, "Affaire mise à jour avec succès !", "Succès", JOptionPane.INFORMATION_MESSAGE);
            dispose();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erreur lors de la mise à jour :\n" + e.getMessage(), "Erreur SQL", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
}