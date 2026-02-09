package com.cloudia.backend.CM_90_1040.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.cloudia.backend.CM_90_1040.model.BannerInfo;

@Mapper
public interface CM901040Mapper {
    /**
     * 배너 전체 리스트 조회
     * 
     * @return 배너 전체 리스트
     */
    List<BannerInfo> findByAllBanner();

    /**
     * 특정 배너 리스트 조회
     * 
     * @param searchTerm 배너명
     * @return 배너 리스트
     */
    List<BannerInfo> findByBanner(@Param("searchTerm") String searchTerm);

    /**
     * 업데이트 배너 리스트 조회
     * 
     * @param bannerId 배너 아이디
     * @return 배너 리스트
     */
    BannerInfo findByBannerById(@Param("bannerId") int bannerId);

    /**
     * 배너 삭제
     * 
     * @param entity 배너 삭제 항목 리스트
     * @return 삭제 여부
     */
    int bannerDel(@Param("bannerId") int bannerId);

    /**
     * 사용중인 배너 리스트 조회
     * 
     * @return 사용중인 배너 리스트 카운트
     */
    int findByUsedAllBanner();

    /**
     * 사용중인 배너 리스트 조회
     * 
     * @return 사용중인 배너 리스트 카운트
     */
    int countByDisplayOrder(@Param("displayOrderId") int displayOrderId);

    /**
     * 배너 등록
     * 
     * @param entity 등록 할 배너 정보
     * @return 등록 여부
     */
    int bannerUpload(BannerInfo entity);

    /**
     * 배너 업데이트
     * 
     * @param entity 업데이트 할 배너 정보
     * @return 업데이트 여부
     */
    int bannerUpdate(BannerInfo entity);

    /**
     * 사용 가능한 디스플레이 번호 리스트 조회
     * 
     * @return 디스플레이 번호 리스트
     */
    List<Integer> getFindDisplayOrder();

}
