package com.cloudia.backend.CM_90_1050.service;

import java.util.List;

import com.cloudia.backend.CM_90_1050.model.ResultDto;
import com.cloudia.backend.CM_90_1050.model.SearchRequestDto;

public interface CM901050Service {
    /**
     * 売上情報一覧取得
     * 
     * @return 売上情報全件一覧
     */
    List<ResultDto> findByAllSales();

    /**
     * 条件指定売上情報一覧取得
     * 
     * @return 売上情報一覧
     */
    List<ResultDto> getFindSales(SearchRequestDto searchRequest);
}
