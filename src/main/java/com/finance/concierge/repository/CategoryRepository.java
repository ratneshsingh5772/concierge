package com.finance.concierge.repository;

import com.finance.concierge.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Category entity
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    /**
     * Find category by name (exact match)
     */
    Optional<Category> findByName(String name);

    /**
     * Find category by name (case-insensitive)
     */
    @Query("SELECT c FROM Category c WHERE LOWER(c.name) = LOWER(:name)")
    Optional<Category> findByNameIgnoreCase(String name);

    /**
     * Find all active categories
     */
    List<Category> findByIsActiveTrue();

    /**
     * Check if category exists by name (case-insensitive)
     */
    @Query("SELECT COUNT(c) > 0 FROM Category c WHERE LOWER(c.name) = LOWER(:name)")
    boolean existsByNameIgnoreCase(String name);
}

