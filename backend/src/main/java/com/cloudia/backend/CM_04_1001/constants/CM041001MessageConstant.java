package com.cloudia.backend.CM_04_1001.constants;

public class CM041001MessageConstant {
    private CM041001MessageConstant() {
    }

    // 登録
    public static final String COMMENT_CREATE_SUCCESS = "コメントが正常に登録されました。";
    public static final String COMMENT_CREATE_FAIL = "コメント登録中にエラーが発生しました。";

    // 修正
    public static final String COMMENT_UPDATE_SUCCESS = "コメントが正常に修正されました。";
    public static final String COMMENT_UPDATE_FAIL = "コメント修正中にエラーが発生しました。";

    // 削除
    public static final String COMMENT_DELETE_SUCCESS = "コメントが正常に削除されました。";
    public static final String COMMENT_DELETE_FAIL = "コメント削除中にエラーが発生しました。";

    // 取得
    public static final String COMMENT_FETCH_SUCCESS = "コメント一覧の取得に成功しました";
    public static final String COMMENT_FETCH_EMPTY = "登録されたコメントがありません。";

    // 返信コメント / ツリー構造
    public static final String COMMENT_TREE_FETCH_SUCCESS = "コメントツリーの取得に成功しました";
    public static final String COMMENT_TREE_EMPTY = "登録されたコメント/返信コメントがありません。";
    public static final String REPLY_CREATE_SUCCESS = "返信コメントが正常に登録されました。";
    public static final String REPLY_CREATE_FAIL = "返信コメント登録中にエラーが発生しました。";

    // 親コメントの有効性
    public static final String COMMENT_PARENT_NOT_FOUND = "親コメントが見つかりません。";
    public static final String COMMENT_SELF_REPLY_FORBIDDEN = "自分のコメントには返信できません。";

    // コメントのバリデーション/例外
    public static final String COMMENT_VALIDATION_FAIL = "コメントのバリデーションに失敗しました: {}";
    public static final String COMMENT_DB_ERROR = "コメント処理中にDBエラーが発生しました: {}";
    public static final String COMMENT_UNEXPECTED_ERROR = "コメント処理中に予期しないエラーが発生しました: {}";

    // 認証（権限）
    public static final String AUTH_REQUIRED = "ログインが必要です。";
    public static final String AUTH_FORBIDDEN = "権限がありません。";

    // ログ
    public static final String COMMENT_SOFT_DELETE_CALLED = "softDeleteComment 呼び出し: reviewId={}, reviewCommentId={}, userId={}";
}