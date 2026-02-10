package com.cloudia.backend.CM_90_1010.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.cloudia.backend.CM_90_1010.model.PreviousInfo;
import com.cloudia.backend.CM_90_1010.model.Status;
import com.cloudia.backend.CM_90_1010.model.WeeklySales;

@Mapper
public interface CM901010Mapper {
    /**
     * 現在のステータス（注文・キャンセル等）を取得
     * 
     * @return 現在のステータス一覧
     */
    Status getStatus(@Param("startDate") String startDate, @Param("EndDate") String EndDate);

    /**
     * 前日情報を取得
     * 
     * @return 前日情報
     */
    PreviousInfo getPreviousInfo(@Param("startDate") String startDate, @Param("EndDate") String EndDate);

    /**
     * 週間売上を取得
     * 
     * @return 週間売上一覧
     */
    List<WeeklySales> getWeeklySales(@Param("startDate") String startDate, @Param("EndDate") String EndDate);
}
