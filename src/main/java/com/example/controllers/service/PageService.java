package com.example.controllers.service;

import com.example.page.pageModel.Page;
import com.example.page.pageModel.PageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PageService {
    private final PageRepository pageRepository;
@Autowired
    public PageService(PageRepository pageRepository) {
        this.pageRepository = pageRepository;
    }
    public List<Page> getPathPage(){
        List<Page> pages = pageRepository.findAll();
        return pages;
    }
}
