package com.cloudia.backend.CM_90_1010.service;

import java.util.List;

import com.cloudia.backend.CM_90_1010.model.PreviousInfo;
import com.cloudia.backend.CM_90_1010.model.Status;
import com.cloudia.backend.CM_90_1010.model.WeeklySales;

public interface CM901010Service {
    /**
     * 現在のステータス（注文・キャンセル等）を取得
     * 
     * @return 現在のステータス
     */
    Status getStatus();

    /**
     * 前日情報を取得
     * 
     * @return 前日情報
     */
    PreviousInfo getPreviousInfo();

    /**
     * 週間売上を取得
     * 
     * @return 週間売上一覧
     */
    List<WeeklySales> getWeeklySales();
}