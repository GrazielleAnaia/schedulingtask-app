package com.grazielleanaia.registration_api.infrastructure.repository;


import com.grazielleanaia.registration_api.infrastructure.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {

    //Soft deleted
    @Query("""
            select a 
            from Address a
            join a.customer c
            where a.id = :addressId
            and c.email = :email
            and c.deleted = false
            """)
    Optional<Address> findActiveAddressByIdAndCustomerEmail(@Param ("addressId") Long addressId,
                                                            @Param("email") String email);

    Optional<Address> findByIdAndCustomerEmail(Long addressId, String email);
}
