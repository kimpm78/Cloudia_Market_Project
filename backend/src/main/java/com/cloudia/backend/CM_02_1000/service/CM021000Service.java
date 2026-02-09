package com.cloudia.backend.CM_02_1000.service;

import java.util.List;

import org.springframework.http.ResponseEntity;

import com.cloudia.backend.CM_02_1000.model.HeaderMenu;
import com.cloudia.backend.CM_02_1000.model.ResponseModel;
import com.cloudia.backend.CM_02_1000.model.Cart;
import com.cloudia.backend.CM_02_1000.model.BannerInfo;

public interface CM021000Service {

    /**
     * 전체 헤더 메뉴 리스트를 조회
     *
     * @return HeaderMenu 리스트
     */
    List<HeaderMenu> findHeaderMenus();

    /**
     * 아이콘 메뉴 리스트를 조회
     *
     * @return HeaderMenu 리스트
     */
    List<HeaderMenu> findIconMenus();
    
    /**
     * 장바구니 요약 정보 조회
     *
     * @return 장바구니 정보 (상품 목록, 총 수량, 총 금액 포함)
     */
    Cart getCart();

    /**
     * 헤더 배지 표시용 장바구니 아이템 개수 조회
     *
     * @param userId 사용자 ID
     * @return 담긴 상품 개수
     */
    int getCartItemCount(Long userId);

    /**
     * 장바구니 총 결제 금액 조회
     *
     * @param userId 사용자 ID
     * @return 장바구니 총 금액(원)
     */
    int getCartTotalAmount(Long userId);
    
    /**
     * 배너 전체 리스트 조회
     * 
     * @return 배너 전체 리스트
     */
    ResponseEntity<ResponseModel<List<BannerInfo>>> findByAllBanner();
}