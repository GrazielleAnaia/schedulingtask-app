package com.grazielleanaia.registration_api.business.dto;


import java.util.List;


public class CustomerRequestDTO {

    private String name;

    private String email;

    private String password;

    private List<PhoneRequestDTO> phoneList;

    private List<AddressRequestDTO> addressDTOList;

    public CustomerRequestDTO() {
    }

    public CustomerRequestDTO(String name, String email, String password,
                              List<PhoneRequestDTO> phoneList, List<AddressRequestDTO> addressDTOList) {
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

    public List<PhoneRequestDTO> getPhoneList() {
        return phoneList;
    }

    public void setPhoneList(List<PhoneRequestDTO> phoneList) {
        this.phoneList = phoneList;
    }

    public List<AddressRequestDTO> getAddressDTOList() {
        return addressDTOList;
    }

    public void setAddressDTOList(List<AddressRequestDTO> addressDTOList) {
        this.addressDTOList = addressDTOList;
    }
}
