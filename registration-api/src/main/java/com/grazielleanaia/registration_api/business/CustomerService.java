package com.grazielleanaia.registration_api.business;


import com.grazielleanaia.registration_api.business.dto.*;
import com.grazielleanaia.registration_api.business.mapper.CustomerMapperConverter;
import com.grazielleanaia.registration_api.business.mapper.CustomerUpdateMapperConverter;
import com.grazielleanaia.registration_api.infrastructure.entity.Address;
import com.grazielleanaia.registration_api.infrastructure.entity.Customer;
import com.grazielleanaia.registration_api.infrastructure.entity.CustomerStatus;
import com.grazielleanaia.registration_api.infrastructure.entity.Phone;
import com.grazielleanaia.registration_api.infrastructure.exception.ConflictException;
import com.grazielleanaia.registration_api.infrastructure.exception.ResourceNotFoundException;
import com.grazielleanaia.registration_api.infrastructure.repository.AddressRepository;
import com.grazielleanaia.registration_api.infrastructure.repository.CustomerRepository;
import com.grazielleanaia.registration_api.infrastructure.repository.PhoneRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service

public class CustomerService {

    private final CustomerRepository customerRepository;
    private final AddressRepository addressRepository;
    private final PhoneRepository phoneRepository;
    private final CustomerMapperConverter customerMapper;
    private final CustomerUpdateMapperConverter customerUpdateMapper;

    private static final Logger logger = LoggerFactory.getLogger(CustomerService.class);

    public CustomerService(CustomerRepository customerRepository, AddressRepository addressRepository,
                           PhoneRepository phoneRepository, CustomerMapperConverter customerMapperConverter,
                           CustomerUpdateMapperConverter customerUpdateMapper) {
        this.customerRepository = customerRepository;
        this.addressRepository = addressRepository;
        this.phoneRepository = phoneRepository;
        this.customerMapper = customerMapperConverter;
        this.customerUpdateMapper = customerUpdateMapper;
    }

    @Transactional
    public CustomerResponseDTO createCustomer(CustomerRequestDTO customerRequestDTO) {

        String email = customerRequestDTO.getEmail().trim().toLowerCase();
        if (customerRepository.existsByEmailAndDeletedFalse(email)) {
            throw new ConflictException("Email already registered: " + email);
        }
        customerRequestDTO.setEmail(email);
        try {
            Customer customer = customerMapper.toCustomer(customerRequestDTO);
            customer.setStatus(CustomerStatus.ACTIVE);

            if (customer.getPhoneList() != null) {
                customer.getPhoneList().forEach(phone -> phone.setCustomer(customer));
            }

            if (customer.getAddressList() != null) {
                customer.getAddressList().forEach(address -> address.setCustomer(customer));
            }

            return customerMapper.toCustomerDTO(customerRepository.save(customer));
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException("Email already in use: " + customerRequestDTO.getEmail());
        }
    }

    //Soft delete
    @Transactional
    public void deleteCustomerByEmail(String email) {
        Customer customer = customerRepository.findByEmailAndDeletedFalse(email)
                .orElseThrow(() -> new ResourceNotFoundException("Email not found: " + email));
        customer.setDeleted(true);
        customer.setDeletedBy("CUSTOMER");
        customer.setStatus(CustomerStatus.DELETED);
        customer.setDeletedAt(LocalDateTime.now());
        customerRepository.save(customer);
    }

    public CustomerResponseDTO getCustomerByEmail(String email) {
        logger.debug("Getting customer by email: {}", email);
        return customerRepository.findByEmailAndDeletedFalse(email)
                .map(customerMapper::toCustomerDTO)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Customer email not found"));
    }

    @Transactional
    public CustomerResponseDTO updateCustomer(CustomerRequestDTO customerRequestDTO, Long customerId) {
        Customer customer = customerRepository.findByIdAndDeletedFalse(customerId).orElseThrow(() ->
                new ResourceNotFoundException("Customer id not found"));
        Customer customer1 = customerUpdateMapper.updateCustomer(customer, customerRequestDTO);
        return customerMapper.toCustomerDTO(customer1);
    }

