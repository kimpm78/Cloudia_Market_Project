package com.cloudia.backend.common.model.pg;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class PGResult {

    /**
    * PG 응답 공통값
    */
    @JsonProperty("RESULTCODE")
    @JsonAlias({"RTN_CD", "rtn_cd"})
    private String resultCode;     // PG 결과 코드

    @JsonProperty("RESULTMSG")
    @JsonAlias({"RTN_MSG", "rtn_msg"})
    private String resultMsg;      // PG 결과 메시지

    @JsonProperty("TID")
    @JsonAlias({"tid", "Tid", "transactionId", "transaction_id"})
    private String tid;            // 거래 ID

    @JsonProperty("ORDERNO")
    private String orderNo;        // 주문번호

    @JsonProperty("AMOUNT")
    private String amount;         // 결제 금액

    @JsonProperty("PAYMETHOD")
    private String payMethod;      // 결제 수단

    @JsonProperty("ACCEPTNO")
    private String acceptNo;       // 승인번호

    @JsonProperty("ACCEPTDATE")
    private String acceptDate;     // 승인일시(YYYYMMDDHHMMSS)


    /**
    *  확장 옵션: 일부 PG에서 제공
    */
    @JsonProperty("AUTHCODE")
    private String authCode;       // 카드사 승인 코드

    @JsonProperty("TRANSACTIONDATE")
    private String transactionDate; // 거래 시간

    /**
    * READY 응답 확장 필드
    */
    private String html;

    @JsonProperty("URL")
    @JsonAlias({"url", "Url"})
    private String url;
    
    @JsonProperty("REDIRECTURL")
    @JsonAlias({"redirectUrl", "REDIRECT_URL", "redirect_url"})
    private String redirectUrl;

    /** 
    * 확장 필드: PG UI Script (jQuery + cookiepay + payverse)
    */
    @JsonIgnore
    private String pgScripts;
    
    /**
    *  유틸리티 메서드 (PG 성공 여부 체크)
    */
    public boolean isSuccess() {
        return "0000".equals(this.resultCode);
    }
}
