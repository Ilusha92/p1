package com.example.demo.repository;


import com.example.demo.entities.Logistic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LogisticRepository extends JpaRepository<Logistic, Long> {
}
