package com.soittransmis.views;

import com.formdev.flatlaf.FlatClientProperties;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.*;

public class NouvelOpposantDialog extends JDialog {

    private JTextField txtNomOpposant;
    private JTextField txtPrenomOpposant;
    private JTextField txtRefDossier;
    private JTextField txtContact;
    private JTextField txtVille;
    private JTextField txtSection;
    private JTextArea txtMotifs;
    
    private String numeroAffaireCible;
    
    // Paramètres de connexion à la base de données (à adapter selon votre configuration)
    private static final String DB_URL = "jdbc:postgresql://localhost:5432/soittransmis_db";
    private static final String DB_USER = "postgres";
    private static final String DB_PASSWORD = "admin";

    public NouvelOpposantDialog(Frame parent, String numeroAffaireCible) {
        super(parent, "Ajouter un opposant - Affaire N° " + numeroAffaireCible, true);
        this.numeroAffaireCible = numeroAffaireCible;
        
        initComponents();
        setSize(500, 650);
        setLocationRelativeTo(parent);
        setResizable(false);
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(new Color(248, 249, 250));

        // En-tête
        JLabel lblTitle = new JLabel("Nouvel Opposant / Tiers");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(new Color(33, 37, 41));
        lblTitle.setBorder(new EmptyBorder(0, 0, 15, 0));
        mainPanel.add(lblTitle, BorderLayout.NORTH);

        // Formulaire central
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(222, 226, 230), 1),
            new EmptyBorder(20, 20, 20, 20)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 10, 0);

        int row = 0;

        // Nom de l'opposant
        gbc.gridy = row++;
        formPanel.add(createFieldLabel("Nom de l'opposant * :"), gbc);
        gbc.gridy = row++;
        txtNomOpposant = new JTextField();
        txtNomOpposant.setPreferredSize(new Dimension(0, 32));
        formPanel.add(txtNomOpposant, gbc);

        // Prénom de l'opposant
        gbc.gridy = row++;
        formPanel.add(createFieldLabel("Prénom(s) :"), gbc);
        gbc.gridy = row++;
        txtPrenomOpposant = new JTextField();
        txtPrenomOpposant.setPreferredSize(new Dimension(0, 32));
        formPanel.add(txtPrenomOpposant, gbc);

        // Référence du dossier
        gbc.gridy = row++;
        formPanel.add(createFieldLabel("Référence du dossier :"), gbc);
        gbc.gridy = row++;
        txtRefDossier = new JTextField();
        txtRefDossier.setPreferredSize(new Dimension(0, 32));
        formPanel.add(txtRefDossier, gbc);

        // Contact (Téléphone / Email)
        gbc.gridy = row++;
        formPanel.add(createFieldLabel("Contact (Téléphone / Email) :"), gbc);
        gbc.gridy = row++;
        txtContact = new JTextField();
        txtContact.setPreferredSize(new Dimension(0, 32));
        formPanel.add(txtContact, gbc);

        // Ville et Section (sur la même ligne ou empilées)
        JPanel subPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        subPanel.setBackground(Color.WHITE);
        
        JPanel pVille = new JPanel(new BorderLayout(0, 5));
        pVille.setBackground(Color.WHITE);
        pVille.add(createFieldLabel("Ville :"), BorderLayout.NORTH);
        txtVille = new JTextField();
        txtVille.setPreferredSize(new Dimension(0, 32));
        pVille.add(txtVille, BorderLayout.CENTER);

        JPanel pSection = new JPanel(new BorderLayout(0, 5));
        pSection.setBackground(Color.WHITE);
        pSection.add(createFieldLabel("Section :"), BorderLayout.NORTH);
        txtSection = new JTextField();
        txtSection.setPreferredSize(new Dimension(0, 32));
        pSection.add(txtSection, BorderLayout.CENTER);

        subPanel.add(pVille);
        subPanel.add(pSection);

        gbc.gridy = row++;
        formPanel.add(subPanel, gbc);
        row++; // incrémentation supplémentaire pour le sous-panneau

        // Motifs de l'opposition
        gbc.gridy = row++;
        formPanel.add(createFieldLabel("Motifs de l'opposition :"), gbc);
        gbc.gridy = row++;
        txtMotifs = new JTextArea(4, 20);
        txtMotifs.setLineWrap(true);
        txtMotifs.setWrapStyleWord(true);
        JScrollPane scrollMotifs = new JScrollPane(txtMotifs);
        formPanel.add(scrollMotifs, gbc);

        mainPanel.add(formPanel, BorderLayout.CENTER);

        // Panneau des boutons du bas
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 15));
        btnPanel.setBackground(new Color(248, 249, 250));

        JButton btnAnnuler = new JButton("Annuler");
        btnAnnuler.setPreferredSize(new Dimension(100, 35));
        btnAnnuler.addActionListener(e -> dispose());

        JButton btnEnregistrer = new JButton("Enregistrer");
        btnEnregistrer.setPreferredSize(new Dimension(120, 35));
        btnEnregistrer.putClientProperty(FlatClientProperties.STYLE, "background: #0d6efd; foreground: #ffffff;");
        btnEnregistrer.addActionListener(e -> enregistrerOpposantEtContinuer());

        btnPanel.add(btnAnnuler);
        btnPanel.add(btnEnregistrer);

        mainPanel.add(btnPanel, BorderLayout.SOUTH);

        setContentPane(mainPanel);
    }

    private JLabel createFieldLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(new Color(73, 80, 87));
        return lbl;
    }

    private void enregistrerOpposantEtContinuer() {
        String nom = txtNomOpposant.getText().trim();
        String prenom = txtPrenomOpposant.getText().trim();
        String refDossier = txtRefDossier.getText().trim();
        String contact = txtContact.getText().trim();
        String ville = txtVille.getText().trim();
        String section = txtSection.getText().trim();
        String motifs = txtMotifs.getText().trim();

        if (nom.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Le nom de l'opposant est obligatoire.", "Champ requis", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String nomComplet = nom + (prenom.isEmpty() ? "" : " " + prenom);

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            // Récupérer l'ID de l'affaire correspondante
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
                // Insertion dans la base de données avec la colonne contact
                String insertOpposant = "INSERT INTO opposants (affaire_id, nom_prenom_ou_raison_sociale, ref_dossier, contact, ville, section) VALUES (?, ?, ?, ?, ?, ?)";
                try (PreparedStatement pstmt = conn.prepareStatement(insertOpposant)) {
                    pstmt.setInt(1, affaireId);
                    pstmt.setString(2, nomComplet);
                    pstmt.setString(3, refDossier.isEmpty() ? null : refDossier);
                    pstmt.setString(4, contact.isEmpty() ? null : contact);
                    pstmt.setString(5, ville);
                    pstmt.setString(6, section);
                    pstmt.executeUpdate();
                }

                JOptionPane.showMessageDialog(this, "Opposant ajouté avec succès !", "Succès", JOptionPane.INFORMATION_MESSAGE);

                // Demander si l'utilisateur souhaite ajouter un autre opposant pour la même affaire
                int reponse = JOptionPane.showConfirmDialog(
                    this, 
                    "Voulez-vous ajouter un autre opposant pour cette affaire ?", 
                    "Continuer", 
                    JOptionPane.YES_NO_OPTION, 
                    JOptionPane.QUESTION_MESSAGE
                );

                if (reponse == JOptionPane.YES_OPTION) {
                    // Réinitialisation des champs pour une nouvelle saisie consécutive
                    txtNomOpposant.setText("");
                    txtPrenomOpposant.setText("");
                    txtRefDossier.setText("");
                    txtContact.setText("");
                    txtVille.setText("");
                    txtSection.setText("");
                    txtMotifs.setText("");
                    txtNomOpposant.requestFocusInWindow();
                } else {
                    dispose();
                }
            } else {
                JOptionPane.showMessageDialog(this, "Affaire introuvable dans la base de données.", "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erreur SQL : " + ex.getMessage(), "Erreur de base de données", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }
}