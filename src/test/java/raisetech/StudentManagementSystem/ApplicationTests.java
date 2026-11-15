package raisetech.StudentManagementSystem;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * アプリケーション全体が正しく起動できるかを確認するテストクラスです。
 */
// @SpringBootTest：本番と同じようにSpring Boot全体を起動した状態でテストする目印
// （起動時にDB接続などの設定が間違っていると、このテストが失敗する）
@SpringBootTest
class ApplicationTests {

	// @Test：このメソッドがテストであることを示す（JUnitが自動で実行する）
	@Test
	void contextLoads() {
		// 中身は空。Springの起動（コンテキストの読み込み）に成功すれば、このテストは合格になる
	}

}
