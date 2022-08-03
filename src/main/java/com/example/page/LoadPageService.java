package com.example.page;

import com.example.controllers.site.IndexType;
import com.example.controllers.site.Site;
import com.example.controllers.site.SiteRepository;
import com.example.page.fieldModel.FieldRepository;
import com.example.page.indexModel.Index;
import com.example.page.indexModel.IndexRepository;
import com.example.page.lemmaModel.Lemma;
import com.example.page.lemmaModel.LemmaRepository;
import com.example.page.pageModel.Page;
import com.example.page.pageModel.PageRepository;
import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.english.EnglishLuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.RecursiveTask;

@Service

public class LoadPageService extends RecursiveTask<List<String>> {
    PageRepository pageRepository;
    IndexRepository indexRepository;
    LemmaRepository lemmaRepository;
    FieldRepository fieldRepository;
    SiteRepository siteRepository;
    @Autowired
    private UrlProperties urlProperties;
    @Autowired
    public LoadPageService(UrlProperties urlProperties, PageRepository pageRepository, IndexRepository indexRepository, LemmaRepository lemmaRepository, FieldRepository fieldRepository, SiteRepository siteRepository) {
        this.urlProperties = urlProperties;
        this.pageRepository = pageRepository;
        this.indexRepository = indexRepository;
        this.lemmaRepository = lemmaRepository;
        this.fieldRepository = fieldRepository;
        this.siteRepository = siteRepository;
    }



