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
     * PG応答の共通項目
     */
    @JsonProperty("RESULTCODE")
    @JsonAlias({"RTN_CD", "rtn_cd"})
    private String resultCode;     // PG結果コード

    @JsonProperty("RESULTMSG")
    @JsonAlias({"RTN_MSG", "rtn_msg"})
    private String resultMsg;      // PG結果メッセージ

    @JsonProperty("TID")
    @JsonAlias({"tid", "Tid", "transactionId", "transaction_id"})
    private String tid;            // 取引ID

    @JsonProperty("ORDERNO")
    private String orderNo;        // 注文番号

    @JsonProperty("AMOUNT")
    private String amount;         // 決済金額

    @JsonProperty("PAYMETHOD")
    private String payMethod;      // 決済手段

    @JsonProperty("ACCEPTNO")
    private String acceptNo;       // 承認番号

    @JsonProperty("ACCEPTDATE")
    private String acceptDate;     // 承認日時（YYYYMMDDHHMMSS）


    /**
     * 拡張オプション（一部PGで提供）
     */
    @JsonProperty("AUTHCODE")
    private String authCode;       // カード会社承認コード

    @JsonProperty("TRANSACTIONDATE")
    private String transactionDate; // 取引時間

    /**
     * READY応答の拡張フィールド
     */
    private String html;

    @JsonProperty("URL")
    @JsonAlias({"url", "Url"})
    private String url;
    
    @JsonProperty("REDIRECTURL")
    @JsonAlias({"redirectUrl", "REDIRECT_URL", "redirect_url"})
    private String redirectUrl;

    /**
     * 拡張フィールド：PG UI Script（jQuery + cookiepay + payverse）
     */
    @JsonIgnore
    private String pgScripts;
    
    /**
     * ユーティリティメソッド（PG成功可否の判定）
     */
    public boolean isSuccess() {
        return "0000".equals(this.resultCode);
    }
}
