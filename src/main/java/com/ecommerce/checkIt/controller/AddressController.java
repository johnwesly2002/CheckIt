package com.ecommerce.checkIt.controller;

import com.ecommerce.checkIt.payload.AddressDTO;
import com.ecommerce.checkIt.service.AddressService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class AddressController {

    @Autowired
    AddressService addressService;

    @PostMapping("/addresses")
    public ResponseEntity<AddressDTO> createAddress(@Valid  @RequestBody AddressDTO addressDTO) {

        AddressDTO savedAddress = addressService.createAddress(addressDTO);

        return new ResponseEntity<>(savedAddress, HttpStatus.OK);
    }

    @GetMapping("/addresses")
    public ResponseEntity<List<AddressDTO>> getAllAddresses() {
        List<AddressDTO> addressDTOList = addressService.getAllAddresses();
        return new ResponseEntity<List<AddressDTO>>(addressDTOList, HttpStatus.OK);
    }
    @GetMapping("/addresses/{addressId}")
    public ResponseEntity<AddressDTO> getAddressByAddressId(@PathVariable Long addressId) {
        AddressDTO addressDTO = addressService.getAddressByAddressId(addressId);
        return new ResponseEntity<AddressDTO>(addressDTO, HttpStatus.OK);
    }

    @GetMapping("/users/addresses")
    public ResponseEntity<List<AddressDTO>> getAddressesByUserId() {
        List<AddressDTO> addressDTOList = addressService.getUserAddresses();
        return new ResponseEntity<List<AddressDTO>>(addressDTOList, HttpStatus.OK);
    }

    @PutMapping("/addresses/{addressId}")
    public ResponseEntity<AddressDTO> updateAddressByAddressId(@PathVariable Long addressId, @RequestBody AddressDTO addressDTO) {
        AddressDTO updatedAddress = addressService.updateAddressByAddressId(addressId,addressDTO);
        return new ResponseEntity<AddressDTO>(updatedAddress, HttpStatus.OK);
    }

    @DeleteMapping("/addresses/{addressId}")
    public ResponseEntity<String> deleteAddressById(@PathVariable Long addressId) {
        String status = addressService.deleteAddressById(addressId);
        return new ResponseEntity<>(status, HttpStatus.OK);
    }

}
