export default function QnaItem({ item, index, open, toggle }) {
  if (!item || typeof item !== 'object') return null;

  const { status, question, name, date, detail, answer, answerDate, secret } = item;

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
          <span className={`fw-bold ${status === '답변 완료' ? 'text-success' : 'text-primary'}`}>
            {status}
          </span>
          <span>
            {secret ? (
              <>
                <i className="bi bi-lock-fill me-1" />
                비밀글 입니다.
              </>
            ) : (
              question
            )}
          </span>
        </div>
        <div className="d-flex gap-3 small text-muted">
          <span>{name === '비공개' ? '작성자 비공개' : name}</span>
          <span>{date}</span>
        </div>
      </div>
      {open === index && (
        <div className="px-4 py-3" style={{ backgroundColor: '#f0f0f0' }}>
          {secret ? (
            <p className="text-muted mb-0">비밀글 입니다.</p>
          ) : detail ? (
            <>
              <p className="fw-bold">질문</p>
              <p>{detail}</p>
            </>
          ) : (
            <p className="text-muted mb-0">비밀글 입니다.</p>
          )}
          {!secret && answer ? (
            <>
              <hr className="my-3" />
              <p className="fw-bold mt-3">답변</p>
              <pre className="text-primary mb-0 fs-6" style={{ whiteSpace: 'pre-wrap' }}>
                {answer}
              </pre>
              <p className="text-end small mt-2">{answerDate}</p>
            </>
          ) : !secret ? (
            <p className="text-muted">답변이 아직 등록되지 않았습니다.</p>
          ) : null}
        </div>
      )}
    </div>
  );
}
