package raisetech.StudentManagementSystem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 受講生管理システムを起動するためのメインクラスです。
 * このクラスのmainメソッドを実行すると、Spring Bootアプリケーションが立ち上がります。
 *
 * <p>【全体の構成（リクエストが処理される流れ）】
 * <pre>
 * クライアント(ブラウザ・Postmanなど)
 *   → StudentController  … リクエストを受け付け、入力チェックをして、結果をJSONで返す（controllerパッケージ）
 *   → StudentService     … 業務処理（検索・登録・更新）を行う（serviceパッケージ）
 *   → StudentRepository  … データベースへのSQLを実行する。SQL本体は resources/mapper/studentRepository.xml（repositoryパッケージ）
 *   → MySQL(student_management)
 * 補助的なクラス：
 *   StudentConverter … 受講生とコース情報をStudentDetailにまとめる（controller/converterパッケージ）
 *   Student / StudentCourse … テーブルの1行に対応するデータ（dataパッケージ）
 *   StudentDetail … 受講生とそのコース一覧を1セットにしたデータ（domainパッケージ）
 * </pre>
 */
// @SpringBootApplication：Spring Bootアプリの起動に必要な設定をまとめて有効にする目印
// （このパッケージ以下の@Controller・@Service・@Componentなどを自動で探して登録する）
@SpringBootApplication
public class Application {

  // Javaプログラムが最初に実行する入り口のメソッド
  public static void main(String[] args) {
    // ① 起動後は http://localhost:8080 でアクセスできる（組み込みのWebサーバーが8080番ポートで待ち受ける）
    // ② SpringApplication.run：Spring Bootを起動し、Webサーバーとアプリ全体を立ち上げる
    SpringApplication.run(Application.class, args);
  }
}
