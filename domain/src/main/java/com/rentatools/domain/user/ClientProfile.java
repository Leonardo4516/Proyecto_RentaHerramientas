package com.rentatools.domain.user;

import com.rentatools.domain.common.BaseEntity;

public class ClientProfile extends BaseEntity<Long> {
    private String companyName;
    private String taxId;
    private String address;
    private String city;
    private String preferredPaymentMethod;

    public ClientProfile() {
        super();
    }

    public ClientProfile(Long id, String companyName, String taxId, String address, String city, String preferredPaymentMethod) {
        super(id);
        this.companyName = companyName;
        this.taxId = taxId;
        this.address = address;
        this.city = city;
        this.preferredPaymentMethod = preferredPaymentMethod;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getTaxId() {
        return taxId;
    }

    public void setTaxId(String taxId) {
        this.taxId = taxId;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getPreferredPaymentMethod() {
        return preferredPaymentMethod;
    }

    public void setPreferredPaymentMethod(String preferredPaymentMethod) {
        this.preferredPaymentMethod = preferredPaymentMethod;
    }
}
