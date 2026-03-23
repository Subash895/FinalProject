package com.smartCity.Web.Controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.smartCity.Web.Model.CityHistory;
import com.smartCity.Web.Service.CityHistoryService;

@RestController
@RequestMapping("/api/cityhistory")
@CrossOrigin("*")
public class CityHistoryController {

    @Autowired
    private CityHistoryService service;

    @PostMapping
    public CityHistory createCityHistory(@RequestBody CityHistory history) {
        return service.createCityHistory(history);
    }

    @GetMapping
    public List<CityHistory> getAllCityHistory() {
        return service.getAllCityHistory();
    }
}