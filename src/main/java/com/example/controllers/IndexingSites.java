package com.example.controllers;

import com.example.controllers.site.IndexType;
import com.example.controllers.site.Site;
import com.example.controllers.site.SiteRepository;
import com.example.page.UrlProperties;
import com.example.page.fieldModel.FieldRepository;
import com.example.page.indexModel.Index;
import com.example.page.indexModel.IndexRepository;
import com.example.page.lemmaModel.Lemma;
import com.example.page.lemmaModel.LemmaRepository;
import com.example.page.pageModel.Page;
import com.example.page.pageModel.PageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Controller
public class IndexingSites {
    PageRepository pageRepository;
    IndexRepository indexRepository;
    LemmaRepository lemmaRepository;
    FieldRepository fieldRepository;
    SiteRepository siteRepository;
    UrlProperties urlProperties;
    @Autowired
    public IndexingSites(PageRepository pageRepository, IndexRepository indexRepository, LemmaRepository lemmaRepository, FieldRepository fieldRepository, SiteRepository siteRepository, UrlProperties urlProperties) {
        this.pageRepository = pageRepository;
        this.indexRepository = indexRepository;
        this.lemmaRepository = lemmaRepository;
        this.fieldRepository = fieldRepository;
        this.siteRepository = siteRepository;
        this.urlProperties = urlProperties;
    }
    Boolean started;
    public void run(){
List<Site> sites = siteRepository.findAll();
        ExecutorService executors = Executors.newFixedThreadPool(20);
for (int i=0 ; i < sites.size(); i++){
    System.out.println(started);
    int finalI = i;
    try {
        executors.submit(() -> indexed(sites.get(finalI), 1));
        if (started == false) {
            executors.shutdown();
        }
    }catch (Exception e){
        indexed(sites.get(finalI),0);
    }
}
executors.shutdown();
setStarted(null);
    }
private void indexed(Site getSite, int number){
    Site site = getSite;
    Date date = new Date();
    if (number == 1) {
        Page page = pageRepository.findFirstByPathLike(site.getName());
        page.setSite(site);
        pageRepository.save(page);
        List<Index> indexList = indexRepository.findByPageId(pageRepository.findFirstByPathLike(site.getName()));
        indexList.forEach(index -> {
            Lemma lemma = index.getLemmaId();
            lemma.setSite(site);
            lemmaRepository.save(lemma);
        });
        site.setStatus(IndexType.INDEXED);
        site.setStatusTime(date);
        site.setLastError(null);
        siteRepository.save(site);
    }
    else {
        site.setStatus(IndexType.FAILED);
        site.setLastError("Преждевременная остановка индексации");
        site.setStatusTime(date);
        siteRepository.save(site);
    }
}

    public Boolean getStarted() {
        return started;
    }

    public void setStarted(Boolean started) {
        this.started = started;
    }
}
