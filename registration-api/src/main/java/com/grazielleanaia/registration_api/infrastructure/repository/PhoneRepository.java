package com.grazielleanaia.registration_api.infrastructure.repository;


import com.grazielleanaia.registration_api.infrastructure.entity.Phone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PhoneRepository extends JpaRepository<Phone, Long> {

    Optional<Phone> findByIdAndCustomerId(Long phoneId, Long customerId);
    Optional<Phone> findByIdAndCustomerEmail(Long phoneId, String email);
}