    @Transactional
    public AddressResponseDTO updateAddress(AddressRequestDTO addressDTO, Long addressId, Long customerId) {
        Address address = addressRepository.findByIdAndCustomerId(customerId, addressId).orElseThrow(() ->
                new ResourceNotFoundException("Address id not found " + addressId));
        customerUpdateMapper.updateAddress(address, addressDTO);
        return customerMapper.toAddressDTO(address);
    }

    @Transactional
    public PhoneResponseDTO updatePhone(PhoneRequestDTO phoneDTO, Long customerId, Long phoneId) {
        Phone phone = phoneRepository.findByIdAndCustomerId(phoneId, customerId).orElseThrow(() ->
                new ResourceNotFoundException("Phone id not found " + phoneId));
        Phone phone1 = customerUpdateMapper.updatePhone(phone, phoneDTO);
        return customerMapper.toPhoneDTO(phone1);
    }

    @Transactional
    public AddressResponseDTO addCustomerAddress(AddressRequestDTO addressDTO, Long customerId) {
        Customer customer = customerRepository.findByIdAndDeletedFalse(customerId).orElseThrow(() ->
                new ResourceNotFoundException("Customer not found "));
        Address address = customerMapper.addAddress(addressDTO);
        address.setCustomer(customer);
        customer.getAddressList().add(address);
        return customerMapper.toAddressDTO(addressRepository.save(address));
    }

    @Transactional
    public PhoneResponseDTO addPhone(PhoneRequestDTO phoneDTO, Long customerId) {
        Customer customer = customerRepository.findByIdAndDeletedFalse(customerId).orElseThrow(() ->
                new ResourceNotFoundException("Customer not found "));
        Phone phone = customerMapper.addPhone(phoneDTO);
        phone.setCustomer(customer);
        customer.getPhoneList().add(phone);
        return customerMapper.toPhoneDTO(phoneRepository.save(phone));
    }

    public PageResponse getAllCustomers(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {
        Sort.Direction sortDirection = sortOrder.equalsIgnoreCase("des") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Sort sort = Sort.by(sortDirection, sortBy);

        Pageable pageDetails = PageRequest.of(pageNumber, pageSize, sort);
        Page<Customer> customerPage = customerRepository.findAll(pageDetails);

        List<Customer> customerList = customerPage.getContent();

        List<CustomerResponseDTO> content = customerList.stream()
                .map(customerMapper::toCustomerDTO)
                .toList();
        PageResponse response = new PageResponse();
        response.setTotalElements(customerPage.getTotalElements());
        response.setTotalPages(customerPage.getTotalPages());
        response.setPageNumber(customerPage.getNumber());
        response.setPageSize(customerPage.getSize());
        response.setLastPage(customerPage.isLast());
        response.setContent(content);
        return response;
    }

    public void deleteCustomerById(Long id) {
        Customer customer = customerRepository
                .findByIdAndDeletedFalse(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Customer not found with id: " + id));
        customer.setDeleted(true);
        customer.setStatus(CustomerStatus.DELETED);
        customer.setDeletedBy("SYSTEM");
        customer.setDeletedAt(LocalDateTime.now());
        customerRepository.save(customer);
    }


    public CustomerResponseDTO getCurrentCustomer(String email) {
        Customer customer = customerRepository.findByEmailAndDeletedFalse(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Customer not found " + email));
        return customerMapper.toCustomerDTO(customer);
    }

    /* Efficient (avoid loading entities when possible)
     * Validated (only allow valid statuses)
     * Concurrent-safe
     * Minimal database load */
    @Transactional
    public void updateCustomerStatus(Long customerId, CustomerStatus status) {
        int updatedRows = customerRepository.updateCustomerStatus(customerId, status);
        if (updatedRows == 0) {
            throw new ResourceNotFoundException("Customer not found");
        }

        //publish a Kafka event to other MS
        //publishStatusEvent(id, event)
    }

    public CustomerResponseDTO getCustomerById(Long id) {
        Customer customer = customerRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
        return customerMapper.toCustomerDTO(customer);
    }

//    private void publishStatusEvent(Long id, CustomerStatus status) {
//        CustomerStatusEvent event = new CustomerStatusEvent(id, status);
//        kafkaTemplate.send("customer-status-topic", event);
//    }
}
