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
     * 注文一覧を取得
     *
     * @return 注文一覧
     */
    @GetMapping("/findAll")
    public ResponseEntity<ResponseModel<List<OrderDto>>> getFindAllUser() {
        List<OrderDto> result = cm901051Service.findByAllOrders();
        return ResponseEntity.ok(ResponseHelper.success(result, "取得に成功しました"));
    }

    /**
     * 注文一覧を検索して取得
     *
     * @param searchRequest 検索条件
     * @return 注文一覧
     */
    @GetMapping("/findOrder")
    public ResponseEntity<ResponseModel<List<OrderDto>>> getFindOrders(SearchRequestDto searchRequest) {
        List<OrderDto> result = cm901051Service.getFindOrders(searchRequest);
        return ResponseEntity.ok(ResponseHelper.success(result, "取得に成功しました"));
    }

    /**
     * 注文詳細一覧を取得
     *
     * @param searchRequest 検索条件
     * @return 注文詳細一覧
     */
    @GetMapping("/findDetails")
    public ResponseEntity<ResponseModel<List<OrderDetailDto>>> getFindOrderDetail(SearchRequestDto searchRequest) {
        List<OrderDetailDto> result = cm901051Service.getFindOrderDetail(searchRequest);
        return ResponseEntity.ok(ResponseHelper.success(result, "取得に成功しました"));
    }

    /**
     * 精算ステータスを更新
     *
     * @param searchRequest 検索条件
     * @return 更新結果
     */
    @PostMapping("/uptStatus")
    public ResponseEntity<ResponseModel<Integer>> uptStatus(@RequestBody SearchRequestDto searchRequest,
            HttpServletRequest request) {
        String userId = jwtTokenProvider
                .getUserIdFromToken(jwtTokenProvider.resolveToken(request))
                .toString();
        Integer result = cm901051Service.uptStatus(searchRequest, userId);
        return ResponseEntity.ok(ResponseHelper.success(result, "更新に成功しました"));
    }

    /**
     * 配送先情報を取得
     *
     * @return 配送先情報
     */
    @GetMapping("/address")
    public ResponseEntity<ResponseModel<AddressDto>> getAddress(SearchRequestDto searchRequest) {
        AddressDto result = cm901051Service.getAddress(searchRequest);
        return ResponseEntity.ok(ResponseHelper.success(result, "取得に成功しました"));
    }
}
