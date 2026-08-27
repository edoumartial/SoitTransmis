package com.soittransmis.views;

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
    
    private JTextField txtRecherche;
    private JComboBox<String> comboStatutFiltre;

    private static final String DB_URL = "jdbc:postgresql://localhost:5432/soit_transmis_db";
    private static final String DB_USER = "postgres";                                    
    private static final String DB_PASSWORD = "postgres"; 

    public DashboardFrame() {
        setTitle("Tableau de bord - Suivi des Affaires");
        
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLocationRelativeTo(null);

        chargerIconeDossier();

        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(new Color(245, 247, 250));

        // --- Panneau Nord (Titre + Barre de recherche/filtres) ---
        JPanel northPanel = new JPanel();
        northPanel.setLayout(new BoxLayout(northPanel, BoxLayout.Y_AXIS));
        northPanel.setBackground(new Color(245, 247, 250));

        JLabel lblTitre = new JLabel("Gestion et Suivi des Litiges Foncier-Administratives");
        lblTitre.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitre.setForeground(new Color(33, 37, 41));
        lblTitre.setAlignmentX(Component.LEFT_ALIGNMENT);
        northPanel.add(lblTitre);
        northPanel.add(Box.createVerticalStrut(15));

        // Ligne de filtres (Recherche texte + Dropdown Statut)
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        filterPanel.setOpaque(false);
        filterPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        txtRecherche = new JTextField(22);
        txtRecherche.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtRecherche.putClientProperty("JTextField.placeholderText", "Rechercher par numéro, commune...");

        comboStatutFiltre = new JComboBox<>(new String[]{"Tous les statuts", "En cours", "Traité et classé"});
        comboStatutFiltre.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        filterPanel.add(new JLabel("🔍 Recherche :"));
        filterPanel.add(txtRecherche);
        filterPanel.add(new JLabel("Statut :"));
        filterPanel.add(comboStatutFiltre);

        northPanel.add(filterPanel);
        mainPanel.add(northPanel, BorderLayout.NORTH);

        // --- Initialisation du Tableau ---
        String[] colonnes = {"Numéro affaire", "Commune", "Section", "Parcelle", "Statut", "Description", "Fichiers"};
        
        tableModel = new DefaultTableModel(new Object[][]{}, colonnes) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 5 || column == 6; 
            }
        };

        tableAffaires = new JTable(tableModel);

        // Chargement initial des données
        chargerDonneesAffaires();
        
        tableModel.addTableModelListener(e -> {
            int row = e.getFirstRow();
            int column = e.getColumn();

            if (column == 5 && row >= 0) {
                String nouveauTexte = (String) tableModel.getValueAt(row, 5);
                String numeroAffaire = (String) tableModel.getValueAt(row, 0);
                mettreAJourDescriptionEnBD(numeroAffaire, nouveauTexte);
            }
        });

        tableAffaires.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
        
        // Remplacement de l'action de la colonne 6 pour un clic direct
        tableAffaires.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = tableAffaires.rowAtPoint(e.getPoint());
                int col = tableAffaires.columnAtPoint(e.getPoint());
                
                // Si l'utilisateur clique sur la colonne "Fichiers" (colonne 6)
                if (col == 6 && row != -1 && e.getClickCount() == 1) {
                    String numAffaire = (String) tableModel.getValueAt(row, 0);
                    voirDocumentsAffaire(numAffaire);
                }
            }
        });

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

                if (column == 5) {
                    label.setText(texteColonne + " [ ? ]");
                    label.setToolTipText("<html><b>Aide Description :</b><br>• Clic simple : Modifier la description<br>• Double-clic : Accéder aux détails de l'affaire</html>");
                } else if (column == 6) {
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
        
        tableAffaires.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        tableAffaires.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);
        tableAffaires.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
        tableAffaires.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);
        
        DefaultTableCellRenderer statutRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(JLabel.CENTER);
                setFont(new Font("Segoe UI", Font.BOLD, 15));

                if (value != null) {
                    String statut = value.toString().trim();
                    if (statut.equalsIgnoreCase("En cours")) {
                        c.setForeground(new Color(40, 167, 69)); // Vert
                    } else if (statut.equalsIgnoreCase("Traité et classé")) {
                        c.setForeground(new Color(220, 53, 69)); // Rouge
                    } else {
                        c.setForeground(isSelected ? table.getSelectionForeground() : Color.BLACK);
                    }
                }
                return c;
            }
        };
        tableAffaires.getColumnModel().getColumn(4).setCellRenderer(statutRenderer);

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
        tableAffaires.getColumnModel().getColumn(5).setCellRenderer(leftRenderer);

        // Boutons actions (Fichiers)
        tableAffaires.getColumnModel().getColumn(6).setCellRenderer(new ButtonRenderer(folderIcon));
        tableAffaires.getColumnModel().getColumn(6).setCellEditor(new ButtonEditor(this, folderIcon, tableAffaires, tableModel));
        tableAffaires.getColumnModel().getColumn(6).setMaxWidth(120);
        tableAffaires.getColumnModel().getColumn(6).setMinWidth(120);

        // Double-clic pour ouvrir les détails
        tableAffaires.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int ligneSelectionnee = tableAffaires.getSelectedRow();
                    if (ligneSelectionnee != -1) {
                        String numAffaire = (String) tableModel.getValueAt(ligneSelectionnee, 0);
                        String commune = (String) tableModel.getValueAt(ligneSelectionnee, 1);
                        String description = (String) tableModel.getValueAt(ligneSelectionnee, 5);
                        ouvrirDetailsAffaire(numAffaire, commune, description);
                    }
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(tableAffaires);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(190, 195, 200), 1));
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // Pied de page explicatif
        JLabel lblInfo = new JLabel("💡 Astuce : Double-cliquez sur une ligne pour afficher les détails. Cliquez sur l'icône dossier 📁 pour uploader des fichiers.");
        lblInfo.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblInfo.setForeground(new Color(90, 100, 110));
        mainPanel.add(lblInfo, BorderLayout.SOUTH);

        add(mainPanel);

        // --- Écouteurs pour déclencher le filtrage dynamique en temps réel ---
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
            "a.numero_affaire, a.description, a.statut, o.ville, o.section, o.parcelle " +
            "FROM affaires a " +
            "LEFT JOIN opposants o ON a.id = o.affaire_id WHERE 1=1"
        );

        String texteRecherche = (txtRecherche != null) ? txtRecherche.getText().trim() : "";
        String statutSelectionne = (comboStatutFiltre != null) ? (String) comboStatutFiltre.getSelectedItem() : "Tous les statuts";

        if (!texteRecherche.isEmpty()) {
            query.append(" AND (a.numero_affaire ILIKE ? OR o.ville ILIKE ? OR a.description ILIKE ?)");
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
                pstmt.setString(paramIndex++, motif);
                pstmt.setString(paramIndex++, motif);
                pstmt.setString(paramIndex++, motif);
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

    private void ouvrirDetailsAffaire(String numAffaire, String commune, String description) {
    JDialog dialogDetails = new JDialog(this, "Détails de l'affaire : " + numAffaire, true);
    dialogDetails.setSize(850, 600);
    dialogDetails.setLocationRelativeTo(this);

    // Panneau principal au design clair et professionnel
    JPanel panelPrincipal = new JPanel(new BorderLayout(0, 15));
    panelPrincipal.setBackground(new Color(245, 247, 250));
    panelPrincipal.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

    // --- EN-TÊTE CLAIR ET STRUCTURÉ ---
    JPanel panelHeader = new JPanel();
    panelHeader.setLayout(new BoxLayout(panelHeader, BoxLayout.Y_AXIS));
    panelHeader.setOpaque(false);

    JLabel lblTitreDetail = new JLabel("Dossier : " + numAffaire);
    lblTitreDetail.setFont(new Font("Segoe UI", Font.BOLD, 22));
    lblTitreDetail.setForeground(new Color(33, 37, 41));
    panelHeader.add(lblTitreDetail);
    panelHeader.add(Box.createVerticalStrut(8));

    JLabel lblInfos = new JLabel("<html><b>Commune :</b> " + (commune != null ? commune : "N/A") + 
                                 " &nbsp;&nbsp;|&nbsp;&nbsp; <b>Description :</b> " + (description != null ? description : "N/A") + "</html>");
    lblInfos.setFont(new Font("Segoe UI", Font.PLAIN, 13));
    lblInfos.setForeground(new Color(108, 117, 125));
    panelHeader.add(lblInfos);

    panelPrincipal.add(panelHeader, BorderLayout.NORTH);

    // --- ZONE DE TRAVAIL LIBRE (Layout Null pour déplacement libre dans toutes les directions) ---
    JPanel panelCentre = new JPanel(null); // Layout null pour positionnement libre absolu
    panelCentre.setBackground(Color.WHITE);
    panelCentre.setBorder(BorderFactory.createTitledBorder(
        BorderFactory.createLineBorder(new Color(206, 212, 218), 1),
        " Parties prenantes (Glissez les cartes librement dans le cadre) ",
        javax.swing.border.TitledBorder.LEFT,
        javax.swing.border.TitledBorder.TOP,
        new Font("Segoe UI", Font.BOLD, 13),
        new Color(73, 80, 87)
    ));

    // Chargement et positionnement initial des cartes libres
    chargerOpposantsCartesLibres(numAffaire, panelCentre);

    panelPrincipal.add(panelCentre, BorderLayout.CENTER);

    dialogDetails.add(panelPrincipal);
    dialogDetails.setVisible(true);
}

// Méthode de génération des cartes avec déplacement libre (X et Y) à la souris
private void chargerOpposantsCartesLibres(String numeroAffaire, JPanel containerLibre) {
    String query = "SELECT o.nom_prenom_ou_raison_sociale, o.ref_dossier " +
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

                // Création de la carte avec bordure en tirets
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
                carte.setBounds(x, y, 245, 125);
                carte.setOpaque(false);
                carte.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
                carte.setCursor(new Cursor(Cursor.DEFAULT_CURSOR)); // Curseur par défaut pour permettre la sélection de texte

                JLabel lblTiret = new JLabel("- - - - - - - - - - - - - - - - -");
                lblTiret.setFont(new Font("Segoe UI", Font.BOLD, 11));
                lblTiret.setForeground(new Color(134, 142, 150));
                lblTiret.setAlignmentX(Component.LEFT_ALIGNMENT);
                carte.add(lblTiret);
                carte.add(Box.createVerticalStrut(4));

                // --- CHAMPS SÉLECTIONNABLES POUR LE COPIER / COLLER ---
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

                // --- GESTION DU DÉPLACEMENT LIBRE ET DU FOCUS CLAVIER ---
                // Le déplacement reste actif si on clique sur les zones vides de la carte,
                // mais le texte garde la priorité pour la sélection (Ctrl+C).
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
                        // Empêche le glisser-déposer si l'utilisateur est en train de surligner du texte avec la souris
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
                
                // Propagation aux champs internes pour un déplacement fluide global
                txtNom.addMouseListener(moveOrSelect);
                txtNom.addMouseMotionListener(moveOrSelect);
                txtRef.addMouseListener(moveOrSelect);
                txtRef.addMouseMotionListener(moveOrSelect);

                containerLibre.add(carte);

                x += 270;
                index++;
                if (index % 2 == 0) {
                    x = 20;
                    y += 145;
                }
            }

            if (!hasData) {
                JLabel lblAucun = new JLabel("Aucun opposant enregistré pour cette affaire.");
                lblAucun.setFont(new Font("Segoe UI", Font.ITALIC, 14));
                lblAucun.setForeground(new Color(108, 117, 125));
                lblAucun.setBounds(20, 30, 300, 25);
                containerLibre.add(lblAucun);
            }
        }

    } catch (SQLException e) {
        JOptionPane.showMessageDialog(null, "Erreur de chargement des opposants.", "Erreur SQL", JOptionPane.ERROR_MESSAGE);
        e.printStackTrace();
    }
}

