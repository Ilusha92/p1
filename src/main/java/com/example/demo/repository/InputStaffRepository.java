package com.example.demo.repository;

import com.example.demo.entities.InputStaff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InputStaffRepository extends JpaRepository<InputStaff, Long> {
}
