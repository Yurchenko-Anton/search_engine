package com.example.controllers.site;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;


public interface SiteRepository extends JpaRepository<Site,Integer> {
    @Query("SELECT count(u) FROM Site u GROUP BY u.url")
    List<Integer> countAllByUrl();
    @Query("SELECT u FROM Site u GROUP BY u.url")
    List<Site> findByUrl();
    @Query("SELECT u FROM Site u WHERE u.url like :url")
    List<Site> findAllByUrl(String url);
List<Site> findByName(String name);
}
