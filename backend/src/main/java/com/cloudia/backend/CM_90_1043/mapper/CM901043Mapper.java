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
     * 카테고리 그룹 전체 리스트 조회
     * 
     * @return 카테고리 그룹 전체 리스트
     */
    List<CategoryGroupDTO> selectCategoryGroups();

    /**
     * 카테고리 자식 전체 리스트 조회
     * 
     * @return 카테고리 자식 전체 리스트
     */
    List<CategoryDetailDTO> selectCategoryDetails();

    /**
     * 그룹 생성
     */
    int insertGroup(CategoryGroupDTO group);

    /**
     * 그룹 삭제
     */
    int deleteGroup(String categoryGroupCode);

    /**
     * 카테고리 생성
     */
    int insertCategory(CategoryDetailDTO category);

    /**
     * 카테고리 수정
     */
    int updateCategory(CategoryDetailDTO category);

    /**
     * 카테고리 삭제
     */
    int deleteCategory(CategoryDetailDTO category);

    /**
     * 카테고리 테이블의 최신 수정 시간 조회
     */
    LocalDateTime selectMaxUpdatedAt();

    /**
     * 특정 그룹의 다음 카테고리 코드 조회
     */
    String selectNextCategoryCode(String categoryGroupCode);

    /**
     * 상품 카테고리 조회
     */
    List<ProductDTO> selectByGroupAndCode(CategoryDetailDTO category);
}
