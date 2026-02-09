package com.cloudia.backend.CM_01_1008.controller;

import com.cloudia.backend.CM_01_1008.model.DeliveryAddress;
import com.cloudia.backend.CM_01_1008.service.CM011008Service;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user/addresses")
@RequiredArgsConstructor
public class CM011008Controller {

    private final CM011008Service service;

    @GetMapping
    public ResponseEntity<List<DeliveryAddress>> getAddresses(@AuthenticationPrincipal UserDetails userDetails) {
        return service.getAddressesByUsername(userDetails.getUsername());
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> addAddress(@AuthenticationPrincipal UserDetails userDetails,
            @RequestBody DeliveryAddress address) {
        return service.addAddress(userDetails.getUsername(), address);
    }

    @PutMapping("/{addressId}")
    public ResponseEntity<Map<String, Object>> updateAddress(@AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Integer addressId,
            @RequestBody DeliveryAddress address) {
        return service.updateAddress(userDetails.getUsername(), addressId, address);
    }

    @GetMapping("/{addressId}")
    public ResponseEntity<DeliveryAddress> getAddressById(@PathVariable Integer addressId) {
        return service.getAddressById(addressId);
    }

    @DeleteMapping("/{addressId}")
    public ResponseEntity<Void> deleteAddress(@PathVariable Integer addressId) {
        return service.deleteAddress(addressId);
    }

    @PatchMapping("/{addressId}/default")
    public ResponseEntity<Void> setDefaultAddress(@AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Integer addressId) {
        return service.setDefaultAddress(userDetails.getUsername(), addressId);
    }
}
