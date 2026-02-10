package com.cloudia.backend.CM_90_1044.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.cloudia.backend.CM_90_1044.model.NoticeInfo;

@Mapper
public interface CM901044Mapper {
    /**
     * お知らせ全件一覧取得
     * 
     * @return お知らせ全件一覧
     */
    List<NoticeInfo> findByAllNotice();

    /**
     * お知らせ検索（条件一覧取得）
     * 
     * @param searchKeyword キーワード
     * @param searchType    種別（1:タイトル＋本文、2:タイトル、3:本文）
     * @return お知らせ一覧
     */
    List<NoticeInfo> findByNotice(@Param("searchKeyword") String searchKeyword, @Param("searchType") int searchType);

    /**
     * お知らせID指定取得
     * 
     * @param noticeId お知らせID
     * @return お知らせ一覧
     */
    List<NoticeInfo> findIdNotice(@Param("noticeId") int noticeId);

    /**
     * お知らせ登録
     * 
     * @param entity 登録対象のお知らせ情報
     * @return 登録結果
     */
    int noticeUpload(NoticeInfo entity);

    /**
     * お知らせ更新
     * 
     * @param entity 更新対象のお知らせ情報
     * @return 更新結果
     */
    int noticeUpdate(NoticeInfo entity);
}
