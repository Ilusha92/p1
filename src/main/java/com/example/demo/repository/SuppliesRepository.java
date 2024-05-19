package com.example.demo.repository;

import com.example.demo.entities.Supplies;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SuppliesRepository extends JpaRepository<Supplies, Long> {
}
