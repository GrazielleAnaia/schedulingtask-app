package com.grazielleanaia.registration_api.business.mapper;


import com.grazielleanaia.registration_api.business.dto.AddressRequestDTO;
import com.grazielleanaia.registration_api.business.dto.CustomerRequestDTO;
import com.grazielleanaia.registration_api.business.dto.PhoneRequestDTO;
import com.grazielleanaia.registration_api.infrastructure.entity.Address;
import com.grazielleanaia.registration_api.infrastructure.entity.Customer;
import com.grazielleanaia.registration_api.infrastructure.entity.Phone;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface CustomerUpdateMapperConverter {

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true) //do not touch this field
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(source = "addressDTOList", target = "addressList")
    Customer updateCustomer(@MappingTarget Customer customer, CustomerRequestDTO customerRequestDTO);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true) //do not overwrite the PK
    @Mapping(target = "customer", ignore = true)
    Phone updatePhone(@MappingTarget Phone phone, PhoneRequestDTO phoneDTO);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "customer", ignore = true)
    Address updateAddress(@MappingTarget Address address, AddressRequestDTO addressDTO);

}
