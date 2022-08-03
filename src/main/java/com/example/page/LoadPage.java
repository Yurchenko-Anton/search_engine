package com.example.page;

import com.example.controllers.site.SiteRepository;
import com.example.page.fieldModel.FieldRepository;
import com.example.page.indexModel.Index;
import com.example.page.indexModel.IndexRepository;
import com.example.page.lemmaModel.LemmaRepository;
import com.example.page.pageModel.PageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

@Controller
public class LoadPage{
     PageRepository pageRepository;
     IndexRepository indexRepository;
     LemmaRepository lemmaRepository;
     FieldRepository fieldRepository;
     SiteRepository siteRepository;
     UrlProperties urlProperties;
    @Autowired
    public LoadPage(UrlProperties urlProperties,PageRepository pageRepository, IndexRepository indexRepository, LemmaRepository lemmaRepository, FieldRepository fieldRepository, SiteRepository siteRepository) {
     this.urlProperties = urlProperties;
        this.pageRepository = pageRepository;
        this.indexRepository = indexRepository;
        this.lemmaRepository = lemmaRepository;
        this.fieldRepository = fieldRepository;
        this.siteRepository = siteRepository;
    }

    @RequestMapping("/startloadpage")
    public String run(String... args) throws Exception {
    getMapList();
        Logger.getLogger(LoadPage.class.getName()).info("DownloadFinished");
        return "finishLoad";
    }
    private  void getMapList () throws InterruptedException, IOException {
    String[] url = urlProperties.getUrl();
    for (int i=0 ; i < url.length; i++){
        url[i] = url[i] + "/";
    }
    for (int i =0 ; i < url.length; i++){
        LoadPageService loadPageService = new LoadPageService(urlProperties,pageRepository,indexRepository,lemmaRepository,fieldRepository,siteRepository);
        System.out.println(url[i].trim());
        loadPageService.getUrl(url[i].trim());
        ForkJoinPool pool = new ForkJoinPool();
        pool.execute(loadPageService);
        do {
            TimeUnit.MILLISECONDS.sleep(150);
        }
        while (!loadPageService.isDone());
        pool.shutdown();
    }
}
    public  PageRepository getPageRepository() {
        return pageRepository;
    }

    public  IndexRepository getIndexRepository() {
        return indexRepository;
    }

    public  LemmaRepository getLemmaRepository() {
        return lemmaRepository;
    }

    public  FieldRepository getFieldRepository() {
        return fieldRepository;
    }
}
