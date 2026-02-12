package com.cloudia.backend.common.service;

import com.cloudia.backend.common.model.ResponseModel;
import com.cloudia.backend.common.model.pg.PGApproveRequest;
import com.cloudia.backend.common.model.pg.PGCancelRequest;
import com.cloudia.backend.common.model.pg.PGDecryptRequest;
import com.cloudia.backend.common.model.pg.PGFailRequest;
import com.cloudia.backend.common.model.pg.PGReadyRequest;

import java.util.Map;

/**
 * 決済サービス（PG連携の共通処理）
 * 各PGProvider（CookiePay 等）の実装は PGProviderRegistry により選択される
 */
public interface PaymentService {

    /**
     * PG決済リクエスト（決済画面へ遷移）
     *
     * @param request PG決済準備リクエスト情報
     * @return ResponseModel（PG ready 結果 + redirect/form 構成データ）
     */
    ResponseModel<Map<String, Object>> ready(PGReadyRequest request);


    /**
     * 決済承認／検証ステップ
     *
     * @param request PG承認リクエスト情報
     * @return ResponseModel（決済承認結果）
     */
    ResponseModel<Map<String, Object>> approve(PGApproveRequest request);


    /**
     * 決済キャンセル処理
     *
     * @param request キャンセルリクエスト情報
     * @return ResponseModel（キャンセル結果）
     */
    ResponseModel<Map<String, Object>> cancel(PGCancelRequest request);

    /**
     * 決済失敗（またはユーザーが画面を閉じた場合）の処理
     *
     * @param request 失敗処理リクエスト情報
     * @return ResponseModel（失敗処理結果）
     */
    ResponseModel<Map<String, Object>> fail(PGFailRequest request);


    /**
     * PG CALLBACK（RETURN URL）処理
     *
     * @param queryParams PGから渡されるパラメータ（encData, orderNo など）
     * @return ResponseModel（callback 処理結果）
     */
    ResponseModel<Map<String, Object>> callback(Map<String, String> queryParams);

    /**
     * PG ENC_DATA の復号
     *
     * @param request 復号リクエスト情報
     * @return ResponseModel（復号結果）
     */
    ResponseModel<Map<String, Object>> decrypt(PGDecryptRequest request);
}
