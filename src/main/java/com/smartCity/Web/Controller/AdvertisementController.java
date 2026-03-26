package com.smartCity.Web.Controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.smartCity.Web.Model.Advertisement;
import com.smartCity.Web.Service.AdvertisementService;

@RestController
@RequestMapping("/api/advertisements")
@CrossOrigin("*")
public class AdvertisementController {

    @Autowired
    private AdvertisementService service;

    @PostMapping
    public Advertisement createAdvertisement(@RequestBody Advertisement ad) {
        return service.createAdvertisement(ad);
    }

    @GetMapping
    public List<Advertisement> getAllAdvertisements() {
        return service.getAllAdvertisements();
    }
}