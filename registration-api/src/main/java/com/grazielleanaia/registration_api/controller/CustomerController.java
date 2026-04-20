package com.grazielleanaia.registration_api.controller;


import com.grazielleanaia.registration_api.business.CustomerService;
import com.grazielleanaia.registration_api.business.dto.*;
import com.grazielleanaia.registration_api.constants.AppConstant;
import com.grazielleanaia.registration_api.infrastructure.entity.CustomerStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RefreshScope
@Validated
@RestController
@RequestMapping("/api/v1/customers")

public class CustomerController {

    private final CustomerService customerService;

    private static final Logger logger = LoggerFactory.getLogger(CustomerController.class);

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @PostMapping
    public ResponseEntity<CustomerResponseDTO> createCustomer(@Valid @RequestBody CustomerRequestDTO customerRequestDTO) {
        CustomerResponseDTO dto = customerService.createCustomer(customerRequestDTO);
        return new ResponseEntity<>(dto, HttpStatus.CREATED);
    }

    @GetMapping(params = "email")
    public ResponseEntity<CustomerResponseDTO> findCustomerByEmail(@RequestParam("email") @Email String email) {
        logger.info("Getting customer by email: {}", email);
        return ResponseEntity.ok(customerService.getCustomerByEmail(email));
    }

    @GetMapping("/{customerId}")
    public ResponseEntity<CustomerResponseDTO> findCustomerById(@PathVariable("customerId") Long customerId) {
        logger.info("Getting customer by id: {}", customerId);
        CustomerResponseDTO dto = customerService.getCustomerById(customerId);
        return new ResponseEntity<>(dto, HttpStatus.OK);
    }

    @GetMapping(params = "!email")
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
    public ResponseEntity<Void> deleteMyAccount(@RequestHeader("X-User-Email")
                                                @Email(message = "Invalid email format") String email) {
        customerService.deleteCustomerByEmail(email);
        return ResponseEntity.noContent().build();
    }

    //Delete Customer by Admin
    //Ok
    @DeleteMapping("/{customerId}")
    public ResponseEntity<Void> deleteCustomerByAdmin(@PathVariable Long customerId) {
        customerService.deleteCustomerById(customerId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{customerId}")
    public ResponseEntity<CustomerResponseDTO> updateCustomer(@Valid @RequestBody CustomerRequestDTO customerRequestDTO,
                                                              @PathVariable Long customerId) {
        return ResponseEntity.ok(customerService.updateCustomer(customerRequestDTO, customerId));
    }

    @PutMapping("/{customerId}/addresses/{addressId}")
    public ResponseEntity<AddressResponseDTO> updateCustomerAddress(@RequestBody AddressRequestDTO addressDTO,
                                                                    @PathVariable Long customerId,
                                                                    @PathVariable Long addressId) {
        return ResponseEntity.ok(customerService.updateAddress(addressDTO, customerId, addressId));
    }

    @PutMapping("/{customerId}/phones/{phoneId}")
    public ResponseEntity<PhoneResponseDTO> updateCustomerPhone(@RequestBody PhoneRequestDTO phoneDTO,
                                                                @PathVariable Long customerId,
                                                                @PathVariable Long phoneId) {
        PhoneResponseDTO phoneDTO1 = customerService.updatePhone(phoneDTO, customerId, phoneId);
        return new ResponseEntity<>(phoneDTO1, HttpStatus.OK);
    }

    @PostMapping("/{customerId}/addresses")
    public ResponseEntity<AddressResponseDTO> addCustomerAddress(@RequestBody AddressRequestDTO addressDTO,
                                                                 @PathVariable Long customerId) {
        AddressResponseDTO addressDTO1 = customerService.addCustomerAddress(addressDTO, customerId);
        return new ResponseEntity<>(addressDTO1, HttpStatus.CREATED);
    }

    @PostMapping("/{customerId}/phones")
    public ResponseEntity<PhoneResponseDTO> addCustomerPhone(@RequestBody PhoneRequestDTO phoneDTO,
                                                             @PathVariable Long customerId) {
        PhoneResponseDTO phoneDTO1 = customerService.addPhone(phoneDTO, customerId);
        return new ResponseEntity<>(phoneDTO1, HttpStatus.CREATED);
    }

    @PatchMapping("/{customerId}/status")
    public ResponseEntity<Void> updateCustomerStatus(@PathVariable Long customerId,
                                                     @RequestParam CustomerStatus status) {
        customerService.updateCustomerStatus(customerId, status);
        return ResponseEntity.ok().build();
    }

    //Load balancers and Kubernetes require health checks
    //Not tested
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("OK");
    }

}
