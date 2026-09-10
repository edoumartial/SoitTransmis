package com.soittransmis.views;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.sql.*;

public class DashboardFrame extends JFrame {

    private JPanel containerAffaires;
    private static ImageIcon folderIcon; 
    private String nomUtilisateur;
    private String roleUtilisateur;
    
    private JTextField txtRecherche;
    private JComboBox<String> comboStatutFiltre;

    // Paramètres de connexion à votre base de données PostgreSQL
    private static final String DB_URL = "jdbc:postgresql://localhost:5432/soit_transmis_db";
    private static final String DB_USER = "postgres";                                    
    private static final String DB_PASSWORD = "postgres"; 
    
    public DashboardFrame() {
        this("Utilisateur", "Agent");
    }

    public DashboardFrame(String nom, String role) {
        this.nomUtilisateur = nom;
        this.roleUtilisateur = role;

        setTitle("Tableau de bord - Suivi des Affaires");
        
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLocationRelativeTo(null);

        chargerIconeDossier();

        JPanel mainPanel = new JPanel(new BorderLayout(20, 20));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));
        mainPanel.setBackground(new Color(245, 247, 250));

        JPanel northPanel = new JPanel();
        northPanel.setLayout(new BoxLayout(northPanel, BoxLayout.Y_AXIS));
        northPanel.setBackground(new Color(245, 247, 250));

        JPanel headerTopPanel = new JPanel(new BorderLayout());
        headerTopPanel.setOpaque(false);

        JLabel lblTitre = new JLabel("Gestion et Suivi des Litiges Foncier-Administratives");
        lblTitre.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblTitre.setForeground(new Color(33, 37, 41));
        headerTopPanel.add(lblTitre, BorderLayout.WEST);

        JPanel userPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        userPanel.setOpaque(false);

        JLabel lblInfoUser = new JLabel("<html><div style='text-align: right;'><span style='color: #212529; font-weight: bold; font-size: 14px;'>" + nomUtilisateur + "</span><br/><span style='color: #6c757d; font-size: 12px;'>" + roleUtilisateur + "</span></div></html>");
        userPanel.add(lblInfoUser);

        JButton btnDeconnexion = new JButton("Déconnexion");
        btnDeconnexion.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnDeconnexion.setBackground(new Color(220, 53, 69));
        btnDeconnexion.setForeground(Color.WHITE);
        btnDeconnexion.setFocusPainted(false);
        btnDeconnexion.setOpaque(true);          
        btnDeconnexion.setBorderPainted(false);  
        btnDeconnexion.setBorder(BorderFactory.createEmptyBorder(10, 18, 10, 18));
        btnDeconnexion.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btnDeconnexion.addActionListener(e -> {
            int choix = JOptionPane.showConfirmDialog(
                this, 
                "Voulez-vous vraiment vous déconnecter ?", 
                "Confirmation de déconnexion", 
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
            );
            
            if (choix == JOptionPane.YES_OPTION) {
                this.dispose();
                SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
            }
        });

        userPanel.add(btnDeconnexion);
        headerTopPanel.add(userPanel, BorderLayout.EAST);

        northPanel.add(headerTopPanel);
        northPanel.add(Box.createVerticalStrut(20));

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        filterPanel.setOpaque(false);
        filterPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        txtRecherche = new JTextField(30);
        txtRecherche.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtRecherche.putClientProperty("JTextField.placeholderText", "Rechercher par nom d'opposant, contact, référence de dossier...");

        comboStatutFiltre = new JComboBox<>(new String[]{"Tous les statuts", "En cours", "Traité et classé"});
        comboStatutFiltre.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        JButton btnNouvelleAffaire = new JButton("+ Nouvelle Affaire");
        btnNouvelleAffaire.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnNouvelleAffaire.setBackground(new Color(40, 167, 69));
        btnNouvelleAffaire.setForeground(Color.WHITE);
        btnNouvelleAffaire.setFocusPainted(false);
        btnNouvelleAffaire.setOpaque(true);
        btnNouvelleAffaire.setBorderPainted(false);
        btnNouvelleAffaire.setBorder(BorderFactory.createEmptyBorder(10, 18, 10, 18));
        btnNouvelleAffaire.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btnNouvelleAffaire.addActionListener(e -> {
            NouvelleAffaireDialog dialogAffaire = new NouvelleAffaireDialog(this);
            dialogAffaire.setVisible(true);
            if (dialogAffaire.isSaved()) {
                chargerDonneesAffaires(); // Actualisation automatique après l'ajout
            }
        });

        filterPanel.add(new JLabel("🔍 Recherche :"));
        filterPanel.add(txtRecherche);
        filterPanel.add(new JLabel("Statut :"));
        filterPanel.add(comboStatutFiltre);
        filterPanel.add(Box.createHorizontalStrut(20)); 
        filterPanel.add(btnNouvelleAffaire);

        northPanel.add(filterPanel);
        mainPanel.add(northPanel, BorderLayout.NORTH);

        // --- Conteneur des Cartes d'Affaires ---
        containerAffaires = new JPanel();
        containerAffaires.setLayout(new BoxLayout(containerAffaires, BoxLayout.Y_AXIS));
        containerAffaires.setBackground(new Color(245, 247, 250));

        JScrollPane scrollPane = new JScrollPane(containerAffaires);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        JLabel lblInfo = new JLabel("💡 Astuce : Utilisez les boutons de dossier pour consulter les documents de l'affaire ou des opposants.");
        lblInfo.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblInfo.setForeground(new Color(90, 100, 110));
        mainPanel.add(lblInfo, BorderLayout.SOUTH);

        add(mainPanel);

        // Chargement initial des données de la base
        chargerDonneesAffaires();

        // Écouteurs pour la recherche dynamique
        txtRecherche.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent e) {
                chargerDonneesAffaires();
            }
        });

        comboStatutFiltre.addActionListener(e -> chargerDonneesAffaires());
    }

    private void chargerIconeDossier() {
        File imgFile = new File("img/folder.png");
        if (imgFile.exists()) {
            ImageIcon originalIcon = new ImageIcon(imgFile.getAbsolutePath());
            Image scaledImage = originalIcon.getImage().getScaledInstance(22, 22, Image.SCALE_SMOOTH);
            folderIcon = new ImageIcon(scaledImage);
        } else {
            folderIcon = null; 
        }
    }

    /**
     * Interroge la base de données PostgreSQL pour récupérer les affaires et leurs opposants associés.
     */
    private void chargerDonneesAffaires() {
        containerAffaires.removeAll();

        String query = 
            "WITH RankedOpposants AS (" +
            "    SELECT affaire_id, nom_prenom_ou_raison_sociale, ref_dossier, contact, ville, section, parcelle, " +
            "    ROW_NUMBER() OVER (PARTITION BY affaire_id ORDER BY id ASC) as rn " +
            "    FROM opposants" +
            ") " +
            "SELECT a.numero_affaire, a.statut, a.cree_le AS date_creation, " +
            "  MAX(o.ville) AS commune, MAX(o.section) AS section, MAX(o.parcelle) AS parcelle, " +
            "  MAX(CASE WHEN o.rn = 1 THEN o.nom_prenom_ou_raison_sociale END) AS nom_opp1, " +
            "  MAX(CASE WHEN o.rn = 1 THEN o.ref_dossier END) AS ref_opp1, " +
            "  MAX(CASE WHEN o.rn = 1 THEN o.contact END) AS contact_opp1, " +
            "  MAX(CASE WHEN o.rn = 2 THEN o.nom_prenom_ou_raison_sociale END) AS nom_opp2, " +
            "  MAX(CASE WHEN o.rn = 2 THEN o.ref_dossier END) AS ref_opp2, " +
            "  MAX(CASE WHEN o.rn = 2 THEN o.contact END) AS contact_opp2 " +
            "FROM affaires a " +
            "LEFT JOIN RankedOpposants o ON a.id = o.affaire_id WHERE 1=1";

        String texteRecherche = (txtRecherche != null) ? txtRecherche.getText().trim() : "";
        String statutSelectionne = (comboStatutFiltre != null) ? (String) comboStatutFiltre.getSelectedItem() : "Tous les statuts";

        if (!texteRecherche.isEmpty()) {
            query += " AND (a.numero_affaire ILIKE ? OR o.nom_prenom_ou_raison_sociale ILIKE ? OR o.contact ILIKE ? OR o.ref_dossier ILIKE ? OR o.ville ILIKE ?)";
        }

        if (statutSelectionne != null && !statutSelectionne.equals("Tous les statuts")) {
            query += " AND a.statut = ?";
        }

        query += " GROUP BY a.numero_affaire, a.statut, a.cree_le ORDER BY a.numero_affaire DESC";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            int paramIndex = 1;
            if (!texteRecherche.isEmpty()) {
                String motif = "%" + texteRecherche + "%";
                pstmt.setString(paramIndex++, motif);
                pstmt.setString(paramIndex++, motif);
                pstmt.setString(paramIndex++, motif);
                pstmt.setString(paramIndex++, motif);
                pstmt.setString(paramIndex++, motif);
            }
            if (statutSelectionne != null && !statutSelectionne.equals("Tous les statuts")) {
                pstmt.setString(paramIndex++, statutSelectionne);
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                boolean hasResults = false;
                while (rs.next()) {
                    hasResults = true;
                    String numAffaire = rs.getString("numero_affaire");
                    String commune = rs.getString("commune");
                    String section = rs.getString("section");
                    String parcelle = rs.getString("parcelle");
                    String lieuDit = "-"; 
                    String date = rs.getString("date_creation");
                    
                    String nomOpp1 = rs.getString("nom_opp1");
                    String refOpp1 = rs.getString("ref_opp1");
                    String contactOpp1 = rs.getString("contact_opp1");

                    String nomOpp2 = rs.getString("nom_opp2");
                    String refOpp2 = rs.getString("ref_opp2");
                    String contactOpp2 = rs.getString("contact_opp2");

                    JPanel card = createAffaireCard(numAffaire, commune, section, parcelle, lieuDit, date,
                            nomOpp1, refOpp1, contactOpp1, nomOpp2, refOpp2, contactOpp2);
                    
                    containerAffaires.add(card);
                    containerAffaires.add(Box.createVerticalStrut(20));
                }

                if (!hasResults) {
                    JPanel emptyPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
                    emptyPanel.setOpaque(false);
                    JLabel lblEmpty = new JLabel("Aucune affaire trouvée dans la base de données.");
                    lblEmpty.setFont(new Font("Segoe UI", Font.ITALIC, 15));
                    lblEmpty.setForeground(new Color(100, 110, 120));
                    emptyPanel.add(lblEmpty);
                    containerAffaires.add(emptyPanel);
                }
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, 
                "Erreur de connexion à la base de données :\n" + e.getMessage(), 
                "Erreur SQL", 
                JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }

        containerAffaires.revalidate();
        containerAffaires.repaint();
    }

    private JPanel createAffaireCard(String numAffaire, String commune, String section, String parcelle, 
                                     String lieuDit, String date, 
                                     String nomOpp1, String refOpp1, String contactOpp1, 
                                     String nomOpp2, String refOpp2, String contactOpp2) {
        
        JPanel cardPanel = new JPanel(new BorderLayout());
        cardPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 210, 220), 1),
            BorderFactory.createEmptyBorder(0, 0, 15, 0)
        ));
        cardPanel.setBackground(Color.WHITE);

        // 1. En-tête (Barre bleue)
        JPanel headerPanel = new JPanel(new GridLayout(1, 6, 15, 0));
        headerPanel.setBackground(new Color(24, 43, 73)); 
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        headerPanel.add(createHeaderColumn("Commune", commune));
        headerPanel.add(createHeaderColumn("Section", section));
        headerPanel.add(createHeaderColumn("Parcelle", parcelle));
        headerPanel.add(createHeaderColumn("Lieu-dit", lieuDit));
        headerPanel.add(createHeaderColumn("Date", date != null ? date.substring(0, Math.min(date.length(), 10)) : "-"));
        
        JPanel affaireActionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        affaireActionPanel.setOpaque(false);
        JButton btnAffaireDoc = createFolderButton(folderIcon, "Documents de l'Affaire");
        btnAffaireDoc.addActionListener(e -> voirDocumentsAffaire(numAffaire));
        affaireActionPanel.add(createHeaderColumn("N° Affaire", numAffaire));
        affaireActionPanel.add(Box.createHorizontalStrut(10));
        affaireActionPanel.add(btnAffaireDoc);

        headerPanel.add(affaireActionPanel);
        cardPanel.add(headerPanel, BorderLayout.NORTH);

        // 2. Corps : Panneaux des Opposants (avec un wrapper BorderLayout.NORTH pour bloquer l'étirement vertical)
        JPanel bodyPanel = new JPanel(new GridLayout(1, 2, 25, 0));
        bodyPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        bodyPanel.setBackground(Color.WHITE);

        JPanel panelOpp1 = createOpposantPanel("Opposant 1 - Plaignant(e)", nomOpp1, refOpp1, contactOpp1, numAffaire, 1);
        JPanel wrapper1 = new JPanel(new BorderLayout());
        wrapper1.setOpaque(false);
        wrapper1.add(panelOpp1, BorderLayout.NORTH);

        JPanel panelOpp2 = createOpposantPanel("Opposant 2", nomOpp2, refOpp2, contactOpp2, numAffaire, 2);
        JPanel wrapper2 = new JPanel(new BorderLayout());
        wrapper2.setOpaque(false);
        wrapper2.add(panelOpp2, BorderLayout.NORTH);

        bodyPanel.add(wrapper1);
        bodyPanel.add(wrapper2);

        cardPanel.add(bodyPanel, BorderLayout.CENTER);

        return cardPanel;
    }

    private JPanel createHeaderColumn(String title, String value) {
        JPanel col = new JPanel(new GridLayout(2, 1, 0, 3));
        col.setOpaque(false);
        
        JLabel lblTitle = new JLabel(title);
        lblTitle.setForeground(new Color(180, 195, 210));
        lblTitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        
        JLabel lblValue = new JLabel(value != null && !value.isEmpty() ? value : "-");
        lblValue.setForeground(Color.WHITE);
        lblValue.setFont(new Font("Segoe UI", Font.BOLD, 14));
        
        col.add(lblTitle);
        col.add(lblValue);
        return col;
    }

    private JPanel createOpposantPanel(String title, String nom, String refDossier, String contact, String numeroAffaire, int typeProtagoniste) {
        JPanel panel = new JPanel(new BorderLayout(5, 12));
        panel.setBackground(new Color(248, 249, 250));
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 225, 230), 1),
            BorderFactory.createEmptyBorder(15, 18, 15, 18)
        ));

        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setOpaque(false);

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTitle.setForeground(new Color(30, 40, 50));
        topRow.add(lblTitle, BorderLayout.WEST);

        JButton btnDossier = createFolderButton(folderIcon, typeProtagoniste == 1 ? "Dossier Plaignant" : "Dossier Opposant");
        btnDossier.addActionListener(e -> gererDossierProtagoniste(numeroAffaire, typeProtagoniste));
        
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        btnPanel.setOpaque(false);
        btnPanel.add(btnDossier);
        topRow.add(btnPanel, BorderLayout.EAST);

        panel.add(topRow, BorderLayout.NORTH);

        JPanel fieldsPanel = new JPanel();
        fieldsPanel.setLayout(new BoxLayout(fieldsPanel, BoxLayout.Y_AXIS));
        fieldsPanel.setOpaque(false);

        fieldsPanel.add(createFieldRow("Nom / Raison Soc. :", nom));
        fieldsPanel.add(Box.createVerticalStrut(8));
        fieldsPanel.add(createFieldRow("Référence Dossier :", refDossier));
        fieldsPanel.add(Box.createVerticalStrut(8));
        fieldsPanel.add(createFieldRow("Contact :", contact));

        panel.add(fieldsPanel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createFieldRow(String labelText, String valueText) {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        row.setPreferredSize(new Dimension(0, 30));

        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        label.setForeground(new Color(90, 100, 110));
        label.setPreferredSize(new Dimension(130, 30));

        JTextField textField = new JTextField(valueText != null ? valueText : "");
        textField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        textField.setEditable(false);
        textField.setBackground(Color.WHITE);
        textField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 205, 210)),
            BorderFactory.createEmptyBorder(2, 6, 2, 6)
        ));

        row.add(label, BorderLayout.WEST);
        row.add(textField, BorderLayout.CENTER);

        return row;
    }

    private JButton createFolderButton(ImageIcon icon, String tooltip) {
        JButton btn = new JButton();
        if (icon != null) {
            btn.setIcon(icon);
        } else {
            btn.setText("📁");
        }
        btn.setPreferredSize(new Dimension(32, 32));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setToolTipText(tooltip);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    public void voirDocumentsAffaire(String numeroAffaire) {
        java.util.List<File> fichiersPdf = new java.util.ArrayList<>();
        
        String query = "SELECT d.chemin_disque FROM documents d " +
                       "JOIN affaires a ON d.affaire_id = a.id " +
                       "WHERE a.numero_affaire = ? AND d.opposant_id IS NULL";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, numeroAffaire);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    File f = new File(rs.getString("chemin_disque"));
                    if (f.exists() && f.getName().toLowerCase().endsWith(".pdf")) {
                        fichiersPdf.add(f);
                    }
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erreur lors de la récupération des documents.", "Erreur SQL", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }

        if (!fichiersPdf.isEmpty()) {
            Object[] options = {"Voir les documents", "Charger de nouveaux documents", "Annuler"};
            int choix = JOptionPane.showOptionDialog(this, 
                "Des documents existent déjà pour cette affaire (" + fichiersPdf.size() + "). Que souhaitez-vous faire ?", 
                "Gestion des documents", 
                JOptionPane.YES_NO_CANCEL_OPTION, 
                JOptionPane.QUESTION_MESSAGE, 
                null, options, options[0]);

            if (choix == 0) {
                afficherFichiersFenetre(fichiersPdf, "Documents de l'affaire : " + numeroAffaire);
            } else if (choix == 1) {
                gererUploadFichier(numeroAffaire);
            }
        } else {
            int choix = JOptionPane.showConfirmDialog(this, 
                "Aucun document n'est présent pour cette affaire.\nVoulez-vous en charger ?", 
                "Dossier vide", 
                JOptionPane.YES_NO_OPTION);
            if (choix == JOptionPane.YES_OPTION) {
                gererUploadFichier(numeroAffaire);
            }
        }
    }

    public void gererUploadFichier(String numeroAffaire) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Charger un ou plusieurs documents pour l'affaire : " + numeroAffaire);
        fileChooser.setMultiSelectionEnabled(true);
        
        int userSelection = fileChooser.showOpenDialog(this);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File[] fichiersSources = fileChooser.getSelectedFiles();
            
            File dossierCible = new File("uploads/" + numeroAffaire + "/_commun");
            if (!dossierCible.exists()) {
                dossierCible.mkdirs();
            }

            int nbSucces = 0;
            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
                for (File fichierSource : fichiersSources) {
                    File fichierDestination = new File(dossierCible, fichierSource.getName());
                    java.nio.file.Files.copy(fichierSource.toPath(), fichierDestination.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                    
                    String insertQuery = "INSERT INTO documents (affaire_id, nom_fichier, chemin_disque) SELECT id, ?, ? FROM affaires WHERE numero_affaire = ?";
                    try (PreparedStatement pstmt = conn.prepareStatement(insertQuery)) {
                        pstmt.setString(1, fichierDestination.getName());
                        pstmt.setString(2, fichierDestination.getAbsolutePath());
                        pstmt.setString(3, numeroAffaire);
                        pstmt.executeUpdate();
                        nbSucces++;
                    }
                }
                JOptionPane.showMessageDialog(this, 
                    nbSucces + " document(s) global(aux) chargé(s) avec succès !", 
                    "Upload réussi", 
                    JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, 
                    "Erreur lors de la copie ou de l'enregistrement :\n" + e.getMessage(), 
                    "Erreur", 
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void afficherFichiersFenetre(java.util.List<File> fichiers, String titre) {
        if (fichiers.size() == 1) {
            ouvrirFichierSysteme(fichiers.get(0));
            return;
        }

        JDialog dialog = new JDialog(this, titre, true);
        dialog.setSize(680, 450);
        dialog.setLocationRelativeTo(this);
        
        JPanel panelPrincipal = new JPanel(new BorderLayout(15, 15));
        panelPrincipal.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panelPrincipal.setBackground(new Color(245, 247, 250));

        JLabel lblTitre = new JLabel("Liste des documents disponibles (" + fichiers.size() + ") - Double-cliquez pour ouvrir :");
        lblTitre.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblTitre.setForeground(new Color(33, 37, 41));
        panelPrincipal.add(lblTitre, BorderLayout.NORTH);

        DefaultListModel<File> listModel = new DefaultListModel<>();
        for (File f : fichiers) {
            listModel.addElement(f);
        }

        JList<File> listeFichiers = new JList<>(listModel);
        listeFichiers.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        listeFichiers.setFixedCellHeight(42);
        listeFichiers.setSelectionBackground(new Color(220, 235, 252));
        listeFichiers.setSelectionForeground(Color.BLACK);

        JScrollPane scrollPane = new JScrollPane(listeFichiers);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(190, 195, 200), 1));
        panelPrincipal.add(scrollPane, BorderLayout.CENTER);

        JButton btnFermer = new JButton("Fermer");
        btnFermer.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        btnFermer.addActionListener(e -> dialog.dispose());
        
        JPanel panelSud = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panelSud.setOpaque(false);
        panelSud.add(btnFermer);
        panelPrincipal.add(panelSud, BorderLayout.SOUTH);

        listeFichiers.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    File selected = listeFichiers.getSelectedValue();
                    if (selected != null) {
                        dialog.dispose();
                        ouvrirFichierSysteme(selected);
                    }
                }
            }
        });

        dialog.add(panelPrincipal);
        dialog.setVisible(true);
    }

    private void ouvrirFichierSysteme(File fichier) {
        if (Desktop.isDesktopSupported()) {
            try {
                Desktop.getDesktop().open(fichier);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, 
                    "Impossible d'ouvrir le fichier :\n" + e.getMessage(), 
                    "Erreur d'ouverture", 
                    JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }
    }
    
    public void gererDossierProtagoniste(String numeroAffaire, int typeProtagoniste) {
        String nomProtagoniste = (typeProtagoniste == 1) ? "Opposant 1" : "Opposant 2";
        JOptionPane.showMessageDialog(this, 
            "Gestion du dossier de " + nomProtagoniste + " pour l'affaire n° : " + numeroAffaire,
            "Dossier Protagoniste", 
            JOptionPane.INFORMATION_MESSAGE);
    }
}