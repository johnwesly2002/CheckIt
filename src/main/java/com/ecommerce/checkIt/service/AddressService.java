package com.ecommerce.checkIt.service;

import com.ecommerce.checkIt.payload.AddressDTO;

import java.util.List;

public interface AddressService {
    AddressDTO createAddress(AddressDTO addressDTO);

    List<AddressDTO> getAllAddresses();

    AddressDTO getAddressByAddressId(Long addressId);

    List<AddressDTO> getUserAddresses();

    AddressDTO updateAddressByAddressId(Long addressId, AddressDTO addressDTO);

    String deleteAddressById(Long addressId);
}
