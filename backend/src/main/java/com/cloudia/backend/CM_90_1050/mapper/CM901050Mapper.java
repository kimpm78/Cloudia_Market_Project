package com.cloudia.backend.CM_90_1050.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.cloudia.backend.CM_90_1050.model.ResultDto;
import com.cloudia.backend.CM_90_1050.model.SearchRequestDto;

@Mapper
public interface CM901050Mapper {
    /**
     * 売上情報一覧取得
     * 
     * @return 売上情報全件一覧
     */
    List<ResultDto> findByAllSales();

    /**
     * 条件指定売上情報一覧取得
     * 
     * @param searchRequest 検索条件
     * @return 売上情報一覧
     */
    List<ResultDto> getFindSales(SearchRequestDto searchRequest);
}
