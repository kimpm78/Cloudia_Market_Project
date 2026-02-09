package com.cloudia.backend.CM_02_1000.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.cloudia.backend.CM_02_1000.model.HeaderMenu;
import com.cloudia.backend.CM_02_1000.model.BannerInfo;
import com.cloudia.backend.CM_02_1000.model.CartItem;

@Mapper
public interface CM021000Mapper {
    /*
     * 헤더 메뉴 조회
     */
    List<HeaderMenu> findHeaderMenus();
    /*
     * 아이콘 메뉴 조회
     */
    List<HeaderMenu> findIconMenus(); 
    
    List<CartItem> findActiveCartItemsByUserId(Long userId);
    Integer selectCartTotalAmount(@Param("userId") Long userId);
    /**
     * 헤더 배지 표시용 장바구니 아이템 개수 조회
     *
     * @param userId 사용자 ID
     * @return 담긴 상품 개수
     */
    Integer selectCartItemCount(@Param("userId") Long userId);
    /**
     * 배너 전체 리스트 조회
     * 
     * @return 배너 전체 리스트
     */
    List<BannerInfo> findByAllBanner();
}
