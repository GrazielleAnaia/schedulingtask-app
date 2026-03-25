package com.grazielleanaia.registration_api.business.dto;


public class PhoneRequestDTO {

    private String number;

    private CustomerRequestDTO customerRequestDTO;

    public PhoneRequestDTO() {
    }

    public PhoneRequestDTO(String number, CustomerRequestDTO customerRequestDTO) {
        this.number = number;

        this.customerRequestDTO = customerRequestDTO;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public CustomerRequestDTO getCustomerDTO() {
        return customerRequestDTO;
    }

    public void setCustomerDTO(CustomerRequestDTO customerRequestDTO) {
        this.customerRequestDTO = customerRequestDTO;
    }
}
