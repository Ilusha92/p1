package com.example.demo.repository;

import com.example.demo.entities.InputHeader;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InputHeaderRepository extends JpaRepository<InputHeader, Long> {

}