package com.cloudia.backend.CM_01_1015.mapper;

import com.cloudia.backend.CM_01_1015.model.ReturnResponse;
import com.cloudia.backend.CM_01_1015.model.ReturnRequest;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface CM011015Mapper {

        /**
         * 로그인한 사용자의 교환/반품 신청 내역 목록을 조회
         */
        List<ReturnResponse> getReturnList(@Param("userId") int userId);

        /**
         * 특정 교환/반품 신청건의 상세 내역을 조회
         */
        ReturnResponse getReturnDetail(@Param("returnId") int returnId, @Param("userId") int userId);

        /**
         * 신청 화면에서 주문번호 선택 시, 해당 주문에 포함된 상품 목록을 조회
         */
        List<ReturnResponse.ProductInfo> getProductsByOrderNo(@Param("orderNo") String orderNo,
                        @Param("userId") int userId);

        /**
         * 교환/반품 신청서의 마스터 정보를 저장
         */
        void insertReturnRequest(@Param("req") ReturnRequest req, @Param("imageUrls") String imageUrls,
                        @Param("userId") int userId, @Param("createdAt") LocalDateTime createdAt);

        /**
         * 교환/반품 신청서에 포함된 개별 상품의 상세 정보를 저장
         */
        void insertReturnDetail(@Param("productCode") String productCode, @Param("quantity") int quantity,
                        @Param("createdBy") String createdBy);

        /**
         * 주문의 상태를 교환/반품 관련 상태로 업데이트
         */
        void updateToExchangeStatus(@Param("orderNo") String orderNo, @Param("memberNumber") String memberNumber);

        /**
         * 현재 세션에서 가장 최근에 생성된 반품 ID를 조회
         */
        int getCurrentReturnId();

        /**
         * 신청 가능한 구매 확정 주문 목록 조회
         */
        List<Map<String, Object>> getReturnableOrders(@Param("userId") int userId);
}