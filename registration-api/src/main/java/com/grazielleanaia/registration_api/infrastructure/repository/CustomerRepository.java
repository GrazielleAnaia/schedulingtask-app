package com.grazielleanaia.registration_api.infrastructure.repository;


import com.grazielleanaia.registration_api.infrastructure.entity.Customer;
import com.grazielleanaia.registration_api.infrastructure.entity.CustomerStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {


    @Transactional
    void deleteByEmail(String email);

    @Transactional
    Optional<Customer> findByEmailAndDeletedFalse(String email);

    Page<Customer> findAllByDeletedFalse(Pageable pageable);

    boolean existsByEmailAndDeletedFalse(String email);

    Optional<Customer> findByIdAndDeletedFalse(Long id);

    //Concurrency Safety
    @Modifying
    @Query("""
            UPDATE Customer c SET c.status = :status WHERE c.id = :customerId""")
    int updateCustomerStatus(@Param("customerId") Long customerId, @Param("status") CustomerStatus status);
}
