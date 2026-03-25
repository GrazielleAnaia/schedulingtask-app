package com.grazielleanaia.registration_api.business.dto;


public class AddressRequestDTO {

    private String street;

    private String complement;

    private String city;

    private String state;

    private Long zipCode;

    private CustomerRequestDTO customerRequestDTO;

    public AddressRequestDTO() {
    }

    public AddressRequestDTO(String street, String complement, String city, String state, Long zipCode, CustomerRequestDTO customerRequestDTO) {
        this.street = street;
        this.complement = complement;
        this.city = city;
        this.state = state;
        this.zipCode = zipCode;
        this.customerRequestDTO = customerRequestDTO;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getComplement() {
        return complement;
    }

    public void setComplement(String complement) {
        this.complement = complement;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public Long getZipCode() {
        return zipCode;
    }

    public void setZipCode(Long zipCode) {
        this.zipCode = zipCode;
    }

    public CustomerRequestDTO getCustomerDTO() {
        return customerRequestDTO;
    }

    public void setCustomerDTO(CustomerRequestDTO customerRequestDTO) {
        this.customerRequestDTO = customerRequestDTO;
    }
}
