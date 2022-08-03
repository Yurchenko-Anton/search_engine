package com.example.page.pageModel;

import com.example.controllers.site.Site;
import com.example.page.lemmaModel.Lemma;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "page")
public class Page {
@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
private int id;
private String path;
private int code;
private String content;
@OneToOne
@JoinColumn(name = "site_id", referencedColumnName = "id")
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

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
