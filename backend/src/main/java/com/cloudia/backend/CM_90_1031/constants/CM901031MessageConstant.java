package com.cloudia.backend.CM_90_1031.constants;

public class CM901031MessageConstant {
    private CM901031MessageConstant() {
    }
    // 年齢層取得関連
    public static final String USER_FIND_ALL_START = "ユーザー全件一覧取得開始";
    public static final String USER_FIND_ALL_COMPLETE = "ユーザー全件一覧取得完了、取得件数: {}";
    public static final String SUCCESS_USER_FIND = "ユーザー取得成功";
    public static final String USER_FIND_ALL_DB_ERROR = "ユーザー全件一覧取得中にDBエラーが発生しました: {}";
    public static final String USER_FIND_ALL_NULL_ERROR = "ユーザー全件一覧取得中にNullPointerExceptionが発生しました: {}";
    public static final String USER_FIND_ALL_UNEXPECTED_ERROR = "ユーザー全件一覧取得中に予期しないエラーが発生しました: {}";
}
