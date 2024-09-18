package com.example.demo.repository;


import com.example.demo.entities.InputBody;
import com.example.demo.entities.InputHeader;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InputBodyRepository extends JpaRepository<InputBody, Long> {

}
