import { useState, useEffect } from 'react';

export default function CookieSettingsModal({ show, onClose, onSave }) {
  const [performance, setPerformance] = useState(false);
  const [targeting, setTargeting] = useState(false);

  useEffect(() => {
    if (show) {
      document.body.classList.add('modal-open');
    } else {
      document.body.classList.remove('modal-open');
    }

    return () => document.body.classList.remove('modal-open');
  }, [show]);

  const handleSave = () => {
    const settings = {
      necessary: true,
      performance,
      targeting,
    };
    onSave(settings);
    onClose();
  };

  if (!show) return null;

  return (
    <div
      className="modal d-block"
      tabIndex="-1"
      role="dialog"
      style={{ backgroundColor: 'rgba(0, 0, 0, 0.5)' }}
    >
      <div className="modal-dialog" role="document">
        <div className="modal-content">
          <div className="modal-header">
            <h5 className="modal-title">クッキー設定</h5>
            <button type="button" className="btn-close" onClick={onClose}></button>
          </div>
          <div className="modal-body">
            <p>サービス提供のためにクッキーを使用しています。設定を選択してください。</p>
            <hr />
            <div>
              <strong>▪︎ 必須クッキー</strong> - 常に有効
              <div className="text-secondary">サイトの機能を動作させるために必須のクッキーです。</div>
            </div>
            <div className="form-check form-switch mt-3">
              <input
                className="form-check-input"
                type="checkbox"
                id="performance-cookie"
                checked={performance}
                onChange={(e) => setPerformance(e.target.checked)}
              />
              <label className="form-check-label" htmlFor="performance-cookie">
                パフォーマンスクッキー
              </label>
            </div>
            <div className="form-check form-switch mt-2">
              <input
                className="form-check-input"
                type="checkbox"
                id="targeting-cookie"
                checked={targeting}
                onChange={(e) => setTargeting(e.target.checked)}
              />
              <label className="form-check-label" htmlFor="targeting-cookie">
                ターゲティングクッキー
              </label>
            </div>
          </div>
          <div className="modal-footer">
            <button type="button" className="btn btn-secondary" onClick={onClose}>
              キャンセル
            </button>
            <button type="button" className="btn btn-primary" onClick={handleSave}>
              選択内容を保存
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}
