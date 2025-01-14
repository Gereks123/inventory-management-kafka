package com.energia.inventory_management.repository;

import com.energia.inventory_management.repository.entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemRepository extends JpaRepository<Item, Long> {
}
