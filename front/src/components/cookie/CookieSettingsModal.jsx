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
            <h5 className="modal-title">쿠키 설정</h5>
            <button type="button" className="btn-close" onClick={onClose}></button>
          </div>
          <div className="modal-body">
            <p>서비스 제공을 위해 쿠키를 사용합니다. 설정을 선택하세요.</p>
            <hr />
            <div>
              <strong>▪︎ 필수 쿠키</strong> - 항상 활성화
              <div className="text-secondary">사이트 기능 작동에 필수적인 쿠키입니다.</div>
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
                성능 쿠키
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
                타겟팅 쿠키
              </label>
            </div>
          </div>
          <div className="modal-footer">
            <button type="button" className="btn btn-secondary" onClick={onClose}>
              취소
            </button>
            <button type="button" className="btn btn-primary" onClick={handleSave}>
              선택 내용 저장
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}
