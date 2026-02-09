package com.cloudia.backend.constants;

public class JwtConstants {
    // JWTクレームキー（Key）一覧
    public static final String KEY_ROLES = "roles"; // 権限情報（既存の auth → roles に変更）
    public static final String KEY_USER_ID = "userId"; // ユーザーPK
    public static final String KEY_MEMBER_NO = "memberNo"; // 会員番号
    public static final String KEY_ROLE_ID = "roleId"; // 権限ID（1, 2, 3...）
    public static final String KEY_PERMISSION = "permissionLevel"; // 権限レベル
}