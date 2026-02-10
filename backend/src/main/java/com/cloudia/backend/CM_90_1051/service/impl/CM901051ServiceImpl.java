package com.cloudia.backend.CM_90_1051.service.impl;

import java.text.DecimalFormat;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cloudia.backend.CM_90_1051.constants.CM901051Constant;
import com.cloudia.backend.CM_90_1051.constants.CM901051MessageConstant;
import com.cloudia.backend.CM_90_1051.mapper.CM901051Mapper;
import com.cloudia.backend.CM_90_1051.model.AddressDto;
import com.cloudia.backend.CM_90_1051.model.OrderDetailDto;
import com.cloudia.backend.CM_90_1051.model.OrderDto;
import com.cloudia.backend.CM_90_1051.model.SearchRequestDto;
import com.cloudia.backend.CM_90_1051.service.CM901051Service;
import com.cloudia.backend.common.exception.AuthenticationException;
import com.cloudia.backend.common.exception.ErrorCode;
import com.cloudia.backend.common.exception.InvalidRequestException;
import com.cloudia.backend.common.log.LogHelper;
import com.cloudia.backend.common.log.LogMessage;
import com.cloudia.backend.common.model.EmailDto;
import com.cloudia.backend.common.model.ResponseModel;
import com.cloudia.backend.common.service.EmailService;
import com.cloudia.backend.common.util.DateCalculator;
import com.cloudia.backend.constants.CMMessageConstant;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CM901051ServiceImpl implements CM901051Service {

    private final CM901051Mapper cm901051Mapper;
    private final EmailService emailService;
    private final DateCalculator dateCalculator;

    /**
     * 注文全件リスト取得
     * 
     * @return 注文全件リスト
     */
    @Override
    @Transactional(readOnly = true)
    public List<OrderDto> findByAllOrders() {
        LogHelper.log(LogMessage.COMMON_SELECT_START, new String[] { "精算ステータス確認" });

        List<OrderDto> responseList = cm901051Mapper.findByAllOrders();

        if (responseList == null) {
            responseList = Collections.emptyList();
        }

        LogHelper.log(LogMessage.COMMON_SELECT_SUCCESS,
                new String[] { "精算ステータス確認", String.valueOf(responseList.size()) });

        return responseList;
    }

    /**
     * 条件付き注文リスト取得
     * 
     * @param searchRequest 検索条件
     * @return 条件付き注文リスト
     */
    @Override
    @Transactional(readOnly = true)
    public List<OrderDto> getFindOrders(SearchRequestDto searchRequest) {
        LogHelper.log(LogMessage.COMMON_SELECT_START, new String[] { "精算ステータス確認" });

        List<OrderDto> responseList = cm901051Mapper.getFindOrder(searchRequest);

        if (responseList == null) {
            responseList = Collections.emptyList();
        }

        LogHelper.log(LogMessage.COMMON_SELECT_SUCCESS,
                new String[] { "精算ステータス確認", String.valueOf(responseList.size()) });

        return responseList;
    }

    /**
     * 条件付き注文詳細リスト取得
     * 
     * @param searchRequest 検索条件
     * @return 条件付き注文詳細リスト
     */
    @Override
    @Transactional(readOnly = true)
    public List<OrderDetailDto> getFindOrderDetail(SearchRequestDto searchRequest) {
        LogHelper.log(LogMessage.COMMON_SELECT_START, new String[] { "精算ステータス確認" });

        List<OrderDetailDto> responseList = cm901051Mapper.getFindOrderDetail(searchRequest);

        if (responseList == null) {
            responseList = Collections.emptyList();
        }

        LogHelper.log(LogMessage.COMMON_SELECT_SUCCESS,
                new String[] { "精算ステータス確認", String.valueOf(responseList.size()) });

        return responseList;

    }

    /**
     * 配送先情報
     * 
     * @return 配送先情報
     */
    @Override
    @Transactional(readOnly = true)
    public AddressDto getAddress(SearchRequestDto searchRequest) {
        LogHelper.log(LogMessage.COMMON_SELECT_START, new String[] { "精算ステータス確認" });

        AddressDto responseList = cm901051Mapper.getAddress(searchRequest);

        return responseList;
    }

    /**
     * 精算ステータス更新
     * 
     * @param searchRequest 更新リクエストデータ
     * @return 成功可否
     */
    @Override
    @Transactional
    public Integer uptStatus(SearchRequestDto searchRequest, String userId) {
        if (searchRequest == null) {
            LogHelper.log(LogMessage.COMMON_UPDATE_EMPTY, new String[] { "精算ステータス確認" });
            throw new InvalidRequestException(ErrorCode.INVALID_INPUT_VALUE);
        }
        if (null == userId || userId.isBlank()) {
            LogHelper.log(LogMessage.AUTH_TOKEN_INVALID, new String[] { "精算ステータス確認" });
            throw new AuthenticationException(ErrorCode.INVALID_TOKEN);
        }

        OrderDto entity = new OrderDto();

        if (CM901051Constant.ORDER_STATUS_SHIPPING == searchRequest.getOrderStatusValue()) {
            entity.setShippingCompany(searchRequest.getCarrier());
            entity.setTrackingNumber(searchRequest.getTrackingNumber());
            entity.setShippingDate(dateCalculator.DateString());
        }
        entity.setMemberNumber(searchRequest.getMemberNumber());
        entity.setOrderNumber(searchRequest.getOrderNumber());
        entity.setOrderStatusValue(searchRequest.getOrderStatusValue());
        entity.setUpdatedBy(userId);
        entity.setUpdatedAt(dateCalculator.tokyoTime());

        int result = cm901051Mapper.uptStatus(entity);

        if (result > 0 && CM901051Constant.ORDER_STATUS_REMITTANCE_PENDING != searchRequest.getOrderStatusValue()) {
            ResponseEntity<ResponseModel<Integer>> emailResult = processEmailByStatus(searchRequest, entity);
            if (emailResult != null) {
                LogHelper.log(LogMessage.COMMON_UPDATE_EMPTY, new String[] { "精算ステータス確認" });
                throw new InvalidRequestException(ErrorCode.INVALID_INPUT_VALUE);
            }
        }

        LogHelper.log(LogMessage.COMMON_UPDATE_SUCCESS, new String[] { "ユーザー一覧", entity.getMemberNumber() });

        return result;
    }

    /**
     * 注文ステータスに応じたメール送信処理
     * 
     * @param searchRequest 検索リクエストデータ
     * @param entity        注文エンティティ
     * @return エラー時はエラーレスポンス、正常時はnull
     */
    private ResponseEntity<ResponseModel<Integer>> processEmailByStatus(SearchRequestDto searchRequest,
            OrderDto entity) {
        log.info(CM901051MessageConstant.EMAIL_START);
        try {
            List<OrderDto> orders = cm901051Mapper.getFindOrders(searchRequest);
            List<OrderDetailDto> responseList = cm901051Mapper.getFindOrderDetail(searchRequest);

            EmailDto emailInfo = new EmailDto();

            emailInfo.setOrderDate(dateCalculator.convertToYYMMDD(orders.get(0).getOrderDate(), 0));
            emailInfo.setOrderNumber(orders.get(0).getOrderNumber());
            emailInfo.setPaymentAmount(new DecimalFormat("#,###").format(orders.get(0).getTotalAmount()));
            emailInfo.setSendEmail(orders.get(0).getEmail());
            emailInfo.setName(orders.get(0).getName());

            if (CM901051Constant.PAYMENT_METHOD_BANK_TRANSFER == orders.get(0).getPaymentValue()) {
                emailInfo.setPaymentMethod(CM901051Constant.PAYMENT_METHOD_BANK_TRANSFER_STRING);
            } else if (CM901051Constant.PAYMENT_METHOD_CREDIT_CARD == orders.get(0).getPaymentValue()) {
                emailInfo.setPaymentMethod(CM901051Constant.PAYMENT_METHOD_CREDIT_CARD_STRING);
            }

            StringBuffer str = new StringBuffer();

            for (int i = 0; i < responseList.size(); i++) {
                OrderDetailDto data = responseList.get(i);

                str.append("・")
                        .append(data.getProductName())
                        .append(" X ")
                        .append(data.getQuantity());

                if (i < responseList.size() - 1) {
                    str.append("<br>\n");
                }
            }
            emailInfo.setOrderItems(str.toString());

            if (CM901051Constant.ORDER_STATUS_CONFIRMED == searchRequest.getOrderStatusValue()) {
                return sendOrderConfirmation(searchRequest, emailInfo);
            }
            if (CM901051Constant.ORDER_STATUS_PREPARING_SHIPMENT == searchRequest.getOrderStatusValue()) {
                return sendShippingPreparing(searchRequest, emailInfo);
            }
            if (CM901051Constant.ORDER_STATUS_SHIPPING == searchRequest.getOrderStatusValue()) {
                return sendShippingInProgress(searchRequest, emailInfo);
            }
            if (CM901051Constant.ORDER_STATUS_DELIVERED == searchRequest.getOrderStatusValue()) {
                return sendShippingCompleted(searchRequest, emailInfo);
            }
            if (CM901051Constant.ORDER_STATUS_CANCELED == searchRequest.getOrderStatusValue()) {
                return sendCancel(searchRequest, emailInfo);
            }
        } catch (IllegalArgumentException iae) {
            log.error(CM901051MessageConstant.EMAIL_SEND_INPUT_ERROR, searchRequest.getOrderNumber(),
                    iae.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createResponseModel(0, false,
                            CM901051MessageConstant.EMAIL_SEND_FAILED_INPUT.replace("{}", iae.getMessage())));
        } catch (RuntimeException re) {
            log.error(CM901051MessageConstant.EMAIL_SEND_SYSTEM_ERROR, searchRequest.getOrderNumber(),
                    re.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createResponseModel(0, false, CM901051MessageConstant.EMAIL_SEND_FAILED_SYSTEM));

        } catch (Exception emailException) {
            log.error(CM901051MessageConstant.EMAIL_SEND_GENERAL_ERROR, searchRequest.getOrderNumber(),
                    emailException.getMessage(), emailException);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createResponseModel(0, false, CM901051MessageConstant.EMAIL_SEND_FAILED_GENERAL));
        }
        return null;
    }

    /**
     * メール送信処理（ステータス：購入確定）
     * 
     * @param searchRequest 検索リクエストデータ
     * @param entity        注文エンティティ
     * @return エラー時はエラーレスポンス、正常時はnull
     */
    private ResponseEntity<ResponseModel<Integer>> sendOrderConfirmation(SearchRequestDto searchRequest,
            EmailDto emailInfo) {
        return sendEmailWithOrderValidation(searchRequest, () -> {
            emailService.sendOrderConfirmation(emailInfo);
        });
    }

    /**
     * メール送信処理（ステータス：発送準備中）
     * 
     * @param searchRequest 検索リクエストデータ
     * @param entity        注文エンティティ
     * @return エラー時はエラーレスポンス、正常時はnull
     */
    private ResponseEntity<ResponseModel<Integer>> sendShippingPreparing(SearchRequestDto searchRequest,
            EmailDto emailInfo) {
        emailInfo.setShippingDate(dateCalculator.convertToYYMMDD(dateCalculator.tokyoTime(), 3));
        return sendEmailWithOrderValidation(searchRequest, () -> {
            emailService.sendShippingPreparing(emailInfo);
        });
    }

    /**
     * メール送信処理（ステータス：配送中）
     * 
     * @param searchRequest 検索リクエストデータ
     * @param entity        注文エンティティ
     * @return エラー時はエラーレスポンス、正常時はnull
     */
    private ResponseEntity<ResponseModel<Integer>> sendShippingInProgress(SearchRequestDto searchRequest,
            EmailDto emailInfo) {
        if (searchRequest.getCarrier() != null && searchRequest.getTrackingNumber() != null) {
            emailInfo.setTrackingNumber(searchRequest.getTrackingNumber());
            return sendEmailWithOrderValidation(searchRequest, () -> {
                emailService.sendShippingInProgress(emailInfo);
            });
        }
        return null;
    }

    /**
     * メール送信処理（ステータス：配送完了（手動））
     * 
     * @param searchRequest 検索リクエストデータ
     * @param entity        注文エンティティ
     * @return エラー時はエラーレスポンス、正常時はnull
     */
    private ResponseEntity<ResponseModel<Integer>> sendShippingCompleted(SearchRequestDto searchRequest,
            EmailDto emailInfo) {
        return sendEmailWithOrderValidation(searchRequest, () -> {
            emailService.sendShippingCompleted(emailInfo);
        });
    }

    /**
     * メール送信処理（ステータス：購入キャンセル）
     * 
     * @param searchRequest 検索リクエストデータ
     * @param entity        注文エンティティ
     * @return エラー時はエラーレスポンス、正常時はnull
     */
    private ResponseEntity<ResponseModel<Integer>> sendCancel(SearchRequestDto searchRequest,
            EmailDto emailInfo) {
        return sendEmailWithOrderValidation(searchRequest, () -> {
            emailService.sendCancel(emailInfo);
        });
    }

    /**
     * 注文情報を検証してからメール送信
     * 
     * @param searchRequest 検索リクエストデータ
     * @param emailSender   メール送信ロジック
     * @return エラー時はエラーレスポンス、正常時はnull
     */
    private ResponseEntity<ResponseModel<Integer>> sendEmailWithOrderValidation(SearchRequestDto searchRequest,
            Runnable emailSender) {
        log.info(CM901051MessageConstant.EMAIL_VALIDATION);
        try {
            List<OrderDto> orders = cm901051Mapper.getFindOrders(searchRequest);
            if (orders != null && !orders.isEmpty()) {
                OrderDto orderInfo = orders.get(0);

                if (orderInfo == null) {
                    log.error(CM901051MessageConstant.EMAIL_ORDER_INFO_QUERY_ERROR, searchRequest.getOrderNumber());
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(createResponseModel(0, false,
                                    CM901051MessageConstant.EMAIL_CUSTOMER_INFO_NOT_FOUND));
                }

                emailSender.run();
                log.info(CM901051MessageConstant.EMAIL_SEND_SUCCESS, searchRequest.getOrderNumber());
            } else {
                log.error(CM901051MessageConstant.EMAIL_ORDER_QUERY_FAILED, searchRequest.getOrderNumber());
                return ResponseEntity
                        .ok(createResponseModel(null, false, CM901051MessageConstant.EMAIL_ORDER_INFO_NOT_FOUND));
            }

        } catch (IllegalArgumentException iae) {
            log.error(CM901051MessageConstant.EMAIL_SEND_INPUT_ERROR, searchRequest.getOrderNumber(),
                    iae.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createResponseModel(0, false,
                            CM901051MessageConstant.EMAIL_SEND_FAILED_INPUT.replace("{}", iae.getMessage())));
        } catch (RuntimeException re) {
            log.error(CM901051MessageConstant.EMAIL_SEND_SYSTEM_ERROR, searchRequest.getOrderNumber(),
                    re.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createResponseModel(0, false, CM901051MessageConstant.EMAIL_SEND_FAILED_SYSTEM));

        } catch (Exception emailException) {
            log.error(CM901051MessageConstant.EMAIL_SEND_GENERAL_ERROR, searchRequest.getOrderNumber(),
                    emailException.getMessage(), emailException);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createResponseModel(0, false, CM901051MessageConstant.EMAIL_SEND_FAILED_GENERAL));
        }

        return null;
    }

    /**
     * 共通レスポンスモデル生成
     * 
     * @param resultList 結果データ
     * @param result     処理結果
     * @param message    レスポンスメッセージ
     * @return ResponseModel オブジェクト
     */
    private <T> ResponseModel<T> createResponseModel(T resultList, boolean result, String message) {
        return ResponseModel.<T>builder()
                .resultList(resultList)
                .result(result)
                .message(Objects.requireNonNull(message, CMMessageConstant.MESSAGE_NULL_NOT_ALLOWED))
                .build();
    }
}