package com.example.PolymarketTracker.repository;

import com.example.PolymarketTracker.model.PriceChange;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PriceChangeRepository extends JpaRepository<PriceChange, Long> {
}