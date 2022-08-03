package com.example.page.lemmaModel;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.transaction.Transactional;
import java.util.List;

public interface LemmaRepository extends JpaRepository<Lemma, Integer> {
    Lemma findFirstByLemmaLike(String lemma);
    @Transactional
    @Modifying
    @Query("update Lemma l set l.frequency = l.frequency+1 where l.lemma like :lemma")
    void setNewFrequency(@Param(value = "lemma") String s);
}
