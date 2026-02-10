package com.cloudia.backend.CM_90_1040.service;

import java.util.List;

import org.springframework.http.ResponseEntity;

import com.cloudia.backend.CM_90_1040.model.BannerInfo;
import com.cloudia.backend.common.model.ResponseModel;

public interface CM901040Service {
    /**
     * バナー登録
     * 
     * @param entity 登録するバナー情報
     * @return 登録結果
     */
    ResponseEntity<ResponseModel<Integer>> bannerUpload(BannerInfo entity, String userId);

    /**
     * バナー更新
     * 
     * @param entity 更新するバナー情報
     * @return 更新結果
     */
    ResponseEntity<ResponseModel<Integer>> bannerUpdate(BannerInfo entity, String userId);

    /**
     * バナー削除
     * 
     * @param entity 削除対象バナーリスト
     * @return 削除結果
     */
    ResponseEntity<ResponseModel<Integer>> bannerDel(List<BannerInfo> entity);

    /**
     * バナー全件一覧取得
     * 
     * @return バナー全件一覧
     */
    ResponseEntity<ResponseModel<List<BannerInfo>>> findByAllBanner();

    /**
     * 特定バナー一覧取得
     * 
     * @param searchTerm バナー名
     * @return バナー一覧
     */
    ResponseEntity<ResponseModel<List<BannerInfo>>> findByBanner(String searchTerm);

    /**
     * 更新対象バナー取得
     * 
     * @param bannerId バナーID
     * @return バナー情報
     */
    ResponseEntity<ResponseModel<BannerInfo>> findByBanner(int bannerId);

    /**
     * 使用可能な表示順一覧取得
     * 
     * @return 表示順一覧
     */
    ResponseEntity<ResponseModel<List<Integer>>> getFindDisplayOrder();

}
