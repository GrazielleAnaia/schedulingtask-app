package com.grazielleanaia.scheduling_api.business.dto;


public class CustomerResponseDTO {

    private Long id;

    public CustomerResponseDTO() {
    }

    public CustomerResponseDTO(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
