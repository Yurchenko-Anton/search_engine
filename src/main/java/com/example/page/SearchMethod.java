package com.example.page;

import com.example.page.fieldModel.FieldRepository;
import com.example.page.indexModel.Index;
import com.example.page.indexModel.IndexRepository;
import com.example.page.lemmaModel.LemmaRepository;
import com.example.page.pageModel.Page;
import com.example.page.pageModel.PageRepository;
import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.english.EnglishLuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

@Controller
public class SearchMethod{
    UrlProperties urlProperties;
    PageRepository pageRepository;
    IndexRepository indexRepository;
    LemmaRepository lemmaRepository;
    FieldRepository fieldRepository;
    String oldWord;
@Autowired
    public SearchMethod(UrlProperties urlProperties,PageRepository pageRepository, IndexRepository indexRepository, LemmaRepository lemmaRepository, FieldRepository fieldRepository) {
       this.urlProperties = urlProperties;
        this.pageRepository = pageRepository;
        this.indexRepository = indexRepository;
        this.lemmaRepository = lemmaRepository;
        this.fieldRepository = fieldRepository;
    }

    public JSONArray run(String query, String site) {
        oldWord = query;
        Set<String> wordLemm = getWordLemmSet(query);
        List<String> sortedLemm = sortedLemm(wordLemm);
        List<String> paths = search(sortedLemm);
        Logger.getLogger(SearchMethod.class.getName()).info("123");
        return sortedPath(paths,sortedLemm,query, site);
    }

private Set<String> getWordLemmSet(String s){
    s = s.replaceAll("[^а-яА-Яa-zA-Z ]"," ");
    List<String> words = List.of(s.split(" "));
    Set<String> wordLemm = new HashSet<>();
    words.forEach(word ->{
        word = word.trim().toLowerCase();
        if (word.length()>2 && word.matches("[а-яА-Яa-zA-Z]+")){
            try {
                getWordLemm(word,wordLemm);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }});
    return wordLemm;
}
    private void getWordLemm(String word, Set<String> wordLemm) throws IOException {
if (word.matches("[а-яА-Я]+")){
    if (!getRuLemm(word).matches(""))
wordLemm.add(getRuLemm(word));
}
else if (word.matches("[a-zA-Z]+")){
    if (!getEnLemm(word).matches(""))
wordLemm.add(getEnLemm(word));
}
    }
    private String getEnLemm(String word) throws IOException {
        LuceneMorphology luceneMorphology = new EnglishLuceneMorphology();
        List<String> morphInfo = luceneMorphology.getMorphInfo(word);
        List<String> normalForms = new ArrayList<>();
        if (morphInfo.size()==1 && (!morphInfo.get(0).contains("ARTICLE") && !morphInfo.get(0).contains("PREP")) ) {
             normalForms = luceneMorphology.getNormalForms(word);
        }
        if (normalForms.size()<1){
            return "";
        }
        else {
            return normalForms.get(0);
        }
    }
    private String getRuLemm(String word) throws IOException {
        LuceneMorphology luceneMorphology = new RussianLuceneMorphology();
        List<String> morphInfo = luceneMorphology.getMorphInfo(word);
        List<String> normalForms = new ArrayList<>();
        if (morphInfo.size()==1 && (!morphInfo.get(0).contains("ПРЕДЛ") && !morphInfo.get(0).contains("СОЮЗ") && !morphInfo.get(0).contains("МЕЖД")) ) {
            normalForms = luceneMorphology.getNormalForms(word);
        }
        if (normalForms.size()<1){
            return "";
        }
        else {
            return normalForms.get(0);
        }
    }
    private List<String> search(List<String> words){
        Set<Index> indexes = new HashSet<>();
        List<String> paths = new ArrayList<>();
for (int i = 0 ; i < words.size(); i++){
    if (i==0){
        indexes.addAll(indexRepository.findByLemmaId(lemmaRepository.findFirstByLemmaLike(words.get(i))));
        indexes.forEach(index -> {
            paths.add(index.getPageId().getPath());
        });
    }else {
        indexes.clear();
    indexes.addAll(indexRepository.findByLemmaId(lemmaRepository.findFirstByLemmaLike(words.get(i))));
        List<String> path = new ArrayList<>();
        indexes.forEach(index -> {
            path.add(index.getPageId().getPath());
        });
    for (int j = 0; j < paths.size(); j++) {
int count = 0;
        for (int p = 0; p < path.size(); p++) {
if (paths.get(j).matches(path.get(p))){
    count=1;
}
        }
        if (count==0){
            paths.remove(paths.get(j));
        }
    }
        }
    }
return paths;
}
    private List<String> sortedLemm (Set<String> wordLemm){
        Map<String, Integer> rankLemm = new HashMap<>();
        List<String> sortedLemm = new ArrayList<>();
        wordLemm.forEach(s -> {
            if (lemmaRepository.findFirstByLemmaLike(s) != null){
            rankLemm.put(s,lemmaRepository.findFirstByLemmaLike(s).getFrequency());}
        });
        rankLemm.entrySet().stream().sorted(Map.Entry.<String, Integer>comparingByValue()).forEach(stringIntegerEntry -> {
            sortedLemm.add(stringIntegerEntry.getKey());
        });
        return sortedLemm;
    }
    private JSONArray sortedPath (List<String> paths, List<String> sortedLemm, String serachText, String site){
    JSONArray data = new JSONArray();
    HashMap<String,Float> sortedPage = new HashMap<>();
    float maxRank =0;
        List<Index> pages = new ArrayList<>();
    for (int i=0; i<paths.size(); i++){
        pages.clear();
        float ranks = 0;
        for (int j=0; j<sortedLemm.size(); j++) {
            if (indexRepository.findByPageIdAndLemmaId(pageRepository.findFirstByPathLike(paths.get(i)),
                    lemmaRepository.findFirstByLemmaLike(sortedLemm.get(j))) != null){
            ranks += indexRepository.findByPageIdAndLemmaId(pageRepository.findFirstByPathLike(paths.get(i)),
                    lemmaRepository.findFirstByLemmaLike(sortedLemm.get(j))).getRank();
        }}
        if (ranks > maxRank){
            maxRank = ranks;
        }
        sortedPage.put(paths.get(i),ranks);
    }

for (int i=0; i<sortedPage.size(); i++){
    sortedPage.put(paths.get(i), sortedPage.get(paths.get(i))/maxRank);
}
        sortedPage.entrySet().stream().sorted(Map.Entry.<String, Float>comparingByValue()).forEach(stringIntegerEntry -> {
            Page page = pageRepository.findFirstByPathLike(stringIntegerEntry.getKey());
            String pageContent = page.getContent();
            try {
                if (site == null) {
                            data.add(saveJsonIfNonHaveSite(stringIntegerEntry.getKey(),stringIntegerEntry.getValue(),pageContent,page));
                }
                else {
                    data.add(saveJsonIfHaveSite(stringIntegerEntry.getKey(),stringIntegerEntry.getValue(),pageContent,page,site));
                }

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
return data;
}
private String getPageTitle(String pageContent){
    String pageTitle = pageContent.substring(pageContent.indexOf("<title>") + "<title>".length(), pageContent.indexOf("</title>"));
    return pageTitle;
}
private String getPageContent(Page page) throws IOException {
    List<String> words = List.of(oldWord.toLowerCase().split(" "));
    int maxIndex = 0;
    int minIndex = 0;
    Connection.Response response = Jsoup.connect(page.getPath()).userAgent(urlProperties.getUserAgent()).referrer("http://www.google.com").maxBodySize(0).execute();
    String pageBody = response.parse().select("body").text();
    for (int i=0; i<words.size();i++){
        int number =0;
        if (pageBody.toLowerCase().contains(words.get(i))){
            if (number == i) {
                maxIndex = pageBody.toLowerCase().indexOf(words.get(i)) + words.get(i).length();
                minIndex = pageBody.toLowerCase().indexOf(words.get(i));
            }
            else {
                if (minIndex > (pageBody.toLowerCase().indexOf(words.get(i)))){
                    minIndex = pageBody.toLowerCase().indexOf(words.get(i));
                }
                if (maxIndex < (pageBody.toLowerCase().indexOf(words.get(i)) + words.get(i).length())){
                    maxIndex = pageBody.toLowerCase().indexOf(words.get(i)) + words.get(i).length();
                }
            }
            }
        else number++;
    }
    if (words.size() == 1){
        pageBody = pageBody.substring(minIndex, minIndex+120);
    }
    else {
        pageBody = pageBody.substring(minIndex, maxIndex);
        if (pageBody.length() > 150) {
            pageBody = pageBody.substring(0, 150);
        }
    }
    String[] pageBodys = pageBody.split(" ");
    for (int i=0; i < pageBodys.length; i++){
        if (words.contains(pageBodys[i].toLowerCase())){
            pageBodys[i] = "<b>" + pageBodys[i] + "</b>";
        }
        if (i==0){
            pageBody = pageBodys[0];
        }
        else {
            pageBody = pageBody + " " + pageBodys[i];
        }
    }
    return pageBody+"...";
}
private JSONObject saveJsonIfNonHaveSite(String url,float ranks, String pageContent, Page page) throws IOException {
    JSONObject date = new JSONObject();
    for (int i = 0; i < urlProperties.getUrl().length; i++) {
        if (url.contains(urlProperties.getUrl()[i])) {
            date.put("site", urlProperties.getUrl()[i]);
            date.put("siteName", urlProperties.getName()[i]);
            date.put("uri", url.substring(urlProperties.getUrl()[i].length()));
            date.put("title", getPageTitle(pageContent));
            date.put("snippet", getPageContent(page));
            date.put("relevance", ranks);
            break;
        }
    }
    return date;
}
    private JSONObject saveJsonIfHaveSite(String url,float ranks, String pageContent, Page page, String site) throws IOException {
        JSONObject date = new JSONObject();
        if (url.contains(site)) {
            date.put("site", site);
            for (int i = 0; i < urlProperties.getUrl().length; i++) {
                if (urlProperties.getUrl()[i].contains(site)) {
                    date.put("siteName", urlProperties.getName()[i]);
                    break;
                }
            }
            date.put("uri", url.substring(site.length()));
            date.put("title", getPageTitle(pageContent));
            date.put("snippet", getPageContent(page));
            date.put("relevance", ranks);
        }
        return date;
    }
}
