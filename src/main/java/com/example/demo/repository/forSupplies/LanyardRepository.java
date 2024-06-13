package com.example.demo.repository.forSupplies;

import com.example.demo.entities.forSupplies.Lanyard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LanyardRepository extends JpaRepository<Lanyard, Long> {

}