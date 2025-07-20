package com.ecommerce.checkIt.service;

import com.ecommerce.checkIt.exceptions.ResourceNotFoundException;
import com.ecommerce.checkIt.model.Address;
import com.ecommerce.checkIt.model.User;
import com.ecommerce.checkIt.payload.AddressDTO;
import com.ecommerce.checkIt.repositories.AddressRepository;
import com.ecommerce.checkIt.repositories.UserRepository;
import com.ecommerce.checkIt.utils.AuthUtil;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Stream;

@Service
public class AddressServiceImpl implements AddressService{

    @Autowired
    AuthUtil authUtil;

    @Autowired
    AddressRepository addressRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    ModelMapper modelMapper;

    private static final Logger logger = LoggerFactory.getLogger(AddressServiceImpl.class);

    @Override
    public AddressDTO createAddress(AddressDTO addressDTO) {
        User user = authUtil.loggedInUser();
        Address newAddress = modelMapper.map(addressDTO, Address.class);
        List<Address> addresses = user.getAddresses();
        addresses.add(newAddress);
        newAddress.setUser(user);
        Address address = addressRepository.save(newAddress);
        return modelMapper.map(address, AddressDTO.class);
    }

    @Override
    public List<AddressDTO> getAllAddresses() {
        List<Address> addressList = addressRepository.findAll();
        Stream<AddressDTO> addressDTOStream = addressList.stream().map(address -> {
            return modelMapper.map(address, AddressDTO.class);
        });
        return addressDTOStream.toList();
    }

    @Override
    public AddressDTO getAddressByAddressId(Long addressId) {
        Address address = addressRepository.findById(addressId).orElseThrow(() -> new ResourceNotFoundException("Address", "AddressId", addressId));
        return modelMapper.map(address, AddressDTO.class);
    }

    @Override
    public List<AddressDTO> getUserAddresses() {
        List<Address> addressList = authUtil.loggedInUser().getAddresses();
        Stream<AddressDTO> addressDTOStream = addressList.stream().map(address -> {
            return modelMapper.map(address, AddressDTO.class);
        });
        return addressDTOStream.toList();
    }

    @Override
    public AddressDTO updateAddressByAddressId(Long addressId, AddressDTO addressDTO) {
        Address existingAddress = addressRepository.findById(addressId).orElseThrow(() -> new ResourceNotFoundException("Address", "AddressId", addressId));
        existingAddress.setCity(addressDTO.getCity());
        existingAddress.setCountry(addressDTO.getCountry());
        existingAddress.setBuildingName(addressDTO.getBuildingName());
        existingAddress.setStreet(addressDTO.getStreet());
        existingAddress.setPincode(addressDTO.getPincode());
        existingAddress.setState(addressDTO.getState());
        Address savedAddress = addressRepository.save(existingAddress);
        User user = existingAddress.getUser();
        user.getAddresses().removeIf(address -> address.getAddressId().equals(addressId));
        user.getAddresses().add(savedAddress);
        userRepository.save(user);
        return modelMapper.map(savedAddress, AddressDTO.class);
    }

    @Override
    public String deleteAddressById(Long addressId) {
        Address existingaddress = addressRepository.findById(addressId).orElseThrow(() -> new ResourceNotFoundException("Address", "AddressId", addressId));
        User user = existingaddress.getUser();
        user.getAddresses().removeIf(address -> address.getAddressId().equals(addressId));
        userRepository.save(user);
        addressRepository.delete(existingaddress);
        return "Address Deleted Successfully with addressId " + existingaddress.getAddressId();
    }
}
