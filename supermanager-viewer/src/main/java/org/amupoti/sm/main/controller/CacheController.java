package org.amupoti.sm.main.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class CacheController {

    @Autowired
    private CacheManager cacheManager;

    // clear all cache using cache manager
    @RequestMapping(value = "/cache/clear.html")
    public String clearCache(Model model) {
        for (String name : cacheManager.getCacheNames()) {
            cacheManager.getCache(name).clear();
        }
        String message = "Cache cleared successfully";
        model.addAttribute("message", message);
        log.info(message);
        return "cache";
    }
}