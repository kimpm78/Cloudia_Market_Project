package com.cloudia.backend.CM_90_1044.service;

import java.util.List;

import org.springframework.http.ResponseEntity;

import com.cloudia.backend.common.model.ResponseModel;
import com.cloudia.backend.CM_90_1044.model.NoticeInfo;

public interface CM901044Service {
    /**
     * お知らせ全件一覧取得
     * 
     * @return お知らせ全件一覧
     */
    ResponseEntity<ResponseModel<List<NoticeInfo>>> findByAllNotice();

    /**
     * お知らせ検索（条件一覧取得）
     * 
     * @param searchKeyword キーワード
     * @param searchType    種別（1:タイトル＋本文、2:タイトル、3:本文）
     * @return お知らせ一覧
     */
    ResponseEntity<ResponseModel<List<NoticeInfo>>> getFindNotice(String searchKeyword, int searchType);

    /**
     * お知らせID指定取得
     * 
     * @param noticeId お知らせID
     * @return お知らせ一覧
     */
    ResponseEntity<ResponseModel<List<NoticeInfo>>> getFindIdNotice(int noticeId);

    /**
     * お知らせ登録
     * 
     * @param entity 登録対象のお知らせ情報
     * @return 登録結果
     */
    ResponseEntity<ResponseModel<Integer>> noticeUpload(NoticeInfo entity, String userId);

    /**
     * お知らせ更新
     * 
     * @param entity 更新対象のお知らせ情報
     * @return 更新結果
     */
    ResponseEntity<ResponseModel<Integer>> noticeUpdate(NoticeInfo entity, String userId);
}
