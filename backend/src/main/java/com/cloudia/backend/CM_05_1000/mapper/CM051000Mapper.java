package com.cloudia.backend.CM_05_1000.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.cloudia.backend.CM_05_1000.model.NoticeInfo;

@Mapper
public interface CM051000Mapper {

    /**
     * お知らせ全件取得
     *
     * @return お知らせ全件一覧
     */
    List<NoticeInfo> findByAllNotice();

    /**
     * お知らせ検索
     *
     * @param searchKeyword 検索キーワード
     * @param searchType    検索タイプ（1: タイトル+本文、2: タイトル、3: 本文）
     * @return 検索結果のお知らせ一覧
     */
    List<NoticeInfo> findByNotice(@Param("searchKeyword") String searchKeyword,
                                  @Param("searchType") int searchType);

    /**
     * お知らせ詳細取得
     *
     * @param noticeId お知らせID
     * @return 対象のお知らせ情報
     */
    List<NoticeInfo> findIdNotice(@Param("noticeId") int noticeId);

    /**
     * お知らせ単件取得
     *
     * @param noticeId お知らせID
     * @return 対象のお知らせ情報
     */
    NoticeInfo findIdNoticeOne(@Param("noticeId") int noticeId);

    /**
     * 前のお知らせ取得（現在IDより小さいIDのうち最大のもの）
     *
     * @param noticeId 基準となるお知らせID
     * @return 前のお知らせ
     */
    NoticeInfo findPrevNotice(@Param("noticeId") int noticeId);

    /**
     * 次のお知らせ取得（現在IDより大きいIDのうち最小のもの）
     *
     * @param noticeId 基準となるお知らせID
     * @return 次のお知らせ
     */
    NoticeInfo findNextNotice(@Param("noticeId") int noticeId);

    /**
     * お知らせ閲覧数の増加
     *
     * @param noticeId お知らせID
     * @return 更新件数
     */
    int incrementViewCount(@Param("noticeId") int noticeId);

    /**
     * お知らせ登録
     *
     * @param entity 登録するお知らせ情報
     * @return 登録成功: 1、失敗: 0
     */
    int noticeUpload(NoticeInfo entity);

    /**
     * お知らせ更新
     *
     * @param entity 更新するお知らせ情報
     * @return 更新成功: 1、失敗: 0
     */
    int noticeUpdate(NoticeInfo entity);

    /**
     * お知らせ削除
     *
     * @param noticeId 削除するお知らせID
     * @return 削除成功: 1、失敗: 0
     */
    int deleteNotice(@Param("noticeId") Long noticeId);
}
