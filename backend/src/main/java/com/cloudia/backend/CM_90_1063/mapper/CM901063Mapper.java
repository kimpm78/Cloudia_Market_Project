package com.cloudia.backend.CM_90_1063.mapper;

import java.util.List;
import java.util.Optional;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.cloudia.backend.CM_90_1063.model.ProductCode;
import com.cloudia.backend.CM_90_1063.model.Stock;
import com.cloudia.backend.CM_90_1063.model.StockDetail;
import com.cloudia.backend.CM_90_1063.model.StockInfo;

@Mapper
public interface CM901063Mapper {
    /**
     * 商品コードの全件リストを取得
     * 
     * @return 商品コードの全件リスト
     */
    List<ProductCode> findAllProductCode();

    /**
     * 商品コードで在庫情報を取得
     *
     * @param productCode 商品コード
     * 
     * @return 在庫情報（存在しない場合は空）
     **/
    Optional<Stock> findByProductCode(String productCode);

    /**
     * 在庫情報のUpsert
     *
     * @param entity 登録／更新する在庫関連情報
     * 
     * @return 実行結果
     **/
    String stockUpsert(ProductCode entity);

    /**
     * 在庫登録
     * 
     * @param entity 在庫情報
     * @return 登録件数
     */
    int insertStock(Stock entity);

    /**
     * 在庫更新
     * 
     * @param entity 在庫情報
     * @return 更新件数
     */
    int updateStock(Stock entity);

    /**
     * 在庫詳細登録
     * 
     * @param entity 在庫詳細情報
     * @return 登録件数
     */
    int insertStockDetail(StockDetail entity);

    /**
     * 入出庫一覧の全件取得
     * 
     * @return 入出庫一覧の全件リスト
     */
    List<StockInfo> findAllStocks();

    /**
     * 指定した商品コード／商品名で入出庫一覧を検索
     * 
     * @param searchType 検索タイプ（1: 商品コード、2: 商品名）
     * @param searchTerm 検索キーワード
     * @return 入出庫一覧
     */
    List<StockInfo> findByStocks(@Param("searchType") String searchType, @Param("searchTerm") String searchTerm);

}
