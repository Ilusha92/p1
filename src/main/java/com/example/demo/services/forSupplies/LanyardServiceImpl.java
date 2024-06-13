package com.example.demo.services.forSupplies;

import com.example.demo.repository.forSupplies.LanyardRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LanyardServiceImpl {

    @Autowired
    private LanyardRepository lanyardRepository;
}
