export default function QnaItem({ item, index, open, toggle }) {
  if (!item || typeof item !== 'object') return null;

  const { status, question, name, date, detail, answer, answerDate, secret } = item;

  // 表示用ステータス（既存データが韓国語の場合も考慮）
  const statusLabelMap = {
    '답변 완료': '回答済み',
    '답변 대기': '回答待ち',
  };
  const displayStatus = statusLabelMap[status] ?? status;
  const isAnswered = status === '답변 완료' || status === '回答済み' || status === '回答完了';

  // 作成者名（既存データが韓国語の場合も考慮）
  const isPrivateName = name === '비공개' || name === '非公開';
  const displayName = isPrivateName ? '投稿者非公開' : name;

  return (
    <div className="py-1">
      <div
        role="button"
        tabIndex={0}
        onClick={() => {
          toggle(index);
        }}
        onKeyDown={(e) => {
          if (e.key === 'Enter' || e.key === ' ') {
            toggle(index);
          }
        }}
        className="d-flex justify-content-between p-3 align-items-center"
        style={{ cursor: 'pointer', background: '#f9f9f9' }}
      >
        <div className="d-flex gap-3">
          <span className={`fw-bold ${isAnswered ? 'text-success' : 'text-primary'}`}>
            {displayStatus}
          </span>
          <span>
            {secret ? (
              <>
                <i className="bi bi-lock-fill me-1" />
                非公開です。
              </>
            ) : (
              question
            )}
          </span>
        </div>
        <div className="d-flex gap-3 small text-muted">
          <span>{displayName}</span>
          <span>{date}</span>
        </div>
      </div>
      {open === index && (
        <div className="px-4 py-3" style={{ backgroundColor: '#f0f0f0' }}>
          {secret ? (
            <p className="text-muted mb-0">非公開です。</p>
          ) : detail ? (
            <>
              <p className="fw-bold">質問</p>
              <p>{detail}</p>
            </>
          ) : (
            <p className="text-muted mb-0">非公開です。</p>
          )}
          {!secret && answer ? (
            <>
              <hr className="my-3" />
              <p className="fw-bold mt-3">回答</p>
              <pre className="text-primary mb-0 fs-6" style={{ whiteSpace: 'pre-wrap' }}>
                {answer}
              </pre>
              <p className="text-end small mt-2">{answerDate}</p>
            </>
          ) : !secret ? (
            <p className="text-muted">回答はまだ登録されていません。</p>
          ) : null}
        </div>
      )}
    </div>
  );
}
