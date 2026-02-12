package com.cloudia.backend.CM_01_1006.constants;

public final class CM011006MessageConstant {

    private CM011006MessageConstant() {
    }

    public static final String LOG_INQUIRY_LIST_START = "1:1問い合わせ一覧取得リクエスト。loginId: {}";
    public static final String LOG_PRODUCT_LIST_ERROR = "商品一覧取得に失敗";
    public static final String LOG_INQUIRY_LIST_ERROR = "1:1問い合わせ一覧取得中にエラーが発生。loginId: {}";
    public static final String LOG_INQUIRY_CREATE_START = "1:1問い合わせ登録リクエスト。loginId: {}";
    public static final String LOG_INQUIRY_CREATE_ERROR = "1:1問い合わせ登録中にエラーが発生。loginId: {}";
    public static final String LOG_VALIDATION_FAIL = "問い合わせ登録の入力値検証に失敗: {}";
    public static final String LOG_INQUIRY_DETAIL_ERROR = "1:1問い合わせ詳細取得中にエラーが発生。inquiryId: {}";
    public static final String LOG_INQUIRY_DETAIL_FORBIDDEN = "非公開問い合わせへのアクセスを遮断。inquiryId: {}, requesterId: {}";
    public static final String LOG_ANSWER_REG_ERROR = "回答登録中にエラーが発生。inquiryId: {}";
    public static final String LOG_ANSWER_STATUS_FAIL = "回答登録後のステータス更新に失敗。inquiryId: {}";
    public static final String LOG_ANSWER_VALIDATION_FAIL = "回答登録の入力値検証に失敗: {}";
    public static final String LOG_DELETE_START = "1:1問い合わせ削除リクエスト。inquiryId: {}, requesterId: {}";
    public static final String LOG_DELETE_FORBIDDEN = "問い合わせ削除権限なし（作成者不一致）。inquiryId: {}, requesterId: {}";
    public static final String LOG_DELETE_FAIL_ANSWERED = "問い合わせ削除不可（回答済み）。inquiryId: {}, status: {}";
    public static final String LOG_DELETE_ERROR = "1:1問い合わせ削除中にDBエラーが発生。inquiryId: {}";
    public static final String LOG_DELETE_UNKNOWN_ERROR = "問い合わせ削除中に不明なエラー";
    public static final String MSG_SERVICE_ERROR = "サービスエラー";
    public static final String MSG_VALIDATION_ERROR = "入力値が正しくありません。";
    public static final String MSG_CREATE_SUCCESS = "問い合わせが正常に登録されました。";
    public static final String MSG_CREATE_ERROR = "問い合わせ登録中にエラーが発生しました。";
    public static final String MSG_USER_NOT_FOUND = "ユーザー情報が見つかりません。";
    public static final String MSG_ANSWER_EMPTY = "回答内容を入力してください。";
    public static final String MSG_ANSWER_ERROR = "回答登録中にエラーが発生しました。";
    public static final String MSG_DELETE_SUCCESS = "問い合わせを削除しました。";
    public static final String MSG_DELETE_FAIL_ANSWERED = "回答済みの問い合わせは削除できません。";
    public static final String MSG_DELETE_ERROR = "問い合わせ削除中に不明なエラーが発生しました。";
    public static final String MSG_NOT_FOUND = "該当の問い合わせが見つかりません。";
}