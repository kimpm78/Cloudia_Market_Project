package com.cloudia.backend.CM_04_1003.constants;

public final class CM041003MessageConstant {
    private CM041003MessageConstant() {
    }
    public static final String QNA_LIST_SUCCESS = "Q&A一覧を取得しました。";
    public static final String QNA_LIST_FAIL = "Q&A一覧を取得できませんでした。";
    public static final String QNA_DETAIL_SUCCESS = "Q&A詳細を取得しました。";
    public static final String QNA_DETAIL_FAIL = "Q&A詳細を取得できませんでした。";
    public static final String QNA_CREATE_SUCCESS = "Q&Aを登録しました。";
    public static final String QNA_CREATE_FAIL = "Q&Aの登録に失敗しました。";
    public static final String QNA_CREATE_FORBIDDEN = "管理者・マネージャーはQ&Aを登録できません。";
    public static final String QNA_ANSWER_SUCCESS = "Q&A回答を登録しました。";
    public static final String QNA_ANSWER_FAIL = "Q&A回答の登録に失敗しました。";
    public static final String QNA_ANSWER_FORBIDDEN = "管理者のみ回答を登録できます。";
    public static final String QNA_DELETE_SUCCESS = "Q&Aを削除しました。";
    public static final String QNA_DELETE_FAIL = "Q&Aの削除に失敗しました。";
    public static final String QNA_PRIVATE_FORBIDDEN = "非公開Q&Aは作成者と管理者のみ閲覧できます。";
    public static final String QNA_DELETE_FORBIDDEN = "作成者または管理者のみ削除できます。";
    public static final String QNA_NOT_FOUND = "指定されたQ&Aが見つかりません。";
    public static final String USER_NOT_FOUND = "ユーザー情報が見つかりません。";
    public static final String INVALID_PAGING = "ページ情報が正しくありません。";
    public static final String INVALID_REQUEST = "リクエスト情報が正しくありません。";

    // ログメッセージ
    public static final String LOG_INVALID_PAGING = "不正なページ要求 page={}, size={}";
    public static final String LOG_QNA_LIST_DB_ERROR = "Q&A一覧取得中にDBエラーが発生しました";
    public static final String LOG_QNA_LIST_UNEXPECTED_ERROR = "Q&A一覧取得中に予期しないエラーが発生しました";

    public static final String LOG_INVALID_QNA_ID_REQUEST = "不正なQ&A ID要求: {}";
    public static final String LOG_QNA_NOT_FOUND = "Q&Aが見つかりません。qnaId={}";
    public static final String LOG_QNA_PRIVATE_ACCESS_DENIED = "非公開Q&Aへのアクセスを拒否しました qnaId={}, requesterId={}";
    public static final String LOG_QNA_DETAIL_DB_ERROR = "Q&A詳細取得中にDBエラーが発生しました";
    public static final String LOG_QNA_DETAIL_UNEXPECTED_ERROR = "Q&A詳細取得中に予期しないエラーが発生しました";

    public static final String LOG_QNA_CREATE_REQUEST_NULL = "Q&A登録リクエストがnullです。";
    public static final String LOG_USER_NOT_FOUND_BY_MEMBER_NUMBER = "ユーザー情報が見つかりません。memberNumber={}";
    public static final String LOG_QNA_CREATE_DB_ERROR = "Q&A登録中にDBエラーが発生しました";
    public static final String LOG_QNA_CREATE_UNEXPECTED_ERROR = "Q&A登録中に予期しないエラーが発生しました";

    public static final String LOG_QNA_ANSWER_CONTENT_EMPTY = "回答内容が空です。qnaId={}";
    public static final String LOG_QNA_ANSWERER_MISSING = "回答者情報が不足しています。qnaId={}";
    public static final String LOG_QNA_ANSWER_INSERT_FAIL = "Q&A回答の登録に失敗しました。qnaId={}";
    public static final String LOG_QNA_STATUS_UPDATE_FAIL = "Q&Aステータス更新に失敗しました qnaId={}";
    public static final String LOG_QNA_ANSWER_DB_ERROR = "Q&A回答登録中にDBエラーが発生しました";
    public static final String LOG_QNA_ANSWER_UNEXPECTED_ERROR = "Q&A回答登録中に予期しないエラーが発生しました";

    public static final String LOG_RECENT_QNA_DB_ERROR = "最新Q&A取得中にDBエラーが発生しました";
    public static final String LOG_RECENT_QNA_UNEXPECTED_ERROR = "最新Q&A取得中に予期しないエラーが発生しました";

    public static final String LOG_QNA_DELETE_DB_ERROR = "Q&A削除中にDBエラーが発生しました";
    public static final String LOG_QNA_DELETE_UNEXPECTED_ERROR = "Q&A削除中に予期しないエラーが発生しました";
}
