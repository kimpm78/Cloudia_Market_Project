package com.cloudia.backend.CM_01_1001.model;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class User implements UserDetails {
    private Integer userId;        // ユーザーID
    private String memberNumber;   // 会員番号
    private Integer roleId;        // ユーザー権限

    @NotBlank(message = "IDを入力してください。")
    @Size(min = 6, message = "IDは6文字以上で入力してください。")
    @Pattern(
        regexp = "^(?=.*[a-zA-Z])(?=.*\\d)[a-zA-Z0-9]+$",
        message = "IDは英字と数字を必ず含み、他の文字は使用できません。"
    )
    private String loginId;        // ログインID

    @NotBlank(message = "パスワードを入力してください。")
    @Size(min = 8, message = "パスワードは8文字以上で入力してください。")
    @Pattern(
        regexp = "^(?=.*[a-zA-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]).*$",
        message = "パスワードは英字・数字・記号をすべて含める必要があります。"
    )
    private String password;       // パスワード

    @NotBlank(message = "パスワード（確認）を入力してください。")
    private String passwordConfirm;// パスワード確認
    @NotBlank(message = "メールアドレスを入力してください。")
    @Email(message = "有効なメールアドレス形式ではありません。")
    private String email;          // メールアドレス
    @NotNull(message = "氏名を入力してください。")
    private String name;           // 氏名
    private String genderType;     // 性別タイプ
    private Integer genderValue;   // 性別値
    @NotBlank(message = "生年月日は必須項目です。")
    private String birthDate;      // 生年月日
    @NotBlank(message = "国を選択してください。")
    private String nationality;    // 国籍/地域
    @NotBlank(message = "携帯電話番号を入力してください。")
    private String phoneNumber;    // 携帯電話番号
    private String postalCode;     // 郵便番号
    private String addressMain;    // 住所
    private String addressDetail1; // 詳細住所1
    private String addressDetail2; // 詳細住所2
    private String addressDetail3; // 詳細住所3

    private String refundAccountBank; // 返金口座 銀行名
    private String refundAccountNumber; // 返金口座 番号
    private String refundAccountHolder; // 返金口座 名義

    @Pattern(
        regexp = "^$|^P\\d{12}$",
        message = "個人通関固有符号は、Pで始まる13桁である必要があります。"
    )
    private String pccc;             // 個人通関固有符号 (KR向け)
    private String reasonText;       // 事由（退会・停止など）

    @NotNull(message = "利用規約および個人情報取扱方針に同意する必要があります。")
    private Boolean termsAgreed;     // 利用規約同意

    private String userStatusType;   // ユーザーステータスタイプ
    private Integer userStatusValue; // ユーザーステータス値
    private String note;             // 備考
    private String createdBy;        // 作成者
    private LocalDateTime createdAt; // 作成日時
    private String updatedBy;        // 更新者
    private LocalDateTime updatedAt; // 更新日時
    private String permissionLevel; // 権限レベル

    // Spring Security 関連
    private List<GrantedAuthority> authorities;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return this.authorities;
    }

    @Override
    public String getUsername() {
        return this.loginId;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
