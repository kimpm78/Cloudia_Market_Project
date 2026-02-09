package com.cloudia.backend.CM_04_1003.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.cloudia.backend.CM_04_1003.model.QnaCreateRequest;
import com.cloudia.backend.CM_04_1003.model.QnaDetail;
import com.cloudia.backend.CM_04_1003.model.QnaSummary;

@Mapper
public interface CM041003Mapper {

    long countQnaList(Map<String, Object> params);

    List<QnaSummary> selectQnaList(Map<String, Object> params);

    QnaDetail selectQnaDetail(@Param("qnaId") Long qnaId);

    QnaSummary selectPrevQna(@Param("qnaId") Long qnaId);

    QnaSummary selectNextQna(@Param("qnaId") Long qnaId);

    int insertQna(QnaCreateRequest request);

    String findLoginIdByMemberNumber(@Param("memberNumber") String memberNumber);

    int insertQnaAnswer(@Param("qnaId") Long qnaId,
                        @Param("answerContent") String answerContent,
                        @Param("answererId") Long answererId,
                        @Param("answererLoginId") String answererLoginId);

    int updateInquiryStatusToAnswered(@Param("qnaId") Long qnaId,
                                      @Param("updatedBy") String updatedBy);

    List<QnaSummary> selectRecentQna(Map<String, Object> params);

    int deleteQnaAnswers(@Param("qnaId") Long qnaId);

    int deleteQna(@Param("qnaId") Long qnaId);
}
