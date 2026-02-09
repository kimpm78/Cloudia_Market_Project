package com.cloudia.backend.CM_90_1051.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cloudia.backend.CM_90_1051.model.AddressDto;
import com.cloudia.backend.CM_90_1051.model.OrderDetailDto;
import com.cloudia.backend.CM_90_1051.model.OrderDto;
import com.cloudia.backend.CM_90_1051.model.SearchRequestDto;
import com.cloudia.backend.CM_90_1051.service.CM901051Service;
import com.cloudia.backend.common.model.ResponseModel;
import com.cloudia.backend.common.util.ResponseHelper;
import com.cloudia.backend.config.jwt.JwtTokenProvider;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/admin/settlement/status")
public class CM901051Controller {

    private final CM901051Service cm901051Service;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * 주문 전체 리스트 조회
     * 
     * @return 주문 전체 리스트
     */
    @GetMapping("/findAll")
    public ResponseEntity<ResponseModel<List<OrderDto>>> getFindAllUser() {
        List<OrderDto> result = cm901051Service.findByAllOrders();
        return ResponseEntity.ok(ResponseHelper.success(result, "조회 성공"));
    }

    /**
     * 주문 전체 리스트 조회
     * 
     * @param 검색 데이터
     * @return 주문 전체 리스트
     */
    @GetMapping("/findOrder")
    public ResponseEntity<ResponseModel<List<OrderDto>>> getFindOrders(SearchRequestDto searchRequest) {
        List<OrderDto> result = cm901051Service.getFindOrders(searchRequest);
        return ResponseEntity.ok(ResponseHelper.success(result, "조회 성공"));
    }

    /**
     * 주문 상세 리스트 조회
     * 
     * @param 검색 데이터
     * @return 주문 상세 리스트
     */
    @GetMapping("/findDetails")
    public ResponseEntity<ResponseModel<List<OrderDetailDto>>> getFindOrderDetail(SearchRequestDto searchRequest) {
        List<OrderDetailDto> result = cm901051Service.getFindOrderDetail(searchRequest);
        return ResponseEntity.ok(ResponseHelper.success(result, "조회 성공"));
    }

    /**
     * 정산 상태 업데이트
     * 
     * @param 검색 데이터
     * @return 성공 여부
     */
    @PostMapping("/uptStatus")
    public ResponseEntity<ResponseModel<Integer>> uptStatus(@RequestBody SearchRequestDto searchRequest,
            HttpServletRequest request) {
        String userId = jwtTokenProvider
                .getUserIdFromToken(jwtTokenProvider.resolveToken(request))
                .toString();
        Integer result = cm901051Service.uptStatus(searchRequest, userId);
        return ResponseEntity.ok(ResponseHelper.success(result, "업데이트 성공"));
    }

    /**
     * 배송지 정보
     * 
     * @return 배송지 정보
     */
    @GetMapping("/address")
    public ResponseEntity<ResponseModel<AddressDto>> getAddress(SearchRequestDto searchRequest) {
        AddressDto result = cm901051Service.getAddress(searchRequest);
        return ResponseEntity.ok(ResponseHelper.success(result, "조회 성공"));
    }
}
