package com.example.demo.repository;

import com.example.demo.entities.Mounting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MountingRepository extends JpaRepository<Mounting, Long> {
}
