package raisetech.StudentManagementSystem.data;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 受講生（1人分の基本情報）を表すクラスです。
 * データベースのstudentsテーブルの1行に対応します。
 * 各項目に付けた@NotBlank・@Sizeなどは、登録・更新時の入力チェックのルールです
 * （Controllerの@Validで、ルール違反のリクエストを受け付けないようにしています）。
 */
// @Schema：Swagger UI（API仕様書の画面）に表示する、このクラスの説明（処理の動きには影響しない）
@Schema(description = "受講生情報")
// @Data：Lombokが、getter・setter・toString・equals・hashCodeを自動で作ってくれる
//（getName()やsetEmail(...)などを自分で書かなくてよい）
@Data
// @JsonPropertyOrder：JSONにしてレスポンスで返すときの、項目の並び順を指定する
//（id → name → kanaName → ... → isDeleted の順で出力される）
@JsonPropertyOrder({"id", "name", "kanaName", "nickname", "email", "area", "age", "sex", "remark", "isDeleted"})
public class Student {

  // 受講生を区別するためのID（データベースが自動で採番する。id列）
  @Schema(description = "受講生ID（データベースが自動採番）", example = "1")
  private Integer id;

  // 受講生の名前（name列）
  // @NotBlank：空・空白だけは不可（必須）／ @Size(max = 50)：50文字以内
  @Schema(description = "氏名", example = "山田 太郎")
  @NotBlank
  @Size(max = 50)
  private String name;

  // 名前のふりがな（kana_name列）
  // 必須・50文字以内。@Pattern：全角カタカナ（ァ〜ヶ）・長音「ー」・全角/半角スペースだけを許可する
  @Schema(description = "ふりがな（全角カタカナ）", example = "ヤマダ タロウ")
  @NotBlank
  @Size(max = 50)
  @Pattern(regexp = "^[ァ-ヶー　 ]+$")
  private String kanaName;

  // ニックネーム（nickname列）。必須ではないが、入力するなら50文字以内
  @Schema(description = "ニックネーム", example = "タロウ")
  @Size(max = 50)
  private String nickname;

  // メールアドレス（email列）
  // 必須・50文字以内。@Email：メールアドレスの形式（xxx@yyy）かどうかを確認する
  @Schema(description = "メールアドレス（他の受講生との重複不可）", example = "taro@example.com")
  @NotBlank
  @Size(max = 50)
  @Email
  private String email;

  // お住まいの地域（area列）。必須ではないが、入力するなら50文字以内
  @Schema(description = "お住まいの地域", example = "兵庫県")
  @Size(max = 50)
  private String area;

  // 年齢（age列）。@Min(0)・@Max(150)：0以上150以下の範囲だけ許可する
  @Schema(description = "年齢（0〜150）", example = "30")
  @Min(0)
  @Max(150)
  private Integer age;

  // 性別（sex列）。必須ではないが、入力するなら10文字以内
  @Schema(description = "性別", example = "男性")
  @Size(max = 10)
  private String sex;

  // 備考（自由記入欄。remark列）。必須ではないが、入力するなら255文字以内
  @Schema(description = "備考", example = "特になし")
  @Size(max = 255)
  private String remark;

  // 削除フラグ（true＝削除済み、false＝未削除。is_deleted列）
  // データを消さずに「削除した扱い」にする論理削除に使う。新規登録時はSQLでfalseが入る
  @Schema(description = "削除フラグ（true＝削除済み、false＝未削除）", example = "false")
  private Boolean isDeleted;
}
