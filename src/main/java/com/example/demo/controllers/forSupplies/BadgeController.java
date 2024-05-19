package com.example.demo.controllers.forSupplies;

import com.example.demo.entities.forSupplies.Badge;
import com.example.demo.services.forSupplies.BadgeServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
public class BadgeController {

    @Autowired
    private BadgeServiceImpl badgeServiceImpl;

    @GetMapping("/badges")
    @ResponseBody
    public List<Badge> getAllBadges() {
        return badgeServiceImpl.getAllBadges();
    }

}
