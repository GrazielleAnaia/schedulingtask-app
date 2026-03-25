package com.grazielleanaia.registration_api.business.dto;


public class AddressResponseDTO {

    private Long id;

    private String street;

    private String complement;

    private String city;

    private String state;

    private Long zipCode;


    public AddressResponseDTO() {
    }

    public AddressResponseDTO(Long zipCode, String state, String city, String complement, String street, Long id) {

        this.zipCode = zipCode;
        this.state = state;
        this.city = city;
        this.complement = complement;
        this.street = street;
        this.id = id;
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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

}
