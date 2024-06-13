package com.example.demo.repository.forSupplies;

import com.example.demo.entities.forSupplies.Sticker;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StickerRepository extends JpaRepository<Sticker, Long> {
    List<Sticker> findByIdIn(List<Long> ids);
}