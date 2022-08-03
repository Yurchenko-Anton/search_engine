package com.example.controllers;

import com.example.controllers.site.SiteRepository;
import com.example.page.LoadPageService;
import com.example.page.SearchMethod;
import com.example.page.UrlProperties;
import com.example.page.fieldModel.FieldRepository;
import com.example.page.indexModel.IndexRepository;
import com.example.page.lemmaModel.Lemma;
import com.example.page.lemmaModel.LemmaRepository;
import com.example.page.pageModel.PageRepository;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.atomic.AtomicInteger;

@RestController
@RequestMapping("/api")
public class ApiController {
    @Autowired
    PageRepository pageRepository;
    @Autowired
    IndexRepository indexRepository;
    @Autowired
    LemmaRepository lemmaRepository;
    @Autowired
    FieldRepository fieldRepository;
    @Autowired
    SiteRepository siteRepository;
    @Autowired
    UrlProperties urlProperties;
    @Autowired
    IndexingSites indexingSites;
    @Autowired
    LoadPageService loadPageService;
    @Autowired
    SearchMethod searchMethod;


@GetMapping("/startIndexing")
public JSONObject startIndex(){
    JSONObject info = new JSONObject();
    if (indexingSites.getStarted()== null) {
        indexingSites.setStarted(true);
        indexingSites.run();
        info.put("result", true);
        return info;
    }
else {
    info.put("result", false);
    info.put("error", "Индексация уже запущена");
        return info;
    }
}
@GetMapping("/stopIndexing")
    public JSONObject stopIndex(){
    JSONObject info = new JSONObject();
    if (indexingSites.getStarted() !=null && indexingSites.getStarted().equals(true)) {
        indexingSites.setStarted(false);
        info.put("result", false);
        return info;
    }
    else {
        info.put("result", true);
        info.put("error", "Индексация не запущена");
        return info;
    }
    }
    @PostMapping("/indexPage")
    public JSONObject indexPage(@RequestParam(name = "url") String url){
        JSONObject info = new JSONObject();
        for (int i=0; i < urlProperties.getUrl().length; i++){
        if (url.contains(urlProperties.getUrl()[i])) {
            if (siteRepository.findByName(url).size() > 0){
                indexRepository.findByPageId(pageRepository.findFirstByPathLike(url)).forEach(index -> {
                   Lemma lemma = index.getLemmaId();
                   lemma.setFrequency(lemma.getFrequency()-1);
                   lemmaRepository.save(lemma);
                });
            }
            loadPageService.addNonLeadSite(url);
            info.put("result", true);
            break;
        }
        else if (i== urlProperties.getUrl().length-1){
            info.put("result", false);
            info.put("error","Данная страница находится за пределами сайтов, указанных в конфигурационном файле");
        }
        }
        return info;
    }
    @GetMapping("/statistics")
    public JSONObject getStatistics(){
        JSONObject info = new JSONObject();
        JSONObject statistics = new JSONObject();
        JSONObject total = new JSONObject();
        JSONArray detailed = new JSONArray();
        info.put("result", true);
        total.put("sites", siteRepository.findByUrl().size());
        total.put("pages", pageRepository.findAll().size());
        total.put("lemmas", lemmaRepository.findAll().size());
        total.put("isIndexing", false);
        statistics.put("total", total);
        for (int i=0; i < siteRepository.findByUrl().size(); i ++){
            JSONObject timeObject = new JSONObject();
            if (siteRepository.findByUrl().get(i).getName().lastIndexOf("/") == siteRepository.findByUrl().get(i).getName().length()-1){
                timeObject.put("url", siteRepository.findByUrl().get(i).getName().substring(0,siteRepository.findByUrl().get(i).getName().lastIndexOf("/")));
            }
            else {
                timeObject.put("url", siteRepository.findByUrl().get(i).getName());
            }
            for (int j=0; j < urlProperties.getName().length; j ++){
                if (siteRepository.findByUrl().get(i).getName().contains(urlProperties.getUrl()[j])){
                    timeObject.put("name", urlProperties.getName()[j]);
                }
            }
timeObject.put("status", siteRepository.findByUrl().get(i).getStatus());
timeObject.put("statusTime", siteRepository.findByUrl().get(i).getStatusTime());
timeObject.put("error", siteRepository.findByUrl().get(i).getLastError());
timeObject.put("pages",siteRepository.countAllByUrl().get(i));
AtomicInteger count = new AtomicInteger();
siteRepository.findAllByUrl(siteRepository.findByUrl().get(i).getUrl()).forEach(site -> {
    count.addAndGet(indexRepository.findByPageId(pageRepository.findFirstByPathLike(site.getName())).size());
});
timeObject.put("lemmas", count);
            detailed.add(timeObject);
        }
        statistics.put("detailed", detailed);
        info.put("statistics", statistics);


        return info;
    }
    @GetMapping("/search")
    public JSONObject getSearch(@RequestParam String query, @RequestParam(name = "site",required = false) String site){
        JSONObject info = new JSONObject();
    if (query.length() == 0){
        info.put("result", false);
        info.put("error", "Задан пустой поисковой запрос");
        return info;
    }
    else {
        info.put("result", true);
        info.put("limit", 5);
        info.put("offset", 5);
        JSONArray data = searchMethod.run(query, site);
        info.put("count", data.size());
        info.put("data", data);
        return info;
    }
    }
}
