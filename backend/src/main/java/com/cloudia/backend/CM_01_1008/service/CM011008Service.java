package com.cloudia.backend.CM_01_1008.service;

import com.cloudia.backend.CM_01_1008.model.DeliveryAddress;
import org.springframework.http.ResponseEntity;
import java.util.List;
import java.util.Map;

public interface CM011008Service {
    ResponseEntity<List<DeliveryAddress>> getAddressesByUsername(String username);

    ResponseEntity<Map<String, Object>> addAddress(String username, DeliveryAddress address);

    ResponseEntity<DeliveryAddress> getAddressById(Integer addressId);

    ResponseEntity<Map<String, Object>> updateAddress(String username, Integer addressId, DeliveryAddress address);

    ResponseEntity<Void> deleteAddress(Integer addressId);

    ResponseEntity<Void> setDefaultAddress(String username, Integer addressId);
}
