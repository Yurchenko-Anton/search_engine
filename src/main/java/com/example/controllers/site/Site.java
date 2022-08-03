package com.example.controllers.site;

import com.fasterxml.jackson.annotation.JsonSubTypes;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "site")
public class Site {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
@Enumerated(EnumType.STRING)
    @Column(columnDefinition = "status")
private IndexType status;
@Column(name = "status_time")
private Date statusTime;
@Column(name = "last_error")
    private String lastError;
private String url;
private String name;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public IndexType getStatus() {
        return status;
    }

    public void setStatus(IndexType status) {
        this.status = status;
    }

    public Date getStatusTime() {
        return statusTime;
    }

    public void setStatusTime(Date statusTime) {
        this.statusTime = statusTime;
    }

    public String getLastError() {
        return lastError;
    }

    public void setLastError(String lastError) {
        this.lastError = lastError;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
