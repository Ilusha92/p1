package com.example.demo.repository.forSupplies;

import com.example.demo.entities.forSupplies.Asup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AsupRepository extends JpaRepository<Asup, Long> {

}
