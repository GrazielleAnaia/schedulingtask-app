package com.grazielleanaia.registration_api.business.dto;


import java.util.List;


public class CustomerResponseDTO {

    private Long id;

    private String name;

    private String email;

    private String password;

    private List<PhoneResponseDTO> phoneList;

    private List<AddressResponseDTO> addressDTOList;

    public CustomerResponseDTO() {
    }

    public CustomerResponseDTO(Long id, String name, String email, String password, List<PhoneResponseDTO> phoneList, List<AddressResponseDTO> addressDTOList) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.phoneList = phoneList;
        this.addressDTOList = addressDTOList;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public List<PhoneResponseDTO> getPhoneList() {
        return phoneList;
    }

    public void setPhoneList(List<PhoneResponseDTO> phoneList) {
        this.phoneList = phoneList;
    }

    public List<AddressResponseDTO> getAddressDTOList() {
        return addressDTOList;
    }

    public void setAddressDTOList(List<AddressResponseDTO> addressDTOList) {
        this.addressDTOList = addressDTOList;
    }
}
