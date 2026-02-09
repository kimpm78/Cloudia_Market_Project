package com.cloudia.backend.CM_90_1040.service;

import java.util.List;

import org.springframework.http.ResponseEntity;

import com.cloudia.backend.CM_90_1040.model.BannerInfo;
import com.cloudia.backend.CM_90_1040.model.ResponseModel;

public interface CM901040Service {
    /**
     * 배너 등록
     * 
     * @param entity 등록 할 배너 정보
     * @return 등록 여부
     */
    ResponseEntity<ResponseModel<Integer>> bannerUpload(BannerInfo entity, String userId);

    /**
     * 배너 업데이트
     * 
     * @param entity 업데이트 할 배너 정보
     * @return 업데이트 여부
     */
    ResponseEntity<ResponseModel<Integer>> bannerUpdate(BannerInfo entity, String userId);

    /**
     * 배너 삭제
     * 
     * @param entity 배너 삭제 항목 리스트
     * @return 삭제 여부
     */
    ResponseEntity<ResponseModel<Integer>> bannerDel(List<BannerInfo> entity);

    /**
     * 배너 전체 리스트 조회
     * 
     * @return 배너 전체 리스트
     */
    ResponseEntity<ResponseModel<List<BannerInfo>>> findByAllBanner();

    /**
     * 특정 배너 리스트 조회
     * 
     * @param searchTerm 배너명
     * @return 배너 리스트
     */
    ResponseEntity<ResponseModel<List<BannerInfo>>> findByBanner(String searchTerm);

    /**
     * 업데이트 배너 리스트 조회
     * 
     * @param bannerId 배너 아이디
     * @return 배너 리스트
     */
    ResponseEntity<ResponseModel<BannerInfo>> findByBanner(int bannerId);

    /**
     * 사용 가능한 디스플레이 번호 리스트 조회
     * 
     * @return 디스플레이 번호 리스트
     */
    ResponseEntity<ResponseModel<List<Integer>>> getFindDisplayOrder();

}
