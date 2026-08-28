package com.soittransmis.views;

import com.soittransmis.utils.ButtonEditor;
import com.soittransmis.utils.ButtonRenderer;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.sql.*;
import java.util.Vector;

public class DashboardFrame extends JFrame {

    private JTable tableAffaires;
    private DefaultTableModel tableModel;
    private static ImageIcon folderIcon; 
    private String nomUtilisateur;
    private String roleUtilisateur;
    
    private JTextField txtRecherche;
    private JComboBox<String> comboStatutFiltre;

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

        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(new Color(245, 247, 250));

        JPanel northPanel = new JPanel();
        northPanel.setLayout(new BoxLayout(northPanel, BoxLayout.Y_AXIS));
        northPanel.setBackground(new Color(245, 247, 250));

        JPanel headerTopPanel = new JPanel(new BorderLayout());
        headerTopPanel.setOpaque(false);

        JLabel lblTitre = new JLabel("Gestion et Suivi des Litiges Foncier-Administratives");
        lblTitre.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitre.setForeground(new Color(33, 37, 41));
        headerTopPanel.add(lblTitre, BorderLayout.WEST);

        JPanel userPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        userPanel.setOpaque(false);

        JLabel lblInfoUser = new JLabel("<html><div style='text-align: right;'><span style='color: #212529; font-weight: bold; font-size: 13px;'>" + nomUtilisateur + "</span><br/><span style='color: #6c757d; font-size: 11px;'>" + roleUtilisateur + "</span></div></html>");
        userPanel.add(lblInfoUser);

        JButton btnDeconnexion = new JButton("Déconnexion");
        btnDeconnexion.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnDeconnexion.setBackground(new Color(220, 53, 69));
        btnDeconnexion.setForeground(Color.WHITE);
        btnDeconnexion.setFocusPainted(false);
        btnDeconnexion.setOpaque(true);          
        btnDeconnexion.setBorderPainted(false);  
        btnDeconnexion.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
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
        northPanel.add(Box.createVerticalStrut(15));

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        filterPanel.setOpaque(false);
        filterPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        txtRecherche = new JTextField(22);
        txtRecherche.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtRecherche.putClientProperty("JTextField.placeholderText", "Rechercher par numéro, commune, lieu-dit...");

        comboStatutFiltre = new JComboBox<>(new String[]{"Tous les statuts", "En cours", "Traité et classé"});
        comboStatutFiltre.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        JButton btnNouvelleAffaire = new JButton("➕ Nouvelle Affaire");
        btnNouvelleAffaire.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnNouvelleAffaire.setBackground(new Color(40, 167, 69));
        btnNouvelleAffaire.setForeground(Color.WHITE);
        btnNouvelleAffaire.setFocusPainted(false);
        btnNouvelleAffaire.setOpaque(true);
        btnNouvelleAffaire.setBorderPainted(false);
        btnNouvelleAffaire.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        btnNouvelleAffaire.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btnNouvelleAffaire.addActionListener(e -> {
            NouvelleAffaireDialog dialogAffaire = new NouvelleAffaireDialog(this, nomUtilisateur);
            dialogAffaire.setVisible(true);
            if (dialogAffaire.affaireCreee) {
                chargerDonneesAffaires(); 
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

        // --- Initialisation du Tableau (avec Lieu-dit inclus à l'index 2) ---
        String[] colonnes = {"Numéro affaire", "Commune", "Lieu-dit", "Section", "Parcelle", "Statut", "Description", "Fichiers"};
        
        tableModel = new DefaultTableModel(new Object[][]{}, colonnes) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 6 || column == 7; // Description (index 6) et Fichiers (index 7)
            }
        };

        tableAffaires = new JTable(tableModel);
        chargerDonneesAffaires();
        
        tableModel.addTableModelListener(e -> {
            int row = e.getFirstRow();
            int column = e.getColumn();

            if (column == 6 && row >= 0) {
                String nouveauTexte = (String) tableModel.getValueAt(row, 6);
                String numeroAffaire = (String) tableModel.getValueAt(row, 0);
                mettreAJourDescriptionEnBD(numeroAffaire, nouveauTexte);
            }
        });

        tableAffaires.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);

