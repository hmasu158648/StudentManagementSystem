package raisetech.StudentManagementSystem;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
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
 *   TestException … 例外の動作確認用の独自例外（exceptionパッケージ）
 *   GlobalExceptionHandler … 全コントローラー共通の例外処理。TestExceptionを400エラーに変換する（exception/handlerパッケージ）
 * </pre>
 */
// @OpenAPIDefinition：OpenAPI（Swagger）によるAPI仕様書全体の設定を、このクラスに置く目印
// 　@Info(title = ...)：仕様書の表紙にあたる情報。ここではタイトルを「受講生管理システム」にしている
// （各APIの内容は、コントローラーの内容から自動で作られる）
@OpenAPIDefinition(
    info = @Info(
        title = "受講生管理システム",
        description = "受講生とコース情報の検索・登録・更新を行うAPIです。",
        version = "1.0.0"))
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
