package com.cloudia.backend.CM_90_1040.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.cloudia.backend.CM_90_1040.model.BannerInfo;

@Mapper
public interface CM901040Mapper {
    /**
     * バナー全件一覧取得
     * 
     * @return バナー全件一覧
     */
    List<BannerInfo> findByAllBanner();

    /**
     * 特定バナー一覧取得
     * 
     * @param searchTerm バナー名
     * @return バナー一覧
     */
    List<BannerInfo> findByBanner(@Param("searchTerm") String searchTerm);

    /**
     * 更新対象バナー取得
     * 
     * @param bannerId バナーID
     * @return バナー情報
     */
    BannerInfo findByBannerById(@Param("bannerId") int bannerId);

    /**
     * バナー削除
     * 
     * @param bannerId バナーID
     * @return 削除結果
     */
    int bannerDel(@Param("bannerId") int bannerId);

    /**
     * 有効バナー件数取得
     * 
     * @return 有効バナー件数
     */
    int findByUsedAllBanner();

    /**
     * 表示順の重複件数取得
     * 
     * @return 重複件数
     */
    int countByDisplayOrder(@Param("displayOrderId") int displayOrderId);

    /**
     * バナー登録
     * 
     * @param entity 登録するバナー情報
     * @return 登録結果
     */
    int bannerUpload(BannerInfo entity);

    /**
     * バナー更新
     * 
     * @param entity 更新するバナー情報
     * @return 更新結果
     */
    int bannerUpdate(BannerInfo entity);

    /**
     * 使用可能な表示順一覧取得
     * 
     * @return 表示順一覧
     */
    List<Integer> getFindDisplayOrder();

}
