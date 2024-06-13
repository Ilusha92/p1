package com.example.demo.repository.forSupplies;

import com.example.demo.entities.forSupplies.Badge;
import com.example.demo.entities.forSupplies.Bracer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BracerRepository extends JpaRepository<Bracer, Long> {
    List<Bracer> findByIdIn(List<Long> ids);
}