        tableAffaires.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        tableAffaires.setRowHeight(45); 
        tableAffaires.setSelectionBackground(new Color(220, 235, 252));
        tableAffaires.setSelectionForeground(Color.BLACK);
        tableAffaires.setGridColor(new Color(210, 215, 220));
        tableAffaires.setShowVerticalLines(true);
        
        JTableHeader header = tableAffaires.getTableHeader();
        header.setPreferredSize(new Dimension(header.getWidth(), 45));
        
        header.setDefaultRenderer(new TableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel label = new JLabel();
                label.setFont(new Font("Segoe UI", Font.BOLD, 16));
                label.setForeground(Color.WHITE);
                label.setBackground(new Color(40, 50, 60));
                label.setOpaque(true);
                label.setHorizontalAlignment(SwingConstants.CENTER);
                label.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(70, 80, 90)));

                String texteColonne = value != null ? value.toString() : "";

                if (column == 6) {
                    label.setText(texteColonne + " [ ? ]");
                    label.setToolTipText("<html><b>Aide Description :</b><br>• Clic simple : Modifier la description<br>• Double-clic : Accéder aux détails de l'affaire</html>");
                } else if (column == 7) {
                    label.setText(texteColonne + " [ ? ]");
                    label.setToolTipText("<html><b>Aide Fichiers :</b><br>• Clic simple : Voir les documents de l'affaire<br>• Double-clic : Charger de nouveaux documents</html>");
                } else {
                    label.setText(texteColonne);
                }

                return label;
            }
        });

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        centerRenderer.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        
        tableAffaires.getColumnModel().getColumn(0).setCellRenderer(centerRenderer); // Numéro
        tableAffaires.getColumnModel().getColumn(1).setCellRenderer(centerRenderer); // Commune
        tableAffaires.getColumnModel().getColumn(2).setCellRenderer(centerRenderer); // Lieu-dit
        tableAffaires.getColumnModel().getColumn(3).setCellRenderer(centerRenderer); // Section
        tableAffaires.getColumnModel().getColumn(4).setCellRenderer(centerRenderer); // Parcelle
        
        DefaultTableCellRenderer statutRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(JLabel.CENTER);
                setFont(new Font("Segoe UI", Font.BOLD, 15));

                if (value != null) {
                    String statut = value.toString().trim();
                    if (statut.equalsIgnoreCase("En cours")) {
                        c.setForeground(new Color(40, 167, 69));
                    } else if (statut.equalsIgnoreCase("Traité et classé")) {
                        c.setForeground(new Color(220, 53, 69));
                    } else {
                        c.setForeground(isSelected ? table.getSelectionForeground() : Color.BLACK);
                    }
                }
                return c;
            }
        };
        tableAffaires.getColumnModel().getColumn(5).setCellRenderer(statutRenderer); // Statut

        DefaultTableCellRenderer leftRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (value != null) {
                    String text = value.toString();
                    setToolTipText(text.isEmpty() ? null : text);
                } else {
                    setToolTipText(null);
                }
                return c;
            }
        };
        leftRenderer.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        tableAffaires.getColumnModel().getColumn(6).setCellRenderer(leftRenderer); // Description

        tableAffaires.getColumnModel().getColumn(7).setCellRenderer(new ButtonRenderer(folderIcon));
        tableAffaires.getColumnModel().getColumn(7).setCellEditor(new ButtonEditor(this, folderIcon, tableAffaires, tableModel));
        tableAffaires.getColumnModel().getColumn(7).setMaxWidth(120);
        tableAffaires.getColumnModel().getColumn(7).setMinWidth(120);

        // --- Menu contextuel (Clic-droit) pour Classer / Réouvrir ---
        JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem menuItemChangerStatut = new JMenuItem();
        menuItemChangerStatut.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        menuItemChangerStatut.addActionListener(e -> basculerStatutAffaireSelectionnee());
        popupMenu.add(menuItemChangerStatut);
        tableAffaires.setComponentPopupMenu(popupMenu);

        tableAffaires.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && !SwingUtilities.isRightMouseButton(e)) {
                    int ligneSelectionnee = tableAffaires.getSelectedRow();
                    if (ligneSelectionnee != -1) {
                        String numAffaire = (String) tableModel.getValueAt(ligneSelectionnee, 0);
                        String commune = (String) tableModel.getValueAt(ligneSelectionnee, 1);
                        String description = (String) tableModel.getValueAt(ligneSelectionnee, 6);
                        
                        new AffaireDetailsDialog(DashboardFrame.this, numAffaire, commune, description, folderIcon).setVisible(true);
                    }
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    int row = tableAffaires.rowAtPoint(e.getPoint());
                    if (row >= 0 && row < tableAffaires.getRowCount()) {
                        tableAffaires.setRowSelectionInterval(row, row);
                        String statut = (String) tableModel.getValueAt(row, 5);
                        if (statut != null && statut.equalsIgnoreCase("En cours")) {
                    menuItemChangerStatut.setText("Classer l'affaire");
                } else {
                    menuItemChangerStatut.setText("Réouvrir l'affaire");
                }
                    }
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(tableAffaires);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(190, 195, 200), 1));
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        JLabel lblInfo = new JLabel("💡 Astuce : Double-cliquez sur une ligne pour afficher les détails. Clic-droit pour Classer/Réouvrir. Dossier 📁 pour uploader.");
        lblInfo.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblInfo.setForeground(new Color(90, 100, 110));
        mainPanel.add(lblInfo, BorderLayout.SOUTH);

        add(mainPanel);

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

    private void chargerDonneesAffaires() {
    StringBuilder query = new StringBuilder(
        "SELECT DISTINCT ON (a.numero_affaire) " +
        "a.numero_affaire, a.description, a.statut, o.ville, o.lieu_dit, o.section, o.parcelle " +
        "FROM affaires a " +
        "LEFT JOIN opposants o ON a.id = o.affaire_id WHERE 1=1"
    );

    String texteRecherche = (txtRecherche != null) ? txtRecherche.getText().trim() : "";
    String statutSelectionne = (comboStatutFiltre != null) ? (String) comboStatutFiltre.getSelectedItem() : "Tous les statuts";

    // Ajout du champ lieu_dit (o.lieu_dit ILIKE ?) dans la condition de recherche
    if (!texteRecherche.isEmpty()) {
        query.append(" AND (a.numero_affaire ILIKE ? OR o.ville ILIKE ? OR o.lieu_dit ILIKE ? OR a.description ILIKE ?)");
    }

    if (statutSelectionne != null && !statutSelectionne.equals("Tous les statuts")) {
        query.append(" AND a.statut = ?");
    }

    query.append(" ORDER BY a.numero_affaire");

    try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
         PreparedStatement pstmt = conn.prepareStatement(query.toString())) {

        int paramIndex = 1;
        if (!texteRecherche.isEmpty()) {
            String motif = "%" + texteRecherche + "%";
            pstmt.setString(paramIndex++, motif); // Numéro d'affaire
            pstmt.setString(paramIndex++, motif); // Ville / Commune
            pstmt.setString(paramIndex++, motif); // Lieu-dit (AJOUTÉ)
            pstmt.setString(paramIndex++, motif); // Description
        }
        if (statutSelectionne != null && !statutSelectionne.equals("Tous les statuts")) {
            pstmt.setString(paramIndex++, statutSelectionne);
        }

        try (ResultSet rs = pstmt.executeQuery()) {
            if (tableModel != null) {
                tableModel.setRowCount(0);

                while (rs.next()) {
                    Vector<Object> row = new Vector<>();
                    row.add(rs.getString("numero_affaire")); 
                    row.add(rs.getString("ville")); 
                    row.add(rs.getString("lieu_dit")); // Affichage du lieu-dit dans la bonne colonne
                    row.add(rs.getString("section"));        
                    row.add(rs.getString("parcelle"));       
                    row.add(rs.getString("statut"));         
                    row.add(rs.getString("description"));    
                    row.add("Ouvrir");                       
                    tableModel.addRow(row);
                }
            }
        }

    } catch (SQLException e) {
        JOptionPane.showMessageDialog(this, 
            "Erreur de chargement :\n" + e.getMessage(), 
            "Erreur SQL", 
            JOptionPane.ERROR_MESSAGE);
        e.printStackTrace();
    }
}

    private void mettreAJourDescriptionEnBD(String numeroAffaire, String nouvelleDescription) {
        String query = "UPDATE affaires SET description = ? WHERE numero_affaire = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, nouvelleDescription);
            pstmt.setString(2, numeroAffaire);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, 
                "Erreur lors de la mise à jour de la description :\n" + e.getMessage(), 
                "Erreur SQL", 
                JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
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
        dialog.setSize(650, 420);
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
        listeFichiersCellRenderer(listeFichiers);
        listeFichiers.setFixedCellHeight(40);
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

    private void listeFichiersCellRenderer(JList<File> list) {
        list.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> jList, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel c = (JLabel) super.getListCellRendererComponent(jList, value, index, isSelected, cellHasFocus);
                if (value instanceof File) {
                    File f = (File) value;
                    c.setText("  - " + f.getName());
                    c.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
                }
                return c;
            }
        });
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
        } else {
            JOptionPane.showMessageDialog(this, 
                "La fonction d'ouverture de fichier n'est pas supportée sur votre système.", 
                "Erreur", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void basculerStatutAffaireSelectionnee() {
    int ligneSelectionnee = tableAffaires.getSelectedRow();
    if (ligneSelectionnee == -1) {
        JOptionPane.showMessageDialog(this, 
            "Veuillez sélectionner une affaire dans le tableau.", 
            "Aucune sélection", 
            JOptionPane.WARNING_MESSAGE);
        return;
    }

    String numeroAffaire = (String) tableModel.getValueAt(ligneSelectionnee, 0);
    String statutActuel = (String) tableModel.getValueAt(ligneSelectionnee, 5);
    
    // Détermination du nouveau statut opposé
    String nouveauStatut = statutActuel.equalsIgnoreCase("En cours") ? "Traité et classé" : "En cours";
    String actionTexte = nouveauStatut.equals("Traité et classé") ? "classer" : "réouvrir";

    int confirmation = JOptionPane.showConfirmDialog(
        this,
        "Voulez-vous vraiment " + actionTexte + " l'affaire n° " + numeroAffaire + " ?",
        "Confirmation de modification de statut",
        JOptionPane.YES_NO_OPTION,
        JOptionPane.QUESTION_MESSAGE
    );

    if (confirmation == JOptionPane.YES_OPTION) {
        String query = "UPDATE affaires SET statut = ?, modifie_par = ? WHERE numero_affaire = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, nouveauStatut);
            pstmt.setString(2, nomUtilisateur); // Utilise l'utilisateur connecté
            pstmt.setString(3, numeroAffaire);
            pstmt.executeUpdate();

            // Rafraîchir le tableau pour refléter le changement
            chargerDonneesAffaires();

            JOptionPane.showMessageDialog(this, 
                "L'affaire a été mise à jour avec succès (Statut : " + nouveauStatut + ").", 
                "Succès", 
                JOptionPane.INFORMATION_MESSAGE);

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, 
                "Erreur lors de la mise à jour du statut :\n" + e.getMessage(), 
                "Erreur SQL", 
                JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
}
}