package com.cloudia.backend.CM_02_1000.service.impl;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.cloudia.backend.CM_02_1000.constants.CM021000MessageConstant;
import com.cloudia.backend.CM_02_1000.mapper.CM021000Mapper;
import com.cloudia.backend.CM_02_1000.model.HeaderMenu;
import com.cloudia.backend.CM_02_1000.model.CartItem;
import com.cloudia.backend.CM_02_1000.model.Cart;
import com.cloudia.backend.CM_02_1000.service.CM021000Service;
import com.cloudia.backend.constants.CMMessageConstant;
import com.cloudia.backend.CM_02_1000.model.BannerInfo;
import com.cloudia.backend.common.model.ResponseModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CM021000ServiceImpl implements CM021000Service {

    private final CM021000Mapper cm021000Mapper;

    /**
     * ヘッダーメニュー一覧取得
     * 
     * @return List<HeaderMenu> ヘッダーメニュー一覧
     */

    @Override
    @Transactional(readOnly = true)
    public List<HeaderMenu> findHeaderMenus() {
        try {
            List<HeaderMenu> headerMenus = cm021000Mapper.findHeaderMenus();
            log.info(CM021000MessageConstant.HEADER_MENU_COUNT, headerMenus == null ? 0 : headerMenus.size());
            return headerMenus;
        } catch (DataAccessException dae) {
            log.error(CM021000MessageConstant.HEADER_MENU_DB_ERROR, dae.getMessage(), dae);
            return Collections.emptyList();
        } catch (NullPointerException npe) {
            log.error(CM021000MessageConstant.HEADER_MENU_NULL_ERROR, npe.getMessage(), npe);
            return Collections.emptyList();
        } catch (Exception e) {
            log.error(CM021000MessageConstant.HEADER_MENU_UNKNOWN_ERROR, e.getMessage(), e);
            return Collections.emptyList();
        }
    }
    /**
     * アイコンメニュー一覧取得
     * 
     * @return List<HeaderMenu> アイコンメニュー一覧
     */
    @Override
    @Transactional(readOnly = true)
    public List<HeaderMenu> findIconMenus() {
        try {
            List<HeaderMenu> iconMenus = cm021000Mapper.findIconMenus();
            log.info(CM021000MessageConstant.HEADER_ICON_COUNT, iconMenus == null ? 0 : iconMenus.size());
            return iconMenus;
        } catch (DataAccessException dae) {
            log.error(CM021000MessageConstant.HEADER_ICON_DB_ERROR, dae.getMessage(), dae);
            return Collections.emptyList();
        } catch (NullPointerException npe) {
            log.error(CM021000MessageConstant.HEADER_ICON_NULL_ERROR, npe.getMessage(), npe);
            return Collections.emptyList();
        } catch (Exception e) {
            log.error(CM021000MessageConstant.HEADER_ICON_UNKNOWN_ERROR, e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    /**
     * ヘッダーのバッジ表示用カート商品件数取得
     *
     * @param userId ユーザーID
     * @return カート内の商品件数（NULL防止：デフォルト0）
     */
    @Override
    @Transactional(readOnly = true)
    public int getCartItemCount(Long userId) {
        try {
            Integer count = cm021000Mapper.selectCartItemCount(userId);
            return count == null ? 0 : count;
        } catch (DataAccessException dae) {
            log.error(CM021000MessageConstant.HEADER_MENU_DB_ERROR, dae.getMessage(), dae);
            return 0;
        } catch (Exception e) {
            log.error(CM021000MessageConstant.HEADER_MENU_UNKNOWN_ERROR, e.getMessage(), e);
            return 0;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public int getCartTotalAmount(Long userId) {
        if (userId == null) {
            return 0;
        }
        try {
            Integer total = cm021000Mapper.selectCartTotalAmount(userId);
            return total == null ? 0 : total;
        } catch (DataAccessException dae) {
            log.error("カート合計金額取得中にDBエラーが発生: {}", dae.getMessage(), dae);
            return 0;
        } catch (Exception e) {
            log.error("カート合計金額取得中に予期しないエラーが発生: {}", e.getMessage(), e);
            return 0;
        }
    }

    /**
     * カート取得
     * 
     * @return Cart ユーザーのカート情報
     */
    @Override
    @Transactional(readOnly = true)
    public Cart getCart() {
        Long userId = getCurrentUserId(); // 実際のログインユーザーID取得方式に置き換え予定
        if (userId == null) {
            log.warn(CM021000MessageConstant.CART_LOGIN_REQUIRED);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, CM021000MessageConstant.CART_LOGIN_REQUIRED);
        }

        List<CartItem> items = cm021000Mapper.findActiveCartItemsByUserId(userId);
        int itemCount = items.stream().mapToInt(CartItem::getQuantity).sum();
        int totalAmount = getCartTotalAmount(userId);

        Cart cart = new Cart(items, itemCount, totalAmount);
        log.info(CM021000MessageConstant.CART_GET_SUCCESS, itemCount, totalAmount);
        return cart;
    }

    /**
     * 現在ログインしているユーザーIDを返却
     *
     * @return Long ユーザーID（未ログインの場合はnull）
     */
    private Long getCurrentUserId() {
        return null;
    }
    /**
     * バナー一覧取得
     * 
     * @return バナー一覧
     */
    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<ResponseModel<List<BannerInfo>>> findByAllBanner() {
        log.info(CM021000MessageConstant.BANNER_FIND_ALL_START);

        try {
            List<BannerInfo> bannerInfoList = cm021000Mapper.findByAllBanner();
            if (bannerInfoList == null) {
                bannerInfoList = Collections.emptyList();
            }

            log.info(CM021000MessageConstant.BANNER_FIND_ALL_COMPLETE, bannerInfoList.size());
            return ResponseEntity
                .ok(createResponseModel(bannerInfoList, true, CM021000MessageConstant.SUCCESS_BANNER_FIND));

        } catch (DataAccessException dae) {
            log.error(CM021000MessageConstant.BANNER_FIND_ALL_DB_ERROR, dae.getMessage(), dae);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createResponseModel(Collections.emptyList(), false, CMMessageConstant.ERROR_DATABASE));
        } catch (Exception e) {
            log.error(CM021000MessageConstant.BANNER_FIND_ALL_UNEXPECTED_ERROR, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createResponseModel(Collections.emptyList(), false,
                CMMessageConstant.ERROR_INTERNAL_SERVER));
        }
    }

    /**
     * ResponseModel 生成
     * 
     * @param resultList 結果データ
     * @param result     処理結果
     * @param message    メッセージ
     * @return ResponseModel
     */
    private <T> ResponseModel<T> createResponseModel(T resultList, boolean result, String message) {
        return ResponseModel.<T>builder()
            .resultList(resultList)
            .result(result)
            .message(Objects.requireNonNull(message, CMMessageConstant.MESSAGE_NULL_NOT_ALLOWED))
            .build();
    }
}
