package com.example.page.pageModel;

import com.example.controllers.site.Site;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PageRepository extends JpaRepository<Page,Integer> {
    Page findFirstByPathLike(String path);
    Page findBySite(Site site);

}
