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
@RequestMapping("/api/v1")

public class CustomerController {

    private final CustomerService customerService;

    private static final Logger logger = LoggerFactory.getLogger(CustomerController.class);

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    //purpose: public signup
    //gateway rule: permitAll
    @PostMapping("/customers")
    public ResponseEntity<CustomerResponseDTO> createCustomer(@Valid @RequestBody CustomerRequestDTO customerRequestDTO) {
        CustomerResponseDTO dto = customerService.createCustomer(customerRequestDTO);
        return new ResponseEntity<>(dto, HttpStatus.CREATED);
    }

    //purpose: current logged-in user profile
    //gateway rule: authenticated
    @GetMapping("/customers/me")
    public ResponseEntity<CustomerResponseDTO> getMyProfile(@RequestHeader("X-User-Email") String email) {
        return ResponseEntity.ok(customerService.getCurrentCustomer(email));
    }

    //purpose: current logged-in user deletes own account
    //gateway rule: authenticated
    @DeleteMapping("/customers/me")
    public ResponseEntity<Void> deleteMyAccount(@RequestHeader("X-User-Email")
                                                @Email(message = "Invalid email format") String email) {
        customerService.deleteCustomerByEmail(email);
        return ResponseEntity.noContent().build();
    }

    //purpose: current logged-in user updates own profile
    //gateway rule: authenticated
    @PutMapping("/customers/me")
    public ResponseEntity<CustomerResponseDTO> updateCustomer(@Valid @RequestBody CustomerRequestDTO customerRequestDTO,
                                                              @RequestHeader("X-User-Email") String email) {
        return ResponseEntity.ok(customerService.updateCustomer(customerRequestDTO, email));
    }

    //purpose: current logged-in user updates own address
    //gateway rule: authenticated
    @PutMapping("/customers/me/addresses/{addressId}")
    public ResponseEntity<AddressResponseDTO> updateCustomerAddress(@RequestBody AddressRequestDTO addressDTO,
                                                                    @RequestHeader("X-User-Email") String email,
                                                                    @PathVariable Long addressId) {
        return ResponseEntity.ok(customerService.updateAddress(addressDTO, addressId, email));
    }

    //purpose: current logged-in user updates own phone
    //gateway rule: authenticated
    @PutMapping("/customers/me/phones/{phoneId}")
    public ResponseEntity<PhoneResponseDTO> updateCustomerPhone(@RequestBody PhoneRequestDTO phoneDTO,
                                                                @RequestHeader("X-User-Email") String email,
                                                                @PathVariable Long phoneId) {
        PhoneResponseDTO phoneDTO1 = customerService.updatePhone(phoneDTO, email, phoneId);
        return new ResponseEntity<>(phoneDTO1, HttpStatus.OK);
    }

    //purpose: current logged-in user adds own address
    //gateway rule: authenticated
    @PostMapping("/customers/me/addresses")
    public ResponseEntity<AddressResponseDTO> addCustomerAddress(@RequestBody AddressRequestDTO addressDTO,
                                                                 @RequestHeader("X-User-Email") String email) {
        AddressResponseDTO addressDTO1 = customerService.addCustomerAddress(addressDTO, email);
        return new ResponseEntity<>(addressDTO1, HttpStatus.CREATED);
    }

    //purpose: current logged-in user adds own phones
    //gateway rule: authenticated
    @PostMapping("/customers/me/phones")
    public ResponseEntity<PhoneResponseDTO> addCustomerPhone(@RequestBody PhoneRequestDTO phoneDTO,
                                                             @RequestHeader("X-User-Email") String email) {
        PhoneResponseDTO phoneDTO1 = customerService.addPhone(phoneDTO, email);
        return new ResponseEntity<>(phoneDTO1, HttpStatus.CREATED);
    }

    //purpose: admin deleted by id
    //gateway rule: hasRole("ADMIN")
    @DeleteMapping("/admin/customers/{customerId}")
    public ResponseEntity<Void> deleteCustomerByAdmin(@PathVariable Long customerId) {
        customerService.deleteCustomerById(customerId);
        return ResponseEntity.noContent().build();
    }

    //purpose: admin lookup by email
    //gateway rule: hasRole("ADMIN")
    @GetMapping(value = "/admin/customers", params = "email")
    public ResponseEntity<CustomerResponseDTO> findCustomerByEmail(@RequestParam("email") @Email String email) {
        logger.info("Getting customer by email: {}", email);
        return ResponseEntity.ok(customerService.getCustomerByEmail(email));
    }

    //purpose: admin lookup by id
    //gateway rule: hasRole("ADMIN")
    @GetMapping("/admin/customers/{customerId}")
    public ResponseEntity<CustomerResponseDTO> findCustomerById(@PathVariable("customerId") Long customerId) {
        logger.info("Getting customer by id: {}", customerId);
        CustomerResponseDTO dto = customerService.getCustomerById(customerId);
        return new ResponseEntity<>(dto, HttpStatus.OK);
    }

    //purpose: admin list customers
    //gateway rule: hasRole("ADMIN")
    @GetMapping(value = "/admin/customers", params = "!email")
    public ResponseEntity<PageResponse> findAllCustomers(
            @RequestParam(name = "pageNumber", defaultValue = AppConstant.PAGE_NUMBER, required = false) Integer pageNumber,
            @RequestParam(name = "pageSize", defaultValue = AppConstant.PAGE_SIZE, required = false) Integer pageSize,
            @RequestParam(name = "sortBy", defaultValue = AppConstant.SORT_BY, required = false) String sortBy,
            @RequestParam(name = "sortOrder", defaultValue = AppConstant.SORT_DIR, required = false) String sortOrder) {
        PageResponse customerPageResponse = customerService.getAllCustomers(pageNumber, pageSize, sortBy, sortOrder);
        return new ResponseEntity<>(customerPageResponse, HttpStatus.OK);
    }

    //purpose: admin changes customer status
    //gateway rule: hasRole("ADMIN")
    @PatchMapping("/admin/customers/{customerId}/status")
    public ResponseEntity<Void> updateCustomerStatus(@PathVariable Long customerId,
                                                     @RequestParam CustomerStatus status) {
        customerService.updateCustomerStatus(customerId, status);
        return ResponseEntity.ok().build();
    }
}
