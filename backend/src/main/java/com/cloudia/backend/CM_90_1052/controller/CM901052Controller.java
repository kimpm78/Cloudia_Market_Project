package com.cloudia.backend.CM_90_1052.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.http.ResponseEntity;

import com.cloudia.backend.CM_90_1052.model.OrderDetailDto;
import com.cloudia.backend.CM_90_1052.model.RefundRequestDto;
import com.cloudia.backend.CM_90_1052.model.RefundSearchRequestDto;
import com.cloudia.backend.CM_90_1052.model.ReturnsDto;
import com.cloudia.backend.CM_90_1052.service.CM901052Service;
import com.cloudia.backend.common.model.ResponseModel;
import com.cloudia.backend.common.util.ResponseHelper;
import com.cloudia.backend.config.jwt.JwtTokenProvider;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/admin/settlement/refund")
public class CM901052Controller {
    private final CM901052Service cm901052Service;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * 返金・交換一覧取得
     * 
     * @return 返金・交換一覧
     */
    @GetMapping("/findByPeriod")

    public ResponseEntity<ResponseModel<List<ReturnsDto>>> getPeriod(RefundSearchRequestDto searchDto) {
        List<ReturnsDto> result = cm901052Service.getPeriod(searchDto);
        return ResponseEntity.ok(ResponseHelper.success(result, "取得成功"));
    }

    /**
     * 返金・交換一覧取得
     * 
     * @return 返金・交換一覧
     */
    @GetMapping("/findAll")
    public ResponseEntity<ResponseModel<List<ReturnsDto>>> getRefund() {
        List<ReturnsDto> result = cm901052Service.getRefund();
        return ResponseEntity.ok(ResponseHelper.success(result, "取得成功"));
    }

    /**
     * 返金商品一覧
     * 
     * @param requestNo    リクエスト番号
     * @param refundNumber 社員番号
     * @param orderNumber  注文番号
     * @return 返金商品一覧
     */
    @GetMapping("/orderDetail")
    public ResponseEntity<ResponseModel<List<OrderDetailDto>>> getOrderDetail(@RequestParam(required = false) String requestNo,
            @RequestParam String refundNumber,
            @RequestParam String orderNumber) {
        List<OrderDetailDto> result = cm901052Service.getOrderDetail(requestNo, refundNumber, orderNumber);
        return ResponseEntity.ok(ResponseHelper.success(result, "取得成功"));
    }

    /**
     * 旧フロント互換: 顧客情報/注文詳細取得
     */
    @GetMapping("/getCustomerInfo")
    public ResponseEntity<ResponseModel<List<OrderDetailDto>>> getCustomerInfo(@RequestParam(required = false) String requestNo,
            @RequestParam String refundNumber,
            @RequestParam String orderNumber) {
        List<OrderDetailDto> result = cm901052Service.getOrderDetail(requestNo, refundNumber, orderNumber);
        return ResponseEntity.ok(ResponseHelper.success(result, "取得成功"));
    }

    /**
     * 返金処理
     * 
     * @param requestNo 返金情報
     * @return 返金処理更新
     */
    @PostMapping("/process")
    public ResponseEntity<ResponseModel<Integer>> updateRefund(@RequestBody RefundRequestDto requestDto,
            HttpServletRequest request) {
        String userId = jwtTokenProvider
                .getUserIdFromToken(jwtTokenProvider.resolveToken(request))
                .toString();
        Integer result = cm901052Service.updateRefund(requestDto, userId);
        return ResponseEntity.ok(ResponseHelper.success(result, "更新成功"));
    }
}
