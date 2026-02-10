import { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { getRequest } from '../services/axiosInstance';
import noImage from '../images/common/CM-NoImage.png';

export default function CM_01_1017() {
  const navigate = useNavigate();
  const { returnId } = useParams();
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);

  const buildImageUrl = (rawUrl) => {
    const base = import.meta.env.VITE_API_BASE_IMAGE_URL || '';

    if (!rawUrl) return noImage;
    if (rawUrl.startsWith('http')) return rawUrl;

    if (rawUrl.startsWith('/images/') && base.endsWith('/images')) {
      return base.replace(/\/images$/, '') + rawUrl;
    }

    if (rawUrl.startsWith('/')) return base + rawUrl;

    return `${base}/${rawUrl}`;
  };

  useEffect(() => {
    if (!returnId) return;

    setLoading(true);
    console.log('詳細取得リクエストID:', returnId);

    getRequest(`/user/returns/${returnId}`)
      .then((res) => {
        console.log('詳細取得結果:', res);
        if (res) {
          setData(res);
        } else {
          alert(res.message || 'データを取得できません。');
          navigate('/mypage/returns');
        }
      })
      .catch((err) => {
        console.error('詳細取得エラー:', err);
        alert('情報の取得中にエラーが発生しました。');
      })
      .finally(() => {
        setLoading(false);
      });
  }, [returnId, navigate]);

  if (loading) return <div className="p-5 text-center fw-bold">読み込み中...</div>;
  if (!data) return <div className="p-5 text-center">データがありません。</div>;

  const imageList = data.imageUrls
    ? data.imageUrls
        .split(',')
        .map((img) => img.trim())
        .filter((img) => img !== '')
    : [];

  // ステータスバッジの色
  const badgeClass = data.returnStatusName?.includes('完了') ? 'bg-success' : 'bg-danger';

  return (
    <div className="container py-4">
      <h3 className="fw-bold mb-4 pb-2 border-bottom">交換／返品 詳細情報</h3>

      {/* 1. 上部情報カード */}
      <div className="card mb-4 shadow-sm">
        <div className="card-header d-flex justify-content-between align-items-center bg-light">
          <span className="fw-bold">申請番号 #{data.returnId}</span>
          <span className={`badge ${badgeClass}`}>{data.returnStatusName}</span>
        </div>
        <div className="card-body">
          <div className="row mb-2">
            <div className="col-3 text-muted">申請商品一覧</div>
            <div className="col-9">
              <div className="vstack gap-2">
                {data.products &&
                  data.products.map((p, idx) => (
                    <div key={idx}>
                      <span className="fw-bold">{p.productName}</span>
                      <span className="text-primary ms-2">({p.quantity}個)</span>
                    </div>
                  ))}
              </div>
            </div>
          </div>
          <div className="row mb-2">
            <div className="col-3 text-muted">注文番号</div>
            <div className="col-9 fw-bold text-primary">{data.orderNo}</div>
          </div>
          <div className="row mb-2">
            <div className="col-3 text-muted">申請日時</div>
            <div className="col-9">{data.requestedAt}</div>
          </div>
          <div className="row mb-2">
            <div className="col-3 text-muted">商品情報</div>
            <div className="col-9">{data.productName}</div>
          </div>
        </div>
      </div>

      {/* 2. 詳細理由（HTML） */}
      <div className="mb-4">
        <label className="form-label fw-bold">詳細理由</label>
        <div
          className="border rounded p-4 bg-white"
          style={{ minHeight: '150px' }}
          dangerouslySetInnerHTML={{ __html: data.reason || '' }}
        />
      </div>

      {imageList.length > 0 && (
        <div className="mb-4">
          <label className="form-label fw-bold">証憑写真 ({imageList.length}枚)</label>
          <div className="d-flex gap-2 overflow-auto p-2 border rounded bg-light">
            {imageList.map((filename, idx) => {
              const fullUrl = buildImageUrl(filename);

              return (
                <a key={idx} href={fullUrl} target="_blank" rel="noreferrer">
                  <img
                    src={fullUrl}
                    alt={`証憑-${idx}`}
                    className="rounded border bg-white"
                    style={{
                      width: '120px',
                      height: '120px',
                      objectFit: 'cover',
                      cursor: 'pointer',
                    }}
                    onError={(e) => {
                      e.target.src = noImage;
                    }}
                  />
                </a>
              );
            })}
          </div>
        </div>
      )}

      <div className="text-center mt-5">
        <button className="btn btn-secondary px-5" onClick={() => navigate('/mypage/returns')}>
          一覧に戻る
        </button>
      </div>
    </div>
  );
}
