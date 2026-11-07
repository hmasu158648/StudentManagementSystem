package raisetech.StudentManagementSystem.data;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;

/**
 * 受講生（1人分の基本情報）を表すクラスです。
 * データベースのstudentsテーブルの1行に対応します。
 */
// @Data：Lombokが、getter・setter・toString・equals・hashCodeを自動で作ってくれる
//（getName()やsetEmail(...)などを自分で書かなくてよい）
@Data
// @JsonPropertyOrder：JSONにしてレスポンスで返すときの、項目の並び順を指定する
//（id → name → kanaName → ... → isDeleted の順で出力される）
@JsonPropertyOrder({"id", "name", "kanaName", "nickname", "email", "area", "age", "sex", "remark", "isDeleted"})
public class Student {

  // 受講生を区別するためのID（データベースが自動で採番する。id列）
  private Integer id;
  // 受講生の名前（name列）
  private String name;
  // 名前のふりがな（kana_name列）
  private String kanaName;
  // ニックネーム（nickname列）
  private String nickname;
  // メールアドレス（email列）
  private String email;
  // お住まいの地域（area列）
  private String area;
  // 年齢（age列）
  private Integer age;
  // 性別（sex列）
  private String sex;
  // 備考（自由記入欄。remark列）
  private String remark;
  // 削除フラグ（true＝削除済み、false＝未削除。is_deleted列）
  // データを消さずに「削除した扱い」にする論理削除に使う。新規登録時はSQLでfalseが入る
  private Boolean isDeleted;
}
