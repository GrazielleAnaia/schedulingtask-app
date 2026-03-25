package com.grazielleanaia.registration_api.controller;


import com.grazielleanaia.registration_api.business.CustomerService;
import com.grazielleanaia.registration_api.business.dto.*;
import com.grazielleanaia.registration_api.constants.AppConstant;
import com.grazielleanaia.registration_api.infrastructure.entity.CustomerStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customers")

public class CustomerController {

    private final CustomerService customerService;

    private static final Logger logger = LoggerFactory.getLogger(CustomerController.class);

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    //Ok
    @PostMapping
    public ResponseEntity<CustomerResponseDTO> createCustomer(@Valid @RequestBody CustomerRequestDTO customerRequestDTO) {
        CustomerResponseDTO dto = customerService.createCustomer(customerRequestDTO);
        return new ResponseEntity<>(dto, HttpStatus.CREATED);
    }

    //Ok
    @GetMapping("/by-email")
    public ResponseEntity<CustomerResponseDTO> findCustomerByEmail(@RequestParam("email") String email) {
        logger.info("Getting customer by email: {}", email);
        return ResponseEntity.ok(customerService.getCustomerByEmail(email));
    }

    //Ok
    @GetMapping("/{id}")
    public ResponseEntity<CustomerResponseDTO> findCustomerById(@PathVariable("id") Long id) {
        logger.info("Getting customer by id: {}", id);
        CustomerResponseDTO dto = customerService.getCustomerById(id);
        return new ResponseEntity<>(dto, HttpStatus.OK);
    }

    //Ok
    @GetMapping
    public ResponseEntity<PageResponse> findAllCustomers(
            @RequestParam(name = "pageNumber", defaultValue = AppConstant.PAGE_NUMBER, required = false) Integer pageNumber,
            @RequestParam(name = "pageSize", defaultValue = AppConstant.PAGE_SIZE, required = false) Integer pageSize,
            @RequestParam(name = "sortBy", defaultValue = AppConstant.SORT_BY, required = false) String sortBy,
            @RequestParam(name = "sortOrder", defaultValue = AppConstant.SORT_DIR, required = false) String sortOrder) {
        PageResponse customerPageResponse = customerService.getAllCustomers(pageNumber, pageSize, sortBy, sortOrder);
        return new ResponseEntity<>(customerPageResponse, HttpStatus.OK);
    }

    //If authentication happens at API gateway, the gateway may inject headers
    @GetMapping("/me")
    public ResponseEntity<CustomerResponseDTO> getMyProfile(@RequestHeader("X-User-Email") String email) {
        return ResponseEntity.ok(customerService.getCurrentCustomer(email));
    }


    //Customer self-deletion -- system should determine the user from authentication (JWT or gateway)
    //Ok
    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteMyAccount(@Email(message = "Invalid email format")
                                                @RequestParam("email") String email) {
        customerService.deleteCustomerByEmail(email);
        return ResponseEntity.noContent().build();
    }

    //Delete Customer by Admin
    //Ok
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCustomerByAdmin(@PathVariable Long id) {
        customerService.deleteCustomerById(id);
        return ResponseEntity.noContent().build();
    }

    //Ok
    @PutMapping("/{customerId}")
    public ResponseEntity<CustomerResponseDTO> updateCustomer(@Valid @RequestBody CustomerRequestDTO customerRequestDTO,
                                                              @PathVariable Long customerId) {
        return ResponseEntity.ok(customerService.updateCustomer(customerRequestDTO, customerId));
    }

    //Ok
    @PutMapping("/{customerId}/addresses/{addressId}")
    public ResponseEntity<AddressResponseDTO> updateCustomerAddress(@RequestBody AddressRequestDTO addressDTO,
                                                                    @PathVariable Long customerId,
                                                                    @PathVariable Long addressId) {
        return ResponseEntity.ok(customerService.updateAddress(addressDTO, customerId, addressId));
    }

    //Ok
    @PutMapping("/{customerId}/phones/{phoneId}")
    public ResponseEntity<PhoneResponseDTO> updateCustomerPhone(@RequestBody PhoneRequestDTO phoneDTO,
                                                                @PathVariable Long customerId,
                                                                @PathVariable Long phoneId) {
        PhoneResponseDTO phoneDTO1 = customerService.updatePhone(phoneDTO, customerId, phoneId);
        return new ResponseEntity<>(phoneDTO1, HttpStatus.OK);
    }

    //Ok
    @PostMapping("/{customerId}/addresses")
    public ResponseEntity<AddressResponseDTO> addCustomerAddress(@RequestBody AddressRequestDTO addressDTO,
                                                                 @PathVariable Long customerId) {
        AddressResponseDTO addressDTO1 = customerService.addCustomerAddress(addressDTO, customerId);
        return new ResponseEntity<>(addressDTO1, HttpStatus.CREATED);
    }

    //Ok
    @PostMapping("/{customerId}/phones")
    public ResponseEntity<PhoneResponseDTO> addCustomerPhone(@RequestBody PhoneRequestDTO phoneDTO,
                                                             @PathVariable Long customerId) {
        PhoneResponseDTO phoneDTO1 = customerService.addPhone(phoneDTO, customerId);
        return new ResponseEntity<>(phoneDTO1, HttpStatus.CREATED);
    }

    //Ok
    @PatchMapping("/{customerId}/status")
    public ResponseEntity<Void> updateCustomerStatus(@PathVariable Long customerId,
                                                     @RequestParam CustomerStatus status) {
        customerService.updateCustomerStatus(customerId, status);
        return ResponseEntity.ok().build();
    }

    //Load balancers and Kubernetes require health checks
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("OK");
    }

}
