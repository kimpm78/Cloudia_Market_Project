export default function CM_CookiePage() {
  return (
    <>
      <h1>クッキーポリシー</h1>
      <div className="container my-5" style={{ fontSize: '18px' }}>
        <p>
          当ウェブサイトでは、ユーザー体験の向上およびパーソナライズされたサービス提供のためにクッキーを使用します。
        </p>

        <h5 className="fw-bold mt-5">1. クッキーとは？</h5>
        <p>
          クッキーとは、ウェブサイト閲覧時にブラウザへ保存される小さなテキストファイルで、ユーザーの設定や訪問履歴などを記憶するために使用されます。
        </p>

        <h5 className="fw-bold mt-5">2. クッキーの種類</h5>
        <ul>
          <li>
            <strong>必須クッキー：</strong>
            カート機能など、サイト利用に必要な中核機能を提供します。
          </li>
          <li>
            <strong>パフォーマンスクッキー：</strong>
            訪問者の利用状況を分析し、サイト改善に役立てます。
          </li>
          <li>
            <strong>マーケティングクッキー：</strong>
            ターゲティング広告やおすすめコンテンツの提供に利用されます。
          </li>
        </ul>

        <h5 className="fw-bold mt-5">3. 第三者クッキー</h5>
        <p>
          <strong>Google</strong>{' '}
          などの外部サービスを通じて収集される情報が含まれる場合があります。詳細は Google の{' '}
          <a
            href="https://policies.google.com/privacy?hl=ja"
            target="_blank"
            rel="noopener noreferrer"
          >
            プライバシーポリシー
          </a>
          に従います。
        </p>

        <h5 className="fw-bold mt-5">4. 収集されるクッキー情報</h5>
        <ul>
          <li>
            <strong>cookieConsent：</strong>
            クッキー同意状況（保存期間：1年、提供者：当社）
          </li>
          <li>
            <strong>cookie_performance：</strong>
            パフォーマンスクッキー同意状況（保存期間：1年、提供者：当社）
          </li>
          <li>
            <strong>cookie_targeting：</strong>
            マーケティング／ターゲティングクッキー同意状況（保存期間：1年、提供者：当社）
          </li>
          <li>
            <strong>Google Analytics クッキー（例：_ga など）：</strong>
            パフォーマンスクッキーに同意した場合に生成されることがあります。保存期間および提供者は
            Google のポリシーに従います。
          </li>
        </ul>

        <h5 className="fw-bold mt-5">5. クッキーの拒否方法と影響</h5>
        <p>
          ユーザーはブラウザ設定によりクッキーの受け入れ可否を変更できます。ただし、必須クッキーを拒否した場合、カートの保持など一部機能に制限が生じることがあります。
        </p>

        <h5 className="fw-bold mt-5">6. ブラウザ別クッキー設定方法</h5>
        <ul>
          <li>
            <a
              href="https://support.google.com/chrome/answer/95647?hl=ja"
              target="_blank"
              rel="noopener noreferrer"
            >
              Chrome のクッキー設定方法
            </a>
          </li>
          <li>
            <a
              href="https://support.apple.com/ja-jp/guide/safari/sfri11471/mac"
              target="_blank"
              rel="noopener noreferrer"
            >
              Safari のクッキー設定方法
            </a>
          </li>
          <li>
            <a
              href="https://support.microsoft.com/ja-jp/help/4027947"
              target="_blank"
              rel="noopener noreferrer"
            >
              Edge のクッキー設定方法
            </a>
          </li>
        </ul>
      </div>
    </>
  );
}
