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

        JLabel lblTitre = new JLabel("Gestion et Suivi des Litiges Foncier-Administratives");
        lblTitre.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitre.setForeground(new Color(33, 37, 41));
        lblTitre.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        mainPanel.add(lblTitre, BorderLayout.NORTH);

        String[] colonnes = {"Numéro affaire", "Commune", "Section", "Parcelle", "Statut", "Description", "Fichiers"};
        
        tableModel = new DefaultTableModel(new Object[][]{}, colonnes) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 5 || column == 6; 
            }
        };

        tableAffaires = new JTable(tableModel);

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
        
        tableAffaires.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = tableAffaires.rowAtPoint(e.getPoint());
                int col = tableAffaires.columnAtPoint(e.getPoint());
                
                if (col == 5 && row != -1 && e.getClickCount() == 1) {
                    if (tableAffaires.isCellEditable(row, col)) {
                        tableAffaires.editCellAt(row, col);
                        Component editorComp = tableAffaires.getEditorComponent();
                        if (editorComp != null) {
                            editorComp.requestFocusInWindow();
                        }
                    }
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

        DefaultTableCellRenderer leftRenderer = new DefaultTableCellRenderer();
        leftRenderer.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        tableAffaires.getColumnModel().getColumn(5).setCellRenderer(leftRenderer);

        // Utilisation des classes externes ButtonRenderer et ButtonEditor
        tableAffaires.getColumnModel().getColumn(6).setCellRenderer(new ButtonRenderer(folderIcon));
        tableAffaires.getColumnModel().getColumn(6).setCellEditor(new ButtonEditor(this, folderIcon, tableAffaires, tableModel));

        tableAffaires.getColumnModel().getColumn(6).setMaxWidth(120);
        tableAffaires.getColumnModel().getColumn(6).setMinWidth(120);

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

        JLabel lblInfo = new JLabel("💡 Astuce : Double-cliquez sur une ligne pour afficher les détails. Cliquez sur l'icône dossier 📁 pour uploader des fichiers.");
        lblInfo.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblInfo.setForeground(new Color(90, 100, 110));
        mainPanel.add(lblInfo, BorderLayout.SOUTH);

        add(mainPanel);
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
        String query = "SELECT DISTINCT ON (a.numero_affaire) " +
                       "a.numero_affaire, " +
                       "a.description, " +
                       "a.statut, " +
                       "o.ville, " +
                       "o.section, " +
                       "o.parcelle " +
                       "FROM affaires a " +
                       "LEFT JOIN opposants o ON a.id = o.affaire_id " +
                       "ORDER BY a.numero_affaire";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

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
        dialogDetails.setSize(550, 450);
        dialogDetails.setLocationRelativeTo(this);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.setBackground(Color.WHITE);

        JLabel lblTitreDetail = new JLabel("Dossier : " + numAffaire);
        lblTitreDetail.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitreDetail.setForeground(new Color(13, 110, 253));
        panel.add(lblTitreDetail);
        panel.add(Box.createVerticalStrut(15));

        JLabel lblCommune = new JLabel("📍 Commune : " + (commune != null ? commune : "N/A"));
        lblCommune.setFont(new Font("Segoe UI", Font.BOLD, 15));
        panel.add(lblCommune);
        panel.add(Box.createVerticalStrut(8));
        
        JLabel lblDesc = new JLabel("📝 Description : " + (description != null ? description : "N/A"));
        lblDesc.setFont(new Font("Segoe UI", Font.BOLD, 15));
        panel.add(lblDesc);
        
        panel.add(Box.createVerticalStrut(20));
        
        JLabel lblProtagonistesTitre = new JLabel("👥 Parties prenantes (Opposants) :");
        lblProtagonistesTitre.setFont(new Font("Segoe UI", Font.BOLD, 16));
        panel.add(lblProtagonistesTitre);
        panel.add(Box.createVerticalStrut(10));
        
        DefaultListModel<String> listModelProtagonistes = new DefaultListModel<>();
        chargerOpposantsPourAffaire(numAffaire, listModelProtagonistes);

        JList<String> listeProtagonistes = new JList<>(listModelProtagonistes);
        listeProtagonistes.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        listeProtagonistes.setFixedCellHeight(35);
        
        JScrollPane scrollProtagonistes = new JScrollPane(listeProtagonistes);
        scrollProtagonistes.setPreferredSize(new Dimension(480, 110));
        panel.add(scrollProtagonistes);

        dialogDetails.add(panel);
        dialogDetails.setVisible(true);
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
    File dossierAffaire = new File("uploads/" + numeroAffaire);
    
    if (!dossierAffaire.exists() || dossierAffaire.listFiles() == null || dossierAffaire.listFiles().length == 0) {
        JOptionPane.showMessageDialog(this, 
            "Aucun document trouvé pour l'affaire : " + numeroAffaire, 
            "Documents", 
            JOptionPane.INFORMATION_MESSAGE);
        return;
    }

    // Récupérer la liste des fichiers du dossier
    File[] fichiers = dossierAffaire.listFiles();
    
    // S'il n'y a qu'un seul fichier, on l'ouvre directement
    if (fichiers.length == 1) {
        ouvrirFichierSysteme(fichiers[0]);
    } else {
        // S'il y a plusieurs fichiers, on propose une liste déroulante ou une boîte de choix
        File fichierChoisi = (File) JOptionPane.showInputDialog(
            this,
            "Sélectionnez le document à ouvrir :",
            "Documents de l'affaire " + numeroAffaire,
            JOptionPane.QUESTION_MESSAGE,
            null,
            fichiers,
            fichiers[0]
        );
        
        if (fichierChoisi != null) {
            ouvrirFichierSysteme(fichierChoisi);
        }
    }
}

// Méthode utilitaire pour ouvrir un fichier avec le programme par défaut du système (Windows, etc.)
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
        fileChooser.setDialogTitle("Charger un document pour l'affaire : " + numeroAffaire);
        int userSelection = fileChooser.showOpenDialog(this);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fichierSource = fileChooser.getSelectedFile();
            
            File dossierCible = new File("uploads/" + numeroAffaire);
            if (!dossierCible.exists()) {
                dossierCible.mkdirs();
            }

            File fichierDestination = new File(dossierCible, fichierSource.getName());

            try {
                java.nio.file.Files.copy(fichierSource.toPath(), fichierDestination.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                JOptionPane.showMessageDialog(this, 
                    "Document chargé avec succès dans :\n" + fichierDestination.getAbsolutePath(), 
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