package com.soittransmis.models;

import java.util.Date;

public class Affaire {
    private int id;
    private String numeroAffaire;
    private String objet;
    private String statut;
    private Date dateCreation;
    // Ajoutez ici les autres champs selon votre base de données (cadastre, etc.)

    // Constructeurs
    public Affaire() {}

    public Affaire(String numeroAffaire, String objet, String statut) {
        this.numeroAffaire = numeroAffaire;
        this.objet = objet;
        this.statut = statut;
    }

    // Getters et Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNumeroAffaire() { return numeroAffaire; }
    public void setNumeroAffaire(String numeroAffaire) { this.numeroAffaire = numeroAffaire; }

    public String getObjet() { return objet; }
    public void setObjet(String objet) { this.objet = objet; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    public Date getDateCreation() { return dateCreation; }
    public void setDateCreation(Date dateCreation) { this.dateCreation = dateCreation; }
}