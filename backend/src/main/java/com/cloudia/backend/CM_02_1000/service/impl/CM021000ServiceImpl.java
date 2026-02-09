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
import com.cloudia.backend.CM_02_1000.model.ResponseModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 헤더 메뉴 정보를 제공하는 서비스 구현 클래스입니다.
 * DB에서 is_header = true 조건의 메뉴 목록을 조회합니다.
 */

@Service
@RequiredArgsConstructor
@Slf4j
public class CM021000ServiceImpl implements CM021000Service {

    private final CM021000Mapper cm021000Mapper;

    /**
     * 헤더 메뉴 목록 조회
     * 
     * @return List<HeaderMenu> 헤더 메뉴 리스트
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
     * 아이콘 목록 조회
     * 
     * @return List<HeaderMenu> 헤더 메뉴 리스트
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
     * 헤더 배지 표시용 장바구니 아이템 개수 조회
     *
     * @param userId 사용자 ID
     * @return 담긴 상품 개수 (NULL 방지 0 기본)
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
            log.error("장바구니 총액 조회 중 DB 오류: {}", dae.getMessage(), dae);
            return 0;
        } catch (Exception e) {
            log.error("장바구니 총액 조회 중 예상치 못한 오류: {}", e.getMessage(), e);
            return 0;
        }
    }

    /**
     * 장바구니 조회
     * 
     * @return Cart 사용자 장바구니 정보
     */
    @Override
    @Transactional(readOnly = true)
    public Cart getCart() {
        Long userId = getCurrentUserId(); // 실제 로그인 사용자 ID를 가져오는 방식으로 교체 예정
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
     * 현재 로그인한 사용자 ID를 반환합니다.
     * 추후 Spring Security 또는 세션 기반 사용자 인증에서 연동 예정입니다.
     *
     * @return Long 사용자 ID (로그인되어 있지 않으면 null)
     */
    private Long getCurrentUserId() {
        // TODO: 로그인된 사용자 ID를 인증 시스템과 연동하여 반환
        return null;
    }
    /**
     * 배너 전체 리스트 조회
     * 
     * @return 배너 전체 리스트
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
     * ResponseModel 생성
     * 
     * @param resultList 결과 데이터
     * @param result     처리 결과
     * @param message    메시지
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