// Méthode de génération des cartes avec interactivité de déplacement à la souris
private void chargerOpposantsCartesMobiles(String numeroAffaire, JPanel containerCartes) {
    String query = "SELECT o.nom_prenom_ou_raison_sociale, o.ref_dossier " +
                   "FROM opposants o " +
                   "JOIN affaires a ON o.affaire_id = a.id " +
                   "WHERE a.numero_affaire = ?";

    try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
         PreparedStatement pstmt = conn.prepareStatement(query)) {

        pstmt.setString(1, numeroAffaire);
        try (ResultSet rs = pstmt.executeQuery()) {
            boolean hasData = false;
            while (rs.next()) {
                hasData = true;
                String nom = rs.getString("nom_prenom_ou_raison_sociale");
                String ref = rs.getString("ref_dossier");

                // Création de la carte visuelle style "Netflix"
                JPanel carte = new JPanel() {
                    @Override
                    protected void paintComponent(Graphics g) {
                        super.paintComponent(g);
                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        g2.setColor(new Color(32, 38, 52));
                        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                        g2.setColor(new Color(70, 130, 255)); // Bordure bleutée interactive
                        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
                        g2.dispose();
                    }
                };
                carte.setLayout(new BoxLayout(carte, BoxLayout.Y_AXIS));
                carte.setPreferredSize(new Dimension(220, 130));
                carte.setOpaque(false);
                carte.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
                carte.setCursor(new Cursor(Cursor.HAND_CURSOR));

                JLabel lblBadge = new JLabel("🖱️ Glisser pour déplacer");
                lblBadge.setFont(new Font("Segoe UI", Font.BOLD, 11));
                lblBadge.setForeground(new Color(13, 110, 253));
                carte.add(lblBadge);
                carte.add(Box.createVerticalStrut(8));

                JLabel lblNom = new JLabel("<html><div style='width:180px;'><b>" + nom + "</b></div></html>");
                lblNom.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                lblNom.setForeground(Color.WHITE);
                carte.add(lblNom);
                carte.add(Box.createVerticalStrut(6));

                JLabel lblRef = new JLabel("Réf : " + (ref != null ? ref : "N/A"));
                lblRef.setFont(new Font("Segoe UI", Font.ITALIC, 12));
                lblRef.setForeground(new Color(150, 165, 185));
                carte.add(lblRef);

                // --- LOGIQUE DE DÉPLACEMENT (Drag & Drop des cartes dans le conteneur) ---
                MouseAdapter dragController = new MouseAdapter() {
                    private int pointX, pointY;

                    @Override
                    public void mousePressed(MouseEvent e) {
                        pointX = e.getX();
                        pointY = e.getY();
                        containerCartes.setComponentZOrder(carte, 0); // Amène la carte au premier plan pendant le déplacement
                    }

                    @Override
                    public void mouseDragged(MouseEvent e) {
                        int currentX = carte.getX() + e.getX() - pointX;
                        int currentY = carte.getY() + e.getY() - pointY;
                        
                        // Permet de réordonner visuellement en changeant l'index dans le FlowLayout
                        int indexCourant = java.util.Arrays.asList(containerCartes.getComponents()).indexOf(carte);
                        int nouvellePosition = Math.max(0, Math.min(containerCartes.getComponentCount() - 1, currentX / 230));
                        
                        if (indexCourant != nouvellePosition && nouvellePosition >= 0) {
                            containerCartes.remove(carte);
                            containerCartes.add(carte, nouvellePosition);
                            containerCartes.revalidate();
                            containerCartes.repaint();
                        }
                    }
                };

                carte.addMouseListener(dragController);
                carte.addMouseMotionListener(dragController);

                containerCartes.add(carte);
            }

            if (!hasData) {
                JLabel lblAucun = new JLabel("  Aucun opposant enregistré pour cette affaire.");
                lblAucun.setFont(new Font("Segoe UI", Font.ITALIC, 14));
                lblAucun.setForeground(new Color(150, 165, 185));
                containerCartes.add(lblAucun);
            }
        }

    } catch (SQLException e) {
        JOptionPane.showMessageDialog(this, "Erreur de chargement des opposants.", "Erreur SQL", JOptionPane.ERROR_MESSAGE);
        e.printStackTrace();
    }
}

    private void chargerOpposantsPourAffaire(String numeroAffaire, DefaultListModel<String> listModel) {
        String query = "SELECT o.nom_prenom_ou_raison_sociale, o.ref_dossier " +
                       "FROM opposants o " +
                       "JOIN affaires a ON o.affaire_id = a.id " +
                       "WHERE a.numero_affaire = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, numeroAffaire);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String nom = rs.getString("nom_prenom_ou_raison_sociale");
                    String ref = rs.getString("ref_dossier");
                    listModel.addElement("  • " + nom + (ref != null ? " (Réf: " + ref + ")" : ""));
                }
            }

            if (listModel.isEmpty()) {
                listModel.addElement("  Aucun opposant enregistré pour cette affaire.");
            }

        } catch (SQLException e) {
            listModel.addElement("  Erreur de chargement des opposants.");
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
    File dossierAffaire = new File("uploads/" + numeroAffaire + "/_commun");
    
    if (!dossierAffaire.exists()) {
        dossierAffaire = new File("uploads/" + numeroAffaire);
    }
    
    if (!dossierAffaire.exists() || dossierAffaire.listFiles() == null || dossierAffaire.listFiles().length == 0) {
        JOptionPane.showMessageDialog(this, 
            "Aucun document trouvé pour l'affaire : " + numeroAffaire, 
            "Documents", 
            JOptionPane.INFORMATION_MESSAGE);
        return;
    }

    File[] fichiers = dossierAffaire.listFiles();
    java.util.List<File> fichiersPdf = new java.util.ArrayList<>();
    for (File f : fichiers) {
        if (f.getName().toLowerCase().endsWith(".pdf")) {
            fichiersPdf.add(f);
        }
    }

    if (fichiersPdf.isEmpty()) {
        JOptionPane.showMessageDialog(this, 
            "Aucun fichier PDF n'est présent dans le dossier de cette affaire.", 
            "Fichiers introuvables", 
            JOptionPane.WARNING_MESSAGE);
        return;
    }

    if (fichiersPdf.size() == 1) {
        ouvrirFichierSysteme(fichiersPdf.get(0));
    } else {
        JDialog dialog = new JDialog(this, "Documents de l'affaire : " + numeroAffaire, true);
        dialog.setSize(650, 420);
        dialog.setLocationRelativeTo(this);
        
        JPanel panelPrincipal = new JPanel(new BorderLayout(15, 15));
        panelPrincipal.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panelPrincipal.setBackground(new Color(245, 247, 250));

        JLabel lblTitre = new JLabel("Liste des documents disponibles (" + fichiersPdf.size() + ") - Double-cliquez pour ouvrir un fichier :");
        lblTitre.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblTitre.setForeground(new Color(33, 37, 41));
        panelPrincipal.add(lblTitre, BorderLayout.NORTH);

        DefaultListModel<File> listModel = new DefaultListModel<>();
        for (File f : fichiersPdf) {
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

        // --- ICI : Uniquement le bouton Fermer, aucun autre bouton créé ---
        JPanel panelBoutons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        panelBoutons.setOpaque(false);

        JButton btnFermer = new JButton("Fermer");
        btnFermer.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        btnFermer.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnFermer.addActionListener(e -> dialog.dispose());

        panelBoutons.add(btnFermer);
        panelPrincipal.add(panelBoutons, BorderLayout.SOUTH);

        // Double-clic pour ouvrir le fichier instantanément
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
}

    // Méthode unique et propre pour le rendu des fichiers avec une icône de document
    // Méthode de rendu propre sans emoji pour éviter les rectangles sous Java
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
    
    public void gererUploadFichier(String numeroAffaire) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Charger un document global pour l'affaire : " + numeroAffaire);
        int userSelection = fileChooser.showOpenDialog(this);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fichierSource = fileChooser.getSelectedFile();
            
            File dossierCible = new File("uploads/" + numeroAffaire + "/_commun");
            if (!dossierCible.exists()) {
                dossierCible.mkdirs();
            }

            File fichierDestination = new File(dossierCible, fichierSource.getName());

            try {
                java.nio.file.Files.copy(fichierSource.toPath(), fichierDestination.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                JOptionPane.showMessageDialog(this, 
                    "Document global de l'affaire chargé avec succès dans :\n" + fichierDestination.getAbsolutePath(), 
                    "Upload réussi", 
                    JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, 
                    "Erreur lors de la copie du fichier :\n" + e.getMessage(), 
                    "Erreur", 
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> {
            new DashboardFrame().setVisible(true);
        });
    }
}