package com.example.demo.repository.forSupplies;

import com.example.demo.entities.forSupplies.Badge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BadgeRepository extends JpaRepository<Badge, Long> {

}
