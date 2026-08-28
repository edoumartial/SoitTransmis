package com.soittransmis.views;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.sql.*;

public class LoginFrame extends JFrame {

    private JTextField txtIdentifiant;
    private JPasswordField txtPassword;
    private JComboBox<String> comboDirection;

    private static final String DB_URL = "jdbc:postgresql://localhost:5432/soit_transmis_db";
    private static final String DB_USER = "postgres";
    private static final String DB_PASSWORD = "postgres";

    public LoginFrame() {
        setTitle("Connexion - SoitTransmis");
        setSize(480, 480);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel panelPrincipal = new JPanel(new BorderLayout(0, 20));
        panelPrincipal.setBorder(BorderFactory.createEmptyBorder(30, 35, 30, 35));
        panelPrincipal.setBackground(new Color(245, 247, 250));

        // --- EN-TÊTE ---
        JPanel panelHeader = new JPanel();
        panelHeader.setLayout(new BoxLayout(panelHeader, BoxLayout.Y_AXIS));
        panelHeader.setOpaque(false);

        JLabel lblTitre = new JLabel("Connexion à l'application");
        lblTitre.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitre.setForeground(new Color(33, 37, 41));
        lblTitre.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblSousTitre = new JLabel("Veuillez saisir vos accès et appuyer sur Entrée");
        lblSousTitre.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblSousTitre.setForeground(new Color(108, 117, 125));
        lblSousTitre.setAlignmentX(Component.CENTER_ALIGNMENT);

        panelHeader.add(lblTitre);
        panelHeader.add(Box.createVerticalStrut(5));
        panelHeader.add(lblSousTitre);
        panelPrincipal.add(panelHeader, BorderLayout.NORTH);

        // --- FORMULAIRE ---
        JPanel panelForm = new JPanel(new GridBagLayout());
        panelForm.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 0, 4, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.weightx = 1.0;

        // Identifiant
        gbc.gridy = 0;
        JLabel lblId = new JLabel("Identifiant :");
        lblId.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblId.setForeground(new Color(73, 80, 87));
        panelForm.add(lblId, gbc);

        gbc.gridy = 1;
        txtIdentifiant = new JTextField();
        txtIdentifiant.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtIdentifiant.setPreferredSize(new Dimension(0, 40));
        txtIdentifiant.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(206, 212, 218), 1),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        panelForm.add(txtIdentifiant, gbc);

        // Mot de passe
        gbc.gridy = 2;
        gbc.insets = new Insets(12, 0, 4, 0);
        JLabel lblPass = new JLabel("Mot de passe :");
        lblPass.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblPass.setForeground(new Color(73, 80, 87));
        panelForm.add(lblPass, gbc);

        gbc.gridy = 3;
        gbc.insets = new Insets(8, 0, 4, 0);
        txtPassword = new JPasswordField();
        txtPassword.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtPassword.setPreferredSize(new Dimension(0, 40));
        txtPassword.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(206, 212, 218), 1),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        panelForm.add(txtPassword, gbc);

        // Direction
        gbc.gridy = 4;
        gbc.insets = new Insets(12, 0, 4, 0);
        JLabel lblDir = new JLabel("Direction :");
        lblDir.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblDir.setForeground(new Color(73, 80, 87));
        panelForm.add(lblDir, gbc);

        gbc.gridy = 5;
        gbc.insets = new Insets(8, 0, 10, 0);
        String[] directions = {
            "Sélectionnez votre direction...",
            "Direction Juridique et du Contentieux",
            "Direction des Travaux Topographiques",
            "Direction du Cadastre"
        };
        comboDirection = new JComboBox<>(directions);
        comboDirection.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        comboDirection.setPreferredSize(new Dimension(0, 40));
        comboDirection.setBackground(Color.WHITE);
        panelForm.add(comboDirection, gbc);

        panelPrincipal.add(panelForm, BorderLayout.CENTER);

        // --- ACTION SUR LA TOUCHE ENTRÉE ---
        ActionListener actionConnexion = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                verifierConnexion();
            }
        };

        txtIdentifiant.addActionListener(actionConnexion);
        txtPassword.addActionListener(actionConnexion);
        comboDirection.addActionListener(e -> {
            // Permet de déclencher si l'utilisateur valide via entrée sur la combo si focus, 
            // ou on peut laisser uniquement sur les champs de texte principaux.
        });

        add(panelPrincipal);
        
        // Mettre le focus direct sur l'identifiant au lancement
        SwingUtilities.invokeLater(() -> txtIdentifiant.requestFocusInWindow());
    }

    private void verifierConnexion() {
        String identifiant = txtIdentifiant.getText().trim();
        String password = new String(txtPassword.getPassword()).trim();
        String directionChoisie = (String) comboDirection.getSelectedItem();

        if (identifiant.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Veuillez remplir tous les champs.", "Champs requis", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (comboDirection.getSelectedIndex() == 0) {
            JOptionPane.showMessageDialog(this, "Veuillez sélectionner votre direction.", "Direction requise", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!directionChoisie.equals("Direction Juridique et du Contentieux")) {
            JOptionPane.showMessageDialog(this, 
                "Accès restreint : Seule la 'Direction Juridique et du Contentieux' peut accéder au tableau de bord des affaires.", 
                "Accès refusé", 
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String query = "SELECT * FROM utilisateurs WHERE identifiant = ? AND mot_de_passe = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setString(1, identifiant);
                pstmt.setString(2, password);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        ouvrirDashboard();
                    } else {
                        JOptionPane.showMessageDialog(this, "Identifiant ou mot de passe incorrect.", "Erreur de connexion", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erreur de connexion à la base de données :\n" + e.getMessage(), "Erreur SQL", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void ouvrirDashboard() {
        String identifiant = txtIdentifiant.getText().trim();
        String roleUtilisateur = "";
        String nomComplet = identifiant; // Par défaut, on prend l'identifiant

        // Récupérer le rôle et éventuellement le nom complet depuis la base
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String query = "SELECT nom, role FROM utilisateurs WHERE identifiant = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setString(1, identifiant);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        // S'assure de récupérer le rôle (adaptez le nom de la colonne selon votre base si besoin, ex: 'role' ou 'fonction')
                        roleUtilisateur = rs.getString("role"); 
                        if (rs.getObject("nom") != null) {
                            nomComplet = rs.getString("nom");
                        }
                    }
                }
            }
        } catch (SQLException ignored) {
            roleUtilisateur = "Administrateur"; // Valeur par défaut en cas de secours
        }

        this.dispose();
        
        final String finalNom = nomComplet;
        final String finalRole = (roleUtilisateur != null && !roleUtilisateur.isEmpty()) ? roleUtilisateur : "Agent";

        SwingUtilities.invokeLater(() -> {
            new DashboardFrame(finalNom, finalRole).setVisible(true);
        });
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> {
            new LoginFrame().setVisible(true);
        });
    }
}