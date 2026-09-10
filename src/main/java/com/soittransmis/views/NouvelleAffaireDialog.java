package com.soittransmis.views;

import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class NouvelleAffaireDialog extends JDialog {

    private JTextField txtNumeroAffaire;
    private JComboBox<String> comboStatut;
    private JTextField txtDescription;

    private JTextField txtVille;
    private JTextField txtSection;
    private JTextField txtParcelle;
    private JTextField txtLieuDit;

    private JTextField txtNomOpp1;
    private JTextField txtRefOpp1;
    private JTextField txtContactOpp1;

    private JTextField txtNomOpp2;
    private JTextField txtRefOpp2;
    private JTextField txtContactOpp2;

    private boolean saved = false;

    private static final String DB_URL = "jdbc:postgresql://localhost:5432/soit_transmis_db";
    private static final String DB_USER = "postgres";
    private static final String DB_PASSWORD = "postgres";

    public NouvelleAffaireDialog(Frame parent) {
        super(parent, "Créer une nouvelle affaire", true);
        setSize(800, 720);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());

        initComponents();
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout(0, 0));
        mainPanel.setBackground(new Color(245, 247, 250));

        // --- En-tête Moderne ---
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(220, 225, 230)),
            BorderFactory.createEmptyBorder(20, 25, 20, 25)
        ));

        JLabel lblTitre = new JLabel("Nouvelle Affaire & Parties");
        lblTitre.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitre.setForeground(new Color(33, 37, 41));
        
        JLabel lblSousTitre = new JLabel("Renseignez les informations générales, cadastrales et les intervenants du dossier.");
        lblSousTitre.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblSousTitre.setForeground(new Color(108, 117, 125));

        JPanel titleContainer = new JPanel(new GridLayout(2, 1, 0, 3));
        titleContainer.setOpaque(false);
        titleContainer.add(lblTitre);
        titleContainer.add(lblSousTitre);
        headerPanel.add(titleContainer, BorderLayout.WEST);

        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // --- Conteneur Central (Cartes de formulaire) ---
        JPanel formContainer = new JPanel();
        formContainer.setLayout(new BoxLayout(formContainer, BoxLayout.Y_AXIS));
        formContainer.setBackground(new Color(245, 247, 250));
        formContainer.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));

        // 1. Section Affaire
        txtNumeroAffaire = new JTextField();
        comboStatut = new JComboBox<>(new String[]{"En cours", "Traité et classé"});
        txtDescription = new JTextField();
        
        JPanel pnlAffaireGrid = new JPanel(new GridLayout(2, 2, 15, 12));
        pnlAffaireGrid.setOpaque(false);
        pnlAffaireGrid.add(createLabeledField("N° d'Affaire *", txtNumeroAffaire, "ex: AFF-2026-001"));
        pnlAffaireGrid.add(createLabeledField("Statut initial", comboStatut, null));
        pnlAffaireGrid.add(createLabeledField("Description / Motif", txtDescription, "Observations particulières..."));
        
        formContainer.add(createCardPanel("📋 Informations Générales", pnlAffaireGrid));
        formContainer.add(Box.createVerticalStrut(15));

        // 2. Section Cadastre
        txtVille = new JTextField("Libreville");
        txtSection = new JTextField();
        txtParcelle = new JTextField();
        txtLieuDit = new JTextField();

        JPanel pnlCadastreGrid = new JPanel(new GridLayout(2, 2, 15, 12));
        pnlCadastreGrid.setOpaque(false);
        pnlCadastreGrid.add(createLabeledField("Ville / Commune", txtVille, "ex: Libreville"));
        pnlCadastreGrid.add(createLabeledField("Section", txtSection, "ex: B"));
        pnlCadastreGrid.add(createLabeledField("Parcelle", txtParcelle, "ex: 142"));
        pnlCadastreGrid.add(createLabeledField("Lieu-dit", txtLieuDit, "ex: Glass"));

        formContainer.add(createCardPanel("📍 Localisation Géographique (Cadastre)", pnlCadastreGrid));
        formContainer.add(Box.createVerticalStrut(15));

        // 3. Section Opposant 1
        txtNomOpp1 = new JTextField();
        txtRefOpp1 = new JTextField();
        txtContactOpp1 = new JTextField();

        JPanel pnlOpp1Grid = new JPanel(new GridLayout(3, 1, 0, 12));
        pnlOpp1Grid.setOpaque(false);
        pnlOpp1Grid.add(createLabeledField("Nom / Raison Sociale (Plaignant) *", txtNomOpp1, "Nom complet ou entreprise"));
        
        JPanel subRow1 = new JPanel(new GridLayout(1, 2, 15, 0));
        subRow1.setOpaque(false);
        subRow1.add(createLabeledField("Référence Dossier", txtRefOpp1, "ex: DOS-2026-01"));
        subRow1.add(createLabeledField("Contact / Téléphone", txtContactOpp1, "+241 ..."));
        pnlOpp1Grid.add(subRow1);

        formContainer.add(createCardPanel("👤 Opposant 1 (Plaignant / Requérant Principal)", pnlOpp1Grid));
        formContainer.add(Box.createVerticalStrut(15));

        // 4. Section Opposant 2
        txtNomOpp2 = new JTextField();
        txtRefOpp2 = new JTextField();
        txtContactOpp2 = new JTextField();

        JPanel pnlOpp2Grid = new JPanel(new GridLayout(3, 1, 0, 12));
        pnlOpp2Grid.setOpaque(false);
        pnlOpp2Grid.add(createLabeledField("Nom / Raison Sociale (Partie Adverse)", txtNomOpp2, "Optionnel"));
        
        JPanel subRow2 = new JPanel(new GridLayout(1, 2, 15, 0));
        subRow2.setOpaque(false);
        subRow2.add(createLabeledField("Référence Dossier", txtRefOpp2, "Optionnel"));
        subRow2.add(createLabeledField("Contact / Téléphone", txtContactOpp2, "Optionnel"));
        pnlOpp2Grid.add(subRow2);

        formContainer.add(createCardPanel("👥 Opposant 2 (Partie Adverse - Optionnel)", pnlOpp2Grid));

        JScrollPane scrollPane = new JScrollPane(formContainer);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // --- Barre de Boutons Inférieure ---
        JPanel southPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 15));
        southPanel.setBackground(Color.WHITE);
        southPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(220, 225, 230)));

        JButton btnAnnuler = new JButton("Annuler");
        styleButton(btnAnnuler, false);
        btnAnnuler.addActionListener(e -> dispose());

        JButton btnEnregistrer = new JButton("Enregistrer l'Affaire");
        styleButton(btnEnregistrer, true);
        btnEnregistrer.addActionListener(e -> enregistrerAffaire());

        southPanel.add(btnAnnuler);
        southPanel.add(btnEnregistrer);
        mainPanel.add(southPanel, BorderLayout.SOUTH);

        add(mainPanel);
    }

    /**
     * Crée une "Carte" visuelle avec un fond blanc, une bordure douce et un titre de section propre.
     */
    private JPanel createCardPanel(String title, JComponent content) {
        JPanel card = new JPanel(new BorderLayout(0, 12));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(218, 224, 233), 1, true),
            BorderFactory.createEmptyBorder(18, 20, 18, 20)
        ));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTitle.setForeground(new Color(24, 43, 73));
        
        card.add(lblTitle, BorderLayout.NORTH);
        card.add(content, BorderLayout.CENTER);
        return card;
    }

    private JPanel createLabeledField(String labelText, JComponent field, String placeholder) {
        JPanel panel = new JPanel(new BorderLayout(0, 5));
        panel.setOpaque(false);
        
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(new Color(90, 100, 110));
        
        if (field instanceof JTextField && placeholder != null) {
            ((JTextField) field).putClientProperty("JTextField.placeholderText", placeholder);
        }
        
        if (field instanceof JTextField || field instanceof JComboBox) {
            field.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            field.setPreferredSize(new Dimension(0, 34));
        }

        panel.add(lbl, BorderLayout.NORTH);
        panel.add(field, BorderLayout.CENTER);
        return panel;
    }

    private void styleButton(JButton btn, boolean isPrimary) {
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        if (isPrimary) {
            btn.setBackground(new Color(40, 167, 69));
            btn.setForeground(Color.WHITE);
        } else {
            btn.setBackground(new Color(230, 235, 240));
            btn.setForeground(new Color(50, 60, 70));
        }
    }

    private void enregistrerAffaire() {
        String numAffaire = txtNumeroAffaire.getText().trim();
        String nomOpp1 = txtNomOpp1.getText().trim();

        if (numAffaire.isEmpty() || nomOpp1.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Veuillez remplir au moins le numéro d'affaire et le nom du premier opposant.", 
                "Champs obligatoires manquants", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        String statut = (String) comboStatut.getSelectedItem();
        String description = txtDescription.getText().trim();
        if (description.isEmpty()) description = "RAS";

        String ville = txtVille.getText().trim();
        String section = txtSection.getText().trim();
        String parcelle = txtParcelle.getText().trim();
        String lieuDit = txtLieuDit.getText().trim();

        String refOpp1 = txtRefOpp1.getText().trim();
        String contactOpp1 = txtContactOpp1.getText().trim();

        String nomOpp2 = txtNomOpp2.getText().trim();
        String refOpp2 = txtRefOpp2.getText().trim();
        String contactOpp2 = txtContactOpp2.getText().trim();

        String sqlAffaire = "INSERT INTO affaires (numero_affaire, description, statut, cree_par) VALUES (?, ?, ?, ?) RETURNING id";
        String sqlOpposant = "INSERT INTO opposants (affaire_id, nom_prenom_ou_raison_sociale, ref_dossier, ville, section, parcelle, lieu_dit, contact, cree_par) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            conn.setAutoCommit(false);

            int affaireId = -1;
            try (PreparedStatement pstmtAffaire = conn.prepareStatement(sqlAffaire)) {
                pstmtAffaire.setString(1, numAffaire);
                pstmtAffaire.setString(2, description);
                pstmtAffaire.setString(3, statut);
                pstmtAffaire.setInt(4, 2);

                try (ResultSet rs = pstmtAffaire.executeQuery()) {
                    if (rs.next()) {
                        affaireId = rs.getInt("id");
                    }
                }
            }

            if (affaireId == -1) {
                conn.rollback();
                throw new SQLException("Échec de la création de l'affaire, ID non récupéré.");
            }

            try (PreparedStatement pstmtOpp = conn.prepareStatement(sqlOpposant)) {
                pstmtOpp.setInt(1, affaireId);
                pstmtOpp.setString(2, nomOpp1);
                pstmtOppOppSetNullable(pstmtOpp, 3, refOpp1);
                pstmtOppOppSetNullable(pstmtOpp, 4, ville);
                pstmtOppOppSetNullable(pstmtOpp, 5, section);
                pstmtOppOppSetNullable(pstmtOpp, 6, parcelle);
                pstmtOppOppSetNullable(pstmtOpp, 7, lieuDit);
                pstmtOppOppSetNullable(pstmtOpp, 8, contactOpp1);
                pstmtOpp.setString(9, "Utilisateur");
                pstmtOpp.executeUpdate();
            }

            if (!nomOpp2.isEmpty()) {
                try (PreparedStatement pstmtOpp = conn.prepareStatement(sqlOpposant)) {
                    pstmtOpp.setInt(1, affaireId);
                    pstmtOpp.setString(2, nomOpp2);
                    pstmtOppOppSetNullable(pstmtOpp, 3, refOpp2);
                    pstmtOppOppSetNullable(pstmtOpp, 4, ville);
                    pstmtOppOppSetNullable(pstmtOpp, 5, section);
                    pstmtOppOppSetNullable(pstmtOpp, 6, parcelle);
                    pstmtOppOppSetNullable(pstmtOpp, 7, lieuDit);
                    pstmtOppOppSetNullable(pstmtOpp, 8, contactOpp2);
                    pstmtOpp.setString(9, "Utilisateur");
                    pstmtOpp.executeUpdate();
                }
            }

            conn.commit();
            saved = true;
            
            JOptionPane.showMessageDialog(this, 
                "Affaire " + numAffaire + " enregistrée avec succès !", 
                "Succès", 
                JOptionPane.INFORMATION_MESSAGE);
            dispose();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, 
                "Erreur lors de l'enregistrement dans la base de données :\n" + e.getMessage(), 
                "Erreur SQL", 
                JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void pstmtOppOppSetNullable(PreparedStatement pstmt, int index, String val) throws SQLException {
        if (val == null || val.isEmpty()) {
            pstmt.setNull(index, Types.VARCHAR);
        } else {
            pstmt.setString(index, val);
        }
    }

    public boolean isSaved() {
        return saved;
    }
}