package com.example.demo.repository;

import com.example.demo.entities.AdditionalSale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdditionalSalesRepository extends JpaRepository<AdditionalSale, Long> {

}
