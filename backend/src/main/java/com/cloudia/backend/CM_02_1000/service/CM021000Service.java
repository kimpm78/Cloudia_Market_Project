package com.cloudia.backend.CM_02_1000.service;

import java.util.List;

import org.springframework.http.ResponseEntity;

import com.cloudia.backend.CM_02_1000.model.HeaderMenu;
import com.cloudia.backend.common.model.ResponseModel;
import com.cloudia.backend.CM_02_1000.model.Cart;
import com.cloudia.backend.CM_02_1000.model.BannerInfo;

public interface CM021000Service {

    /**
     * ヘッダーメニュー一覧取得
     *
     * @return HeaderMenu 一覧
     */
    List<HeaderMenu> findHeaderMenus();

    /**
     * アイコンメニュー一覧取得
     *
     * @return HeaderMenu 一覧
     */
    List<HeaderMenu> findIconMenus();
    
    /**
     * カート要約情報取得
     *
     * @return カート情報（商品一覧・合計数量・合計金額を含む）
     */
    Cart getCart();

    /**
     * ヘッダーのバッジ表示用カート商品件数取得
     *
     * @param userId ユーザーID
     * @return カート内の商品件数
     */
    int getCartItemCount(Long userId);

    /**
     * カート合計決済金額取得
     *
     * @param userId ユーザーID
     * @return カート合計金額（ウォン）
     */
    int getCartTotalAmount(Long userId);
    
    /**
     * バナー一覧取得
     * 
     * @return バナー一覧
     */
    ResponseEntity<ResponseModel<List<BannerInfo>>> findByAllBanner();
}