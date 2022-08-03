package com.example.page.indexModel;

import com.example.page.lemmaModel.Lemma;
import com.example.page.pageModel.Page;
import org.hibernate.annotations.Type;

import javax.persistence.*;

@Entity
@Table(name = "indexes")
public class Index {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
@ManyToOne
@JoinColumn(name = "page_id",referencedColumnName = "id")
    private Page pageId;
    @ManyToOne
    @JoinColumn(name = "lemma_id",referencedColumnName = "id")
    private Lemma lemmaId;
@Column(name = "ranks")
@Type(type = "float")
    private float rank;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Page getPageId() {
        return pageId;
    }

    public void setPageId(Page pageId) {
        this.pageId = pageId;
    }

    public Lemma getLemmaId() {
        return lemmaId;
    }

    public void setLemmaId(Lemma lemmaId) {
        this.lemmaId = lemmaId;
    }

    public float getRank() {
        return rank;
    }

    public void setRank(float rank) {
        this.rank = rank;
    }
}
