package com.energia.inventory_management.repository;

import com.energia.inventory_management.repository.entity.SalesHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SalesHistoryRepository extends JpaRepository<SalesHistory, Long> {
    Page<SalesHistory> findByItemId(Long itemId, Pageable pageable);
}