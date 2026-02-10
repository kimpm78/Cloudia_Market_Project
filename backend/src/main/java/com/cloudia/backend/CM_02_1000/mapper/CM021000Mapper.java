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
     * ヘッダーメニュー取得
     */
    List<HeaderMenu> findHeaderMenus();
    /*
     * アイコンメニュー取得
     */
    List<HeaderMenu> findIconMenus(); 
    
    List<CartItem> findActiveCartItemsByUserId(Long userId);
    Integer selectCartTotalAmount(@Param("userId") Long userId);
    /**
     * ヘッダーのバッジ表示用カート商品件数取得
     *
     * @param userId ユーザーID
     * @return カート内の商品件数
     */
    Integer selectCartItemCount(@Param("userId") Long userId);
    /**
     * バナー一覧取得
     * 
     * @return バナー一覧
     */
    List<BannerInfo> findByAllBanner();
}
