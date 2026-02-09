package com.cloudia.backend.batch.service.impl;

import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cloudia.backend.batch.mapper.SchedulerMapper;
import com.cloudia.backend.batch.model.OrdersDto;
import com.cloudia.backend.batch.model.SendEmail;
import com.cloudia.backend.batch.service.SchedulerService;
import com.cloudia.backend.common.model.EmailDto;
import com.cloudia.backend.common.service.EmailService;
import com.cloudia.backend.common.util.DateCalculator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class SchedulerServiceImpl implements SchedulerService {
    @Autowired
    private final EmailService emailService;
    private final SchedulerMapper schedulerMapper;
    private final DateCalculator dateCalculator;
    

    /**
     * 予約締切商品のステータス更新
     */
    @Override
    @Transactional
    public void updateResv() {
        try {
            log.info("予約締切商品のステータス更新バッチを実行します");
            int updateRowCount = schedulerMapper.updateProductCount(dateCalculator.DateString());
            log.info("{}件の商品ステータス変更予定", updateRowCount);
            if (updateRowCount > 0) {
                updateRowCount = schedulerMapper.updateProduct(dateCalculator.DateString(),
                        dateCalculator.tokyoTime());
                log.info("更新完了: {}件の商品ステータスを変更しました", updateRowCount);
                log.info("更新日: ", dateCalculator.DateString());
            }
        } catch (DataAccessException dae) {
            // DB関連例外
            log.error("DBアクセス中にエラーが発生: {}", dae.getMessage(), dae);
        } catch (NullPointerException npe) {
            // Null処理例外
            log.error("NullPointerExceptionが発生: {}", npe.getMessage(), npe);
        } catch (Exception e) {
            // その他の一般例外
            log.error("予期しないエラーが発生: {}", e.getMessage(), e);
        }
    }

    /**
     * 翌年の祝日をDBに保存
     */
    @Override
    @Transactional
    public void syncNextYearHolidays() {
        log.info("ローカルモードでは祝日の同期処理を実行しません。");
    }

    /**
     * 銀行振込の支払期限確認メール
     */
    @Override
    @Transactional
    public void sendPaymentDeadline() {
        try {
            log.info("銀行振込の支払期限確認メール送信バッチを実行します");
            int sendRowCount = schedulerMapper.sendPaymentDeadlineNoticeCount(dateCalculator.DateString());
            log.info("{}名のお客様へメール送信予定", sendRowCount);
            if (sendRowCount > 0) {
                StringBuilder orderTableRows = new StringBuilder();
                List<OrdersDto> sendEmailInfo = schedulerMapper.sendPaymentDeadlineNotice(dateCalculator.DateString());
                for (OrdersDto order : sendEmailInfo) {
                    String formattedDeadline = order.getEndDate().format(
                            DateTimeFormatter.ofPattern("yy/MM/dd(E) HH:mm", Locale.JAPAN));

                    boolean isOverdue = LocalDateTime.now().isAfter(order.getEndDate());
                    String rowStyle = isOverdue ? "background-color: #ffebee;" : "background-color: white;";
                    String deadlineStyle = isOverdue ? "color: #dc3545; font-weight: bold;" : "color: #28a745;";

                    orderTableRows.append(String.format(
                            "<tr style=\"%s\">" +
                                    "<td style=\"padding: 12px 8px; text-align: center; border: 1px solid #dee2e6; font-size: 13px;\">%s</td>"
                                    +
                                    "<td style=\"padding: 12px 8px; text-align: center; border: 1px solid #dee2e6; font-size: 13px;\">%s</td>"
                                    +
                                    "<td style=\"padding: 12px 8px; text-align: center; border: 1px solid #dee2e6; font-size: 13px;\">%s</td>"
                                    +
                                    "<td style=\"padding: 12px 8px; text-align: center; border: 1px solid #dee2e6; font-size: 13px; font-weight: bold;\">%s円</td>"
                                    +
                                    "<td style=\"padding: 12px 8px; text-align: center; border: 1px solid #dee2e6; font-size: 13px;\">%s</td>"
                                    +
                                    "<td style=\"padding: 12px 8px; text-align: center; border: 1px solid #dee2e6; font-size: 13px; %s\">%s%s</td>"
                                    +
                                    "</tr>\n",
                            rowStyle,
                            order.getMemberNumber(),
                            order.getOrderNumber(),
                            order.getRecipientName(),
                            new DecimalFormat("#,###").format(order.getTotalAmount()),
                            dateCalculator.convertToYYMMDD(order.getOrderDate(), 0),
                            deadlineStyle,
                            formattedDeadline,
                            isOverdue ? " <span style=\"font-size: 12px;\">⚠️ 注文をキャンセルしてください</span>" : ""));
                }
                List<SendEmail> sendEmails = schedulerMapper.sendEmails();
                List<String> adminEmails = new ArrayList<>();

                for (SendEmail email : sendEmails) {
                    adminEmails.add(email.getEmail());
                }

                EmailDto emailInfo = new EmailDto();
                emailInfo.setPendingCount(String.valueOf(sendRowCount));
                emailInfo.setSendEmails(adminEmails);
                emailService.sendPaymentDeadlineNotice(emailInfo, orderTableRows);
                log.info("メール送信完了");
                log.info("メール送信日: ", dateCalculator.DateString());
            }

        } catch (DataAccessException dae) {
            // DB関連例外
            log.error("DBアクセス中にエラーが発生: {}", dae.getMessage(), dae);
        } catch (NullPointerException npe) {
            // Null処理例外
            log.error("NullPointerExceptionが発生: {}", npe.getMessage(), npe);
        } catch (Exception e) {
            // その他の一般例外
            log.error("予期しないエラーが発生: {}", e.getMessage(), e);
        }
    }

    /**
     * 発送完了案内メッセージ
     */
    public void confirmDelivery() {
        int sendRowCount = schedulerMapper.sendConfirmDeliveryCont(dateCalculator.DateString());
        if (sendRowCount > 0) {
            List<String> adminEmails = new ArrayList<>();
            StringBuilder orderTableRows = new StringBuilder();

            List<SendEmail> sendEmails = schedulerMapper.sendEmails();
            List<OrdersDto> sendEmailInfo = schedulerMapper.sendConfirmDelivery(dateCalculator.DateString());

            for (SendEmail email : sendEmails) {
                adminEmails.add(email.getEmail());
            }

            for (OrdersDto order : sendEmailInfo) {
                orderTableRows.append(String.format(
                        "<tr style=\"%s\">" +
                                "<td style=\"padding: 12px 8px; text-align: center; border: 1px solid #dee2e6; font-size: 13px;\">%s</td>"
                                +
                                "<td style=\"padding: 12px 8px; text-align: center; border: 1px solid #dee2e6; font-size: 13px;\">%s</td>"
                                +
                                "<td style=\"padding: 12px 8px; text-align: center; border: 1px solid #dee2e6; font-size: 13px;\">%s</td>"
                                +
                                "<td style=\"padding: 12px 8px; text-align: center; border: 1px solid #dee2e6; font-size: 13px; font-weight: bold;\">%s円</td>"
                                +
                                "<td style=\"padding: 12px 8px; text-align: center; border: 1px solid #dee2e6; font-size: 13px;\">%s</td>"
                                +
                                "</tr>\n",
                        order.getMemberNumber(),
                        order.getOrderNumber(),
                        order.getRecipientName(),
                        new DecimalFormat("#,###").format(order.getTotalAmount()),
                        dateCalculator.convertToYYMMDD(order.getOrderDate(), 0)));
            }

            EmailDto emailInfo = new EmailDto();
            emailInfo.setPendingCount(String.valueOf(sendRowCount));
            emailInfo.setSendEmails(adminEmails);
        }
    }
}