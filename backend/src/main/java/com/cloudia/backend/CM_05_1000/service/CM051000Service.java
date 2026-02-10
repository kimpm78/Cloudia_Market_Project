package com.cloudia.backend.CM_05_1000.service;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;

import com.cloudia.backend.CM_05_1000.model.NoticeInfo;
import com.cloudia.backend.common.model.ResponseModel;

public interface CM051000Service {
  /**
   * お知らせ一覧（全件）取得
   *
   * @return お知らせ全件一覧
   */
  ResponseEntity<ResponseModel<List<NoticeInfo>>> findByAllNotice();

  /**
   * お知らせ検索取得
   *
   * @param searchKeyword キーワード
   * @param searchType    タイプ（1: タイトル+本文、2: タイトル、3: 本文）
   * @return お知らせ一覧
   */
  ResponseEntity<ResponseModel<List<NoticeInfo>>> getFindNotice(String searchKeyword, int searchType);

  /**
   * 特定のお知らせおよび前後（前/次）のお知らせ取得
   *
   * @param noticeId お知らせID
   * @return Map形式で current, prev, next のお知らせオブジェクトを返却
   */
  ResponseEntity<ResponseModel<Map<String, NoticeInfo>>> getFindIdNotice(int noticeId);

  /**
   * お知らせ閲覧数の増加（1日1回まで）
   *
   * @param noticeId  お知らせID
   * @param viewerKey ビューアーキー
   * @return 増加可否
   */
  boolean increaseViewOncePerDay(int noticeId, String viewerKey);

  /**
   * お知らせ登録
   *
   * @param entity 登録するお知らせ情報
   * @return 登録結果
   */
  ResponseEntity<ResponseModel<Integer>> noticeUpload(NoticeInfo entity);

  /**
   * お知らせ更新
   *
   * @param entity 更新するお知らせ情報
   * @return 更新結果
   */
  ResponseEntity<ResponseModel<Integer>> noticeUpdate(NoticeInfo entity);

  /**
   * お知らせ削除
   *
   * @param noticeId 削除するお知らせID
   * @return 削除処理結果
   */
  ResponseEntity<ResponseModel<Integer>> deleteNotice(Long noticeId);
}