    private String url;
    private List<String> finalMap = new ArrayList<>();
    public void getUrl (String url){
        this.url =url;
    }
    @Override
    protected List<String> compute() {

        List<String> ready = null;
        try {
            ready = getHttp(url);
        } catch (IOException e) {
            e.printStackTrace();
        }
        List<LoadPageService> tasks = new ArrayList<>();
        for (String beg : ready) {
            if (beg.replace(url,"").length() > 0 && !finalMap.contains(beg)){
                LoadPageService task = new LoadPageService(urlProperties,pageRepository,indexRepository,lemmaRepository,fieldRepository,siteRepository);
                task.getUrl(beg);
                task.fork();
                finalMap.addAll(task.join());
                if (!finalMap.contains(beg))
                {
                    finalMap.add(beg);
                }
            }
            else if (!finalMap.contains(beg))
            {
                finalMap.add(beg);
            }
        }
        return finalMap;
    }
    private List<String> getHttp(String url) throws IOException {
        List<String> get = new ArrayList<>();
        try {
            Connection.Response response = Jsoup.connect(url).userAgent(urlProperties.getUserAgent()).referrer("http://www.google.com").maxBodySize(0).execute();
            Elements elements =response.parse().select("a");
            savePage(url,response.statusCode(),response.body(), response);
            elements.forEach(element -> {
                String absHerf= element.attr("abs:href");
                if ((absHerf.contains(url) && absHerf.lastIndexOf('/') == absHerf.length()-1))  {
                    get.add(absHerf);
                }
                else if (absHerf.contains(url)){
                    if (!finalMap.contains(absHerf) && absHerf.lastIndexOf("#")+1 != absHerf.length() && !absHerf.contains("auth")) {
                        addNonLeadSite(absHerf);
                    }
                }
            });
        }
        catch (Exception e){
            e.printStackTrace();
            System.out.println("Не удалось получить доступ");
        }
        return get;
    }
    public void addNonLeadSite(String absHerf){
        try {
            System.out.println(absHerf);
            Connection.Response response = Jsoup.connect(absHerf).userAgent(urlProperties.getUserAgent()).referrer("http://www.google.com").maxBodySize(0).execute();
            savePage(absHerf,response.statusCode(),response.body(), response);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Не удалось получить доступ");
        }
        finalMap.add(absHerf);
    }
    private void savePage(String path, int code, String content, Connection.Response response){
        try {
            Page page;
            if (pageRepository.findFirstByPathLike(path) != null){
                 page = pageRepository.findFirstByPathLike(path);
            }
            else {
                 page = new Page();
            }
            page.setPath(path);
            page.setCode(code);
            page.setContent(content);
            pageRepository.save(page);
            if (code < 300) {
                saveLemm(response, path);
            }
        }
        catch (Exception e){
            e.printStackTrace();
            System.out.println("Не удалось подгрузить данные");
            System.out.println(path);

        }
    }
private void saveLemm(Connection.Response responses , String path) throws IOException {
       HashMap <String,Integer> notCloneLemmInTitle = new HashMap<>();
       HashMap <String,Integer> notCloneLemmInBody = new HashMap<>();
       String textBody = String.valueOf(responses.parse().select("body").text());
textBody = textBody.replaceAll("[^а-яА-Яa-zA-Z ]"," ");
String textTitle = String.valueOf(responses.parse().select("title").text());
textTitle = textTitle.replaceAll("[^а-яА-Яa-zA-Z ]"," ");
List<String> body = List.of(textBody.split(" "));
List<String> title = List.of(textTitle.split(" "));
body.forEach(s -> {
    s = s.trim().toLowerCase();
    if (s.length()>2 && s.matches("[а-яА-Я]+")){
        try {
            ruLemm(s, notCloneLemmInBody);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    else if (s.length()>2 && s.matches("[a-zA-Z]+")){
        try {
            enLemm(s, notCloneLemmInBody);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
});
title.forEach(s -> {
    s = s.trim().toLowerCase();
    if (s.length()>2 && s.matches("[а-яА-Я]+")){
        try {
            ruLemm(s, notCloneLemmInTitle);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    else if (s.length()>2 && s.matches("[a-zA-Z]+")){
        try {
            enLemm(s, notCloneLemmInTitle);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
});
saveSites(path);
saveIndex(notCloneLemmInTitle,notCloneLemmInBody,path);
}
private void enLemm(String text, HashMap<String,Integer> notClone) throws IOException {
    LuceneMorphology luceneMorphology = new EnglishLuceneMorphology();
    List<String> morphInfo = luceneMorphology.getMorphInfo(text);
    if (morphInfo.size()==1 && (!morphInfo.get(0).contains("ARTICLE") && !morphInfo.get(0).contains("PREP")) ){
        List<String> normalForms = luceneMorphology.getNormalForms(text);
        if (!notClone.containsKey(normalForms.get(0)) && lemmaRepository.findFirstByLemmaLike(normalForms.get(0)) != null) {
            lemmaRepository.setNewFrequency(normalForms.get(0));
            notClone.put(normalForms.get(0), 1);
        }
        else if (!notClone.containsKey(normalForms.get(0))){
            Lemma lemma = new Lemma();
            lemma.setLemma(normalForms.get(0));
            lemma.setFrequency(1);
            lemmaRepository.save(lemma);
            notClone.put(normalForms.get(0),1);
        }
        else notClone.put(normalForms.get(0),notClone.get(normalForms.get(0))+1);
    }
}
private void ruLemm(String text, HashMap<String,Integer> notClone) throws IOException {
    LuceneMorphology luceneMorphology = new RussianLuceneMorphology();
    List<String> morphInfo = luceneMorphology.getMorphInfo(text);
    if (morphInfo.size()==1 && (!morphInfo.get(0).contains("ПРЕДЛ") && !morphInfo.get(0).contains("СОЮЗ") && !morphInfo.get(0).contains("МЕЖД")) ){
        List<String> normalForms = luceneMorphology.getNormalForms(text);
        if (!notClone.containsKey(normalForms.get(0)) && lemmaRepository.findFirstByLemmaLike(normalForms.get(0)) != null) {
            lemmaRepository.setNewFrequency(normalForms.get(0));
            notClone.put(normalForms.get(0), 1);
        }
        else if (!notClone.containsKey(normalForms.get(0))){
            Lemma lemma = new Lemma();
            lemma.setLemma(normalForms.get(0));
            lemma.setFrequency(1);
            lemmaRepository.save(lemma);
            notClone.put(normalForms.get(0),1);
        }
        else notClone.put(normalForms.get(0),notClone.get(normalForms.get(0))+1);
    }
}
private void saveIndex(HashMap<String,Integer> lemmTitle, HashMap<String,Integer> lemmBody, String path){
        lemmBody.forEach((s, integer) -> {
            Index index = new Index();
            float rank;
            if (lemmTitle.get(s) == null){
                rank = (float) (0.8*integer);
            }
            else{ rank = (float) (lemmTitle.get(s) + 0.8*integer);
            lemmTitle.remove(s);}
index.setRank(rank);
index.setLemmaId(lemmaRepository.findFirstByLemmaLike(s));
index.setPageId(pageRepository.findFirstByPathLike(path));
indexRepository.save(index);
        });
        if (!lemmTitle.isEmpty()){
            lemmTitle.forEach((s, integer) -> {
                Index index = new Index();
                index.setRank(1F);
                index.setLemmaId(lemmaRepository.findFirstByLemmaLike(s));
                index.setPageId(pageRepository.findFirstByPathLike(path));
                indexRepository.save(index);
            });
        }
}
private void saveSites(String path){
        if (siteRepository.findByName(path).size() > 0){
            updateSite(siteRepository.findByName(path));
        }
        else {
            Site site = new Site();
            Date date = new Date();
            String[] urls = urlProperties.getUrl();
            site.setName(path);
            for (int j = 0; j < urls.length; j++) {
                if (path.contains(urls[j])) {
                    site.setUrl(urls[j]);
                    if (site.getUrl().lastIndexOf("/") != site.getUrl().length()-1){
                        site.setUrl(urls[j] + "/");
                    }
                    break;
                }
            }
            site.setStatus(IndexType.INDEXING);
            site.setStatusTime(date);
            siteRepository.save(site);
        }
}
private void updateSite(List<Site> sites){
       Site site = sites.get(0);
    Date date = new Date();
    site.setStatus(IndexType.INDEXING);
    site.setStatusTime(date);
    siteRepository.save(site);
}
}
