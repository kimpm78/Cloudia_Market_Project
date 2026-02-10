import '../styles/CM_company.css';

export default function CM_company() {
  return (
    <div className="container my-5 cm-company">
      <div className="d-flex align-items-center gap-2 mb-4">
        <h1 className="m-0 cm-company_title">会社情報</h1>
      </div>

      <div className="table-responsive">
        <table className="table cm-company_table">
          <tbody>
            <tr>
              <th scope="row">法人名（商号）</th>
              <td>Cloudia Market株式会社</td>
            </tr>
            <tr>
              <th scope="row">代表者</th>
              <td>山田 太郎</td>
            </tr>
            <tr>
              <th scope="row">法人番号</th>
              <td>1234-56-789012</td>
            </tr>
            <tr>
              <th scope="row">所在地</th>
              <td>〒163-0627 東京都 新宿区 西新宿 新宿センタービル 27階</td>
            </tr>
            <tr>
              <th scope="row">メールアドレス</th>
              <td>support@cloudia-market.example</td>
            </tr>
            <tr>
              <th scope="row">個人情報保護管理者</th>
              <td>佐藤 花子</td>
            </tr>
            <tr>
              <th scope="row">営業時間</th>
              <td>平日 10:00〜18:00（土日祝休）</td>
            </tr>
            <tr>
              <th scope="row">事業内容</th>
              <td>ECサイト運営、輸出入代行、カスタマーサポート</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  );
}