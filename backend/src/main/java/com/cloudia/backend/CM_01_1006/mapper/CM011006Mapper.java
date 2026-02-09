package com.cloudia.backend.CM_01_1006.mapper;

import com.cloudia.backend.CM_01_1006.model.InquiryEntity;
import com.cloudia.backend.CM_01_1006.model.InquiryProductDTO;
import com.cloudia.backend.CM_01_1006.model.InquiryResponseDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface CM011006Mapper {
    /**
     * 問い合わせ一覧取得（会員番号基準）
     */
    List<InquiryResponseDTO> findByMemberNumber(
            @Param("memberNumber") String memberNumber
    );

    /**
     * 問い合わせ詳細取得
     */
    InquiryResponseDTO findDetailByInquiryId(
            @Param("inquiryId") Long inquiryId
    );

    /**
     * 前の問い合わせ取得
     */
    InquiryResponseDTO findPrevInquiry(
            @Param("inquiryId") Long inquiryId,
            @Param("memberNumber") String memberNumber
    );

    /**
     * 次の問い合わせ取得
     */
    InquiryResponseDTO findNextInquiry(
            @Param("inquiryId") Long inquiryId,
            @Param("memberNumber") String memberNumber
    );

    /**
     * 問い合わせ登録
     */
    int insertInquiry(
            InquiryEntity entity
    );

    /**
     * 商品一覧取得（問い合わせ作成用）
     */
    List<InquiryProductDTO> getProductList();

    /**
     * 問い合わせ回答登録（管理者）
     */
    int insertInquiryAnswer(
            @Param("inquiryId") Long inquiryId,
            @Param("answerContent") String answerContent,
            @Param("answererId") Long answererId,
            @Param("answererLoginId") String answererLoginId
    );

    /**
     * 回答登録時にステータスを「回答済み」に更新
     */
    int updateInquiryStatusToAnswered(
            @Param("inquiryId") Long inquiryId,
            @Param("updatedBy") String updatedBy
    );

    /**
     * 問い合わせ削除
     */
    int deleteInquiryById(
            @Param("inquiryId") Long inquiryId
    );
}