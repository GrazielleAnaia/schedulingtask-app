package com.grazielleanaia.registration_api.business.mapper;


import com.grazielleanaia.registration_api.business.dto.*;
import com.grazielleanaia.registration_api.infrastructure.entity.Address;
import com.grazielleanaia.registration_api.infrastructure.entity.Customer;
import com.grazielleanaia.registration_api.infrastructure.entity.Phone;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CustomerMapperConverter {

    @Mapping(source = "addressDTOList", target = "addressList")
    @Mapping(source = "phoneList", target = "phoneList")
    Customer toCustomer(CustomerRequestDTO customerRequestDTO);

    @Mapping(source = "addressList", target = "addressDTOList")
    @Mapping(source = "phoneList", target = "phoneList")
    CustomerResponseDTO toCustomerDTO(Customer customer);

    AddressResponseDTO toAddressDTO(Address address);

    Address addAddress(AddressRequestDTO addressDTO);

    Phone addPhone(PhoneRequestDTO phoneDTO);

    PhoneResponseDTO toPhoneDTO(Phone phone);

    List<AddressResponseDTO> toAddressDTOList(List<Address> addressList);

}
