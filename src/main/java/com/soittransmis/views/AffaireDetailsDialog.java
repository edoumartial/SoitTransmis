package com.soittransmis.views;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.sql.*;

public class AffaireDetailsDialog extends JDialog {

    private static final String DB_URL = "jdbc:postgresql://localhost:5432/soit_transmis_db";
    private static final String DB_USER = "postgres";
    private static final String DB_PASSWORD = "postgres";
    
    private ImageIcon folderIcon;
    private String numAffaireActuelle;
    private JPanel panelCentre; // Déclaré en variable de classe pour rafraîchir l'affichage après un ajout

    public AffaireDetailsDialog(JFrame parent, String numAffaire, String commune, String description, ImageIcon folderIcon) {
        super(parent, "Détails de l'affaire : " + numAffaire, true);
        this.folderIcon = folderIcon;
        this.numAffaireActuelle = numAffaire;
        setSize(850, 600);
        setLocationRelativeTo(parent);

        JPanel panelPrincipal = new JPanel(new BorderLayout(0, 15));
        panelPrincipal.setBackground(new Color(245, 247, 250));
        panelPrincipal.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // --- EN-TÊTE ---
        JPanel panelHeader = new JPanel();
        panelHeader.setLayout(new BoxLayout(panelHeader, BoxLayout.Y_AXIS));
        panelHeader.setOpaque(false);

        // Ligne supérieure de l'en-tête : Titre à gauche, Bouton d'ajout d'opposant à droite
        JPanel headerTopRow = new JPanel(new BorderLayout());
        headerTopRow.setOpaque(false);

        JLabel lblTitreDetail = new JLabel("Dossier : " + numAffaire);
        lblTitreDetail.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitreDetail.setForeground(new Color(33, 37, 41));
        headerTopRow.add(lblTitreDetail, BorderLayout.WEST);

        // Bouton pour ajouter un opposant
        JButton btnAjouterOpposant = new JButton("➕ Ajouter un opposant");
        btnAjouterOpposant.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnAjouterOpposant.setBackground(new Color(0, 123, 255));
        btnAjouterOpposant.setForeground(Color.WHITE);
        btnAjouterOpposant.setFocusPainted(false);
        btnAjouterOpposant.setOpaque(true);
        btnAjouterOpposant.setBorderPainted(false);
        btnAjouterOpposant.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        btnAjouterOpposant.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btnAjouterOpposant.addActionListener(e -> {
            // Ouvre le formulaire d'ajout d opposant
            NouvelOpposantDialog dialogOpposant = new NouvelOpposantDialog((Frame) SwingUtilities.getWindowAncestor(this), numAffaireActuelle);
            dialogOpposant.setVisible(true);
            
            // Actualise les cartes des opposants après fermeture
            rafraichirOpposants();
        });

        headerTopRow.add(btnAjouterOpposant, BorderLayout.EAST);
        panelHeader.add(headerTopRow);
        panelHeader.add(Box.createVerticalStrut(8));

        JLabel lblInfos = new JLabel("<html><b>Commune :</b> " + (commune != null ? commune : "N/A") + 
                                     " &nbsp;&nbsp;|&nbsp;&nbsp; <b>Description :</b> " + (description != null ? description : "N/A") + "</html>");
        lblInfos.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblInfos.setForeground(new Color(108, 117, 125));
        panelHeader.add(lblInfos);

        panelPrincipal.add(panelHeader, BorderLayout.NORTH);

        // --- ZONE DE TRAVAIL LIBRE POUR LES CARTES ---
        panelCentre = new JPanel(null);
        panelCentre.setBackground(Color.WHITE);
        panelCentre.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(206, 212, 218), 1),
            " Parties prenantes (Glissez les cartes librement dans le cadre) ",
            javax.swing.border.TitledBorder.LEFT,
            javax.swing.border.TitledBorder.TOP,
            new Font("Segoe UI", Font.BOLD, 13),
            new Color(73, 80, 87)
        ));

        panelCentre.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                panelCentre.requestFocusInWindow();
            }
        });

        chargerOpposantsCartesLibres(numAffaire, panelCentre);
        panelPrincipal.add(panelCentre, BorderLayout.CENTER);

        add(panelPrincipal);
    }

    private void rafraichirOpposants() {
        panelCentre.removeAll();
        chargerOpposantsCartesLibres(numAffaireActuelle, panelCentre);
        panelCentre.revalidate();
        panelCentre.repaint();
    }

    private void chargerOpposantsCartesLibres(String numeroAffaire, JPanel containerLibre) {
        String query = "SELECT o.id, o.nom_prenom_ou_raison_sociale, o.ref_dossier " +
                       "FROM opposants o " +
                       "JOIN affaires a ON o.affaire_id = a.id " +
                       "WHERE a.numero_affaire = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, numeroAffaire);
            try (ResultSet rs = pstmt.executeQuery()) {
                boolean hasData = false;
                int x = 20;
                int y = 35;
                int index = 0;

                while (rs.next()) {
                    hasData = true;
                    String nom = rs.getString("nom_prenom_ou_raison_sociale");
                    String ref = rs.getString("ref_dossier");

                    JPanel carte = new JPanel() {
                        @Override
                        protected void paintComponent(Graphics g) {
                            super.paintComponent(g);
                            Graphics2D g2 = (Graphics2D) g.create();
                            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                            
                            g2.setColor(Color.WHITE);
                            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                            
                            float[] dashPattern = {6f, 6f};
                            g2.setStroke(new java.awt.BasicStroke(1.5f, java.awt.BasicStroke.CAP_BUTT, java.awt.BasicStroke.JOIN_MITER, 10.0f, dashPattern, 0.0f));
                            g2.setColor(new Color(173, 181, 189));
                            g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 10, 10);
                            
                            g2.dispose();
                        }
                    };
                    carte.setLayout(new BoxLayout(carte, BoxLayout.Y_AXIS));
                    carte.setBounds(x, y, 245, 165);
                    carte.setOpaque(false);
                    carte.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
                    carte.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));

                    JLabel lblTiret = new JLabel("- - - - - - - - - - - - - - - - -");
                    lblTiret.setFont(new Font("Segoe UI", Font.BOLD, 11));
                    lblTiret.setForeground(new Color(134, 142, 150));
                    lblTiret.setAlignmentX(Component.LEFT_ALIGNMENT);
                    carte.add(lblTiret);
                    carte.add(Box.createVerticalStrut(4));

                    JTextField txtNom = new JTextField(nom);
                    txtNom.setFont(new Font("Segoe UI", Font.BOLD, 13));
                    txtNom.setForeground(new Color(33, 37, 41));
                    txtNom.setEditable(false);
                    txtNom.setBorder(null);
                    txtNom.setOpaque(false);
                    txtNom.setAlignmentX(Component.LEFT_ALIGNMENT);
                    carte.add(txtNom);
                    carte.add(Box.createVerticalStrut(2));

                    JTextField txtRef = new JTextField("Réf : " + (ref != null ? ref : "N/A"));
                    txtRef.setFont(new Font("Segoe UI", Font.ITALIC, 12));
                    txtRef.setForeground(new Color(108, 117, 125));
                    txtRef.setEditable(false);
                    txtRef.setBorder(null);
                    txtRef.setOpaque(false);
                    txtRef.setAlignmentX(Component.LEFT_ALIGNMENT);
                    carte.add(txtRef);
                    carte.add(Box.createVerticalStrut(8));

                    // Bouton Dossier individuel pour chaque opposant
                    JButton btnDossierOpposant = new JButton("Documents");
                    if (folderIcon != null) {
                        btnDossierOpposant.setIcon(folderIcon);
                    }
                    btnDossierOpposant.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                    btnDossierOpposant.setFocusPainted(false);
                    btnDossierOpposant.setCursor(new Cursor(Cursor.HAND_CURSOR));
                    btnDossierOpposant.setAlignmentX(Component.LEFT_ALIGNMENT);
                    
                    btnDossierOpposant.addActionListener(e -> voirDocumentsOpposant(numeroAffaire, nom));
                    
                    carte.add(btnDossierOpposant);

                    // Glisser-déposer de la carte
                    MouseAdapter moveOrSelect = new MouseAdapter() {
                        private int mouseX, mouseY;

                        @Override
                        public void mousePressed(MouseEvent e) {
                            mouseX = e.getX();
                            mouseY = e.getY();
                            containerLibre.setComponentZOrder(carte, 0);
                        }

                        @Override
                        public void mouseDragged(MouseEvent e) {
                            if (!txtNom.getCaret().isSelectionVisible() && !txtRef.getCaret().isSelectionVisible()) {
                                int nouveauX = carte.getX() + (e.getX() - mouseX);
                                int nouveauY = carte.getY() + (e.getY() - mouseY);
                                carte.setLocation(nouveauX, nouveauY);
                                containerLibre.revalidate();
                                containerLibre.repaint();
                            }
                        }
                    };

                    carte.addMouseListener(moveOrSelect);
                    carte.addMouseMotionListener(moveOrSelect);
                    txtNom.addMouseListener(moveOrSelect);
                    txtNom.addMouseMotionListener(moveOrSelect);
                    txtRef.addMouseListener(moveOrSelect);
                    txtRef.addMouseMotionListener(moveOrSelect);

                    containerLibre.add(carte);

                    x += 270;
                    index++;
                    if (index % 2 == 0) {
                        x = 20;
                        y += 185;
                    }
                }

                if (!hasData) {
                    JLabel lblAucun = new JLabel("Aucun opposant enregistré pour cette affaire.");
                    lblAucun.setFont(new Font("Segoe UI", Font.ITALIC, 14));
                    lblAucun.setForeground(new Color(108, 117, 125));
                    lblAucun.setBounds(20, 30, 350, 25);
                    containerLibre.add(lblAucun);
                }
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erreur de chargement des opposants.", "Erreur SQL", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void voirDocumentsOpposant(String numeroAffaire, String nomOpposant) {
        java.util.List<File> fichiersOpposant = new java.util.ArrayList<>();
        
        String query = "SELECT d.chemin_disque FROM documents d " +
                       "JOIN opposants o ON d.opposant_id = o.id " +
                       "JOIN affaires a ON o.affaire_id = a.id " +
                       "WHERE a.numero_affaire = ? AND o.nom_prenom_ou_raison_sociale = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, numeroAffaire);
            pstmt.setString(2, nomOpposant);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    File f = new File(rs.getString("chemin_disque"));
                    if (f.exists()) {
                        fichiersOpposant.add(f);
                    }
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erreur lors de la récupération des documents.", "Erreur SQL", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }

        if (!fichiersOpposant.isEmpty()) {
            Object[] options = {"Voir les documents", "Charger de nouveaux documents", "Annuler"};
            int choix = JOptionPane.showOptionDialog(this, 
                "Des documents existent déjà pour cet opposant (" + fichiersOpposant.size() + "). Que souhaitez-vous faire ?", 
                "Gestion des documents de l'opposant", 
                JOptionPane.YES_NO_CANCEL_OPTION, 
                JOptionPane.QUESTION_MESSAGE, 
                null, options, options[0]);

            if (choix == 0) {
                ouvrirFichiersDialogue(fichiersOpposant.toArray(new File[0]), "Documents de l'opposant : " + nomOpposant);
            } else if (choix == 1) {
                gererUploadFichierOpposant(numeroAffaire, nomOpposant);
            }
            return;
        }

        int choix = JOptionPane.showConfirmDialog(this, 
            "Aucun document enregistré pour cet opposant.\nVoulez-vous en charger ?", 
            "Dossier vide", 
            JOptionPane.YES_NO_OPTION);
        if (choix == JOptionPane.YES_OPTION) {
            gererUploadFichierOpposant(numeroAffaire, nomOpposant);
        }
    }

    private void gererUploadFichierOpposant(String numeroAffaire, String nomOpposant) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Charger un document pour l'opposant : " + nomOpposant);
        int userSelection = fileChooser.showOpenDialog(this);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fichierSource = fileChooser.getSelectedFile();
            String nomDossierPropre = nomOpposant.replaceAll("[^a-zA-Z0-9-_]", "_");
            File dossierCible = new File("uploads/" + numeroAffaire + "/" + nomDossierPropre);
            if (!dossierCible.exists()) {
                dossierCible.mkdirs();
            }

            File fichierDestination = new File(dossierCible, fichierSource.getName());

            try {
                java.nio.file.Files.copy(fichierSource.toPath(), fichierDestination.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                
                String insertQuery = "INSERT INTO documents (affaire_id, opposant_id, nom_fichier, chemin_disque) " +
                                     "SELECT a.id, o.id, ?, ? FROM opposants o " +
                                     "JOIN affaires a ON o.affaire_id = a.id " +
                                     "WHERE a.numero_affaire = ? AND o.nom_prenom_ou_raison_sociale = ?";
                
                try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                     PreparedStatement pstmt = conn.prepareStatement(insertQuery)) {
                    pstmt.setString(1, fichierDestination.getName());
                    pstmt.setString(2, fichierDestination.getAbsolutePath());
                    pstmt.setString(3, numeroAffaire);
                    pstmt.setString(4, nomOpposant);
                    pstmt.executeUpdate();
                }

                JOptionPane.showMessageDialog(this, 
                    "Document de l'opposant chargé et enregistré avec succès !", 
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

    private void ouvrirFichiersDialogue(File[] fichiers, String titre) {
        JDialog dialog = new JDialog(this, titre, true);
        dialog.setSize(650, 420);
        dialog.setLocationRelativeTo(this);
        
        JPanel panelPrincipal = new JPanel(new BorderLayout(15, 15));
        panelPrincipal.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panelPrincipal.setBackground(new Color(245, 247, 250));

        JLabel lblTitre = new JLabel("Double-cliquez pour ouvrir un fichier :");
        lblTitre.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblTitre.setForeground(new Color(33, 37, 41));
        panelPrincipal.add(lblTitre, BorderLayout.NORTH);

        DefaultListModel<File> listModel = new DefaultListModel<>();
        for (File f : fichiers) {
            if (f.isFile()) listModel.addElement(f);
        }

        JList<File> listeFichiers = new JList<>(listModel);
        listeFichiers.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        listeFichiers.setFixedCellHeight(40);
        listeFichiers.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> jList, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel c = (JLabel) super.getListCellRendererComponent(jList, value, index, isSelected, cellHasFocus);
                if (value instanceof File) {
                    c.setText("  - " + ((File) value).getName());
                    c.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
                }
                return c;
            }
        });

        JScrollPane scrollPane = new JScrollPane(listeFichiers);
        panelPrincipal.add(scrollPane, BorderLayout.CENTER);

        JButton btnFermer = new JButton("Fermer");
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
                    if (selected != null && Desktop.isDesktopSupported()) {
                        try {
                            Desktop.getDesktop().open(selected);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                }
            }
        });

        dialog.add(panelPrincipal);
        dialog.setVisible(true);
    }
}