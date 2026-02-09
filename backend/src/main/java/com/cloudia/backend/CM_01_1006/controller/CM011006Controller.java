package com.cloudia.backend.CM_01_1006.controller;

import com.cloudia.backend.CM_01_1006.constants.CM011006MessageConstant;
import com.cloudia.backend.CM_01_1006.model.InquiryDetailResponse;
import com.cloudia.backend.CM_01_1006.model.InquiryProductDTO;
import com.cloudia.backend.CM_01_1006.model.InquiryResponseDTO;
import com.cloudia.backend.CM_01_1006.model.InquiryWriteDTO;
import com.cloudia.backend.CM_01_1006.service.CM011006Service;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CM011006Controller {

    private final CM011006Service inquiryService;

    @GetMapping("/user/mypage/inquiries")
    public ResponseEntity<List<InquiryResponseDTO>> getMyInquiries(
            @AuthenticationPrincipal UserDetails userDetails) {

        String loginId = userDetails.getUsername();
        try {
            // Service에 String loginId 전달
            List<InquiryResponseDTO> inquiries = inquiryService.getMyInquiries(loginId);
            return ResponseEntity.ok(inquiries);
        } catch (Exception e) {
            log.error(CM011006MessageConstant.LOG_INQUIRY_LIST_ERROR, loginId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/products/list")
    public ResponseEntity<List<InquiryProductDTO>> getProductList() {
        try {
            List<InquiryProductDTO> list = inquiryService.getProductList();
            return ResponseEntity.ok(list);
        } catch (Exception e) {
            log.error(CM011006MessageConstant.LOG_PRODUCT_LIST_ERROR, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/user/mypage/inquiries/{inquiryId}")
    public ResponseEntity<InquiryDetailResponse> getInquiryDetail(
            @PathVariable Long inquiryId,
            @AuthenticationPrincipal UserDetails userDetails) {

        String loginId = userDetails.getUsername();
        return inquiryService.getInquiryDetail(inquiryId, loginId);
    }

    @PostMapping("/user/mypage/inquiries")
    public ResponseEntity<?> createInquiry(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody InquiryWriteDTO writeDTO,
            BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            String message = bindingResult.getAllErrors().stream()
                    .map(error -> error.getDefaultMessage())
                    .collect(Collectors.joining("\n"));
            log.warn(CM011006MessageConstant.LOG_VALIDATION_FAIL, message);
            return ResponseEntity.badRequest().body(Map.of("message", message));
        }

        String loginId = userDetails.getUsername();
        try {
            InquiryResponseDTO createdInquiry = inquiryService.createInquiry(loginId, writeDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdInquiry);
        } catch (RuntimeException e) {
            log.warn(CM011006MessageConstant.LOG_INQUIRY_CREATE_ERROR, loginId, e);
            return ResponseEntity.badRequest().body(Map.of("message", CM011006MessageConstant.MSG_CREATE_ERROR));
        } catch (Exception e) {
            log.error(CM011006MessageConstant.LOG_INQUIRY_CREATE_ERROR, loginId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", CM011006MessageConstant.MSG_CREATE_ERROR));
        }
    }

    @DeleteMapping("/user/mypage/inquiries/{inquiryId}")
    public ResponseEntity<Void> deleteInquiry(
            @PathVariable Long inquiryId,
            @AuthenticationPrincipal UserDetails userDetails) {

        String loginId = userDetails.getUsername();
        return inquiryService.deleteInquiry(inquiryId, loginId);
    }
}