package com.example.page.indexModel;

import com.example.page.lemmaModel.Lemma;
import com.example.page.pageModel.Page;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Set;

public interface IndexRepository extends JpaRepository<Index,Integer> {
    Set<Index> findByLemmaId(Lemma lemma);
    List<Index> findByPageId(Page page);
    Index findByPageIdAndLemmaId(Page page, Lemma lemma);
}
