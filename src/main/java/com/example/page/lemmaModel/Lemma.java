package com.example.page.lemmaModel;

import com.example.controllers.site.Site;

import javax.persistence.*;

@Entity
@Table(name = "lemma")
public class Lemma {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
private String lemma;
private int frequency;
@ManyToOne
@JoinColumn(name = "site_id",referencedColumnName = "id")
private Site site;

    public Site getSite() {
        return site;
    }

    public void setSite(Site site) {
        this.site = site;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getLemma() {
        return lemma;
    }

    public void setLemma(String lemma) {
        this.lemma = lemma;
    }

    public int getFrequency() {
        return frequency;
    }

    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }
}
