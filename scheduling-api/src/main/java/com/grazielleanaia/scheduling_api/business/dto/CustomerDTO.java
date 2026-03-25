package com.grazielleanaia.scheduling_api.business.dto;


public class CustomerDTO {

    private Long customerId;

    private String email;

    private String password;

    public CustomerDTO() {
    }

    public CustomerDTO(Long customerId, String email, String password) {
        this.customerId = customerId;
        this.email = email;
        this.password = password;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
