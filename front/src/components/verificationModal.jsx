export default function VerificationModal({
  show,
  onClose,
  timeLeft,
  verificationCode,
  onCodeChange,
  onVerify,
  onResend,
  isEmailVerified,
  error,
  isLoading,
}) {
  if (!show) {
    return null;
  }

  const minutes = Math.floor(timeLeft / 60);
  const seconds = timeLeft % 60;

  return (
    <div className="popup-overlay">
      <div className="popup-content">
        <button onClick={onClose} className="popup-close-button">
          &times;
        </button>

        <h2>認証番号を入力してください。</h2>

        <input
          type="text"
          className="form-control my-3"
          placeholder="認証番号を入力"
          value={verificationCode}
          onChange={onCodeChange}
          disabled={timeLeft <= 0 || isEmailVerified || isLoading}
        />

        {error && <div className="text-danger mt-2">{error}</div>}

        <div className="timer-display">
          残り時間: {String(minutes).padStart(2, '0')}:{String(seconds).padStart(2, '0')}
        </div>

        {isEmailVerified ? (
          <p className="text-success mt-2">メール認証が完了しました。</p>
        ) : timeLeft > 0 ? (
          <button className="btn btn-primary mt-3" onClick={onVerify} disabled={isLoading}>
            {isLoading ? '処理中...' : '確認'}
          </button>
        ) : (
          <button className="btn btn-secondary mt-3" onClick={onResend} disabled={isLoading}>
            {isLoading ? '処理中...' : '認証コードを再送信'}
          </button>
        )}

        {timeLeft <= 0 && !isEmailVerified && (
          <p className="text-danger mt-2">
            認証時間が切れました。コードを再送信するか、ポップアップを閉じて再度お試しください。
          </p>
        )}
      </div>
    </div>
  );
}
