package com.cloudia.backend.CM_90_1043.mapper;

import java.time.LocalDateTime;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.cloudia.backend.CM_90_1043.model.CategoryDetailDTO;
import com.cloudia.backend.CM_90_1043.model.CategoryGroupDTO;
import com.cloudia.backend.CM_90_1043.model.ProductDTO;

@Mapper
public interface CM901043Mapper {
    /**
     * カテゴリグループ全件一覧取得
     * 
     * @return カテゴリグループ一覧
     */
    List<CategoryGroupDTO> selectCategoryGroups();

    /**
     * 子カテゴリ全件一覧取得
     * 
     * @return 子カテゴリ一覧
     */
    List<CategoryDetailDTO> selectCategoryDetails();

    /**
     * グループ作成
     */
    int insertGroup(CategoryGroupDTO group);

    /**
     * グループ削除
     */
    int deleteGroup(String categoryGroupCode);

    /**
     * カテゴリ作成
     */
    int insertCategory(CategoryDetailDTO category);

    /**
     * カテゴリ更新
     */
    int updateCategory(CategoryDetailDTO category);

    /**
     * カテゴリ削除
     */
    int deleteCategory(CategoryDetailDTO category);

    /**
     * カテゴリテーブルの最終更新日時取得
     */
    LocalDateTime selectMaxUpdatedAt();

    /**
     * 指定グループの次カテゴリコード取得
     */
    String selectNextCategoryCode(String categoryGroupCode);

    /**
     * 商品カテゴリ照会
     */
    List<ProductDTO> selectByGroupAndCode(CategoryDetailDTO category);
}
