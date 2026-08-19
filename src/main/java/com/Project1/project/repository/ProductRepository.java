package com.Project1.project.repository;

import com.Project1.project.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.data.jpa.repository.Query("update Product p set p.stockQuantity = p.stockQuantity - :qty where p.id = :id and p.stockQuantity >= :qty")
    int decreaseStockIfAvailable(@org.springframework.data.repository.query.Param("id") Long id, @org.springframework.data.repository.query.Param("qty") int qty);

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.data.jpa.repository.Query("update Product p set p.stockQuantity = p.stockQuantity + :qty where p.id = :id")
    void restoreStock(@org.springframework.data.repository.query.Param("id") Long id, @org.springframework.data.repository.query.Param("qty") int qty);

}
