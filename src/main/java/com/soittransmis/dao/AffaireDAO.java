package com.soittransmis.dao;

import com.soittransmis.models.Affaire;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AffaireDAO {
    private static final String DB_URL = "jdbc:postgresql://localhost:5432/soit_transmis_db";
    private static final String DB_USER = "postgres";
    private static final String DB_PASSWORD = "postgres";

    public int insererAffaire(String numAffaire, String description, String statut, int creePar) throws SQLException {
        String sql = "INSERT INTO affaires (numero_affaire, description, statut, cree_par) VALUES (?, ?, ?, ?) RETURNING id";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, numAffaire);
            pstmt.setString(2, description);
            pstmt.setString(3, statut);
            pstmt.setInt(4, creePar);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");
                }
            }
        }
        return -1;
    }

    public void insererOpposant(Connection conn, int affaireId, String nom, String refDossier, 
                                String ville, String section, String parcelle, 
                                String lieuDit, String contact, String creePar) throws SQLException {
        String sql = "INSERT INTO opposants (affaire_id, nom_prenom_ou_raison_sociale, ref_dossier, ville, section, parcelle, lieu_dit, contact, cree_par) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, affaireId);
            pstmt.setString(2, nom);
            setNullableString(pstmt, 3, refDossier);
            setNullableString(pstmt, 4, ville);
            setNullableString(pstmt, 5, section);
            setNullableString(pstmt, 6, parcelle);
            setNullableString(pstmt, 7, lieuDit);
            setNullableString(pstmt, 8, contact);
            pstmt.setString(9, creePar);
            pstmt.executeUpdate();
        }
    }

    private void setNullableString(PreparedStatement pstmt, int index, String val) throws SQLException {
        if (val == null || val.isEmpty()) {
            pstmt.setNull(index, Types.VARCHAR);
        } else {
            pstmt.setString(index, val);
        }
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }
}