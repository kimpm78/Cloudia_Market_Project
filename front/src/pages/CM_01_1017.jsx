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
    console.log('상세 조회 요청 ID:', returnId);

    getRequest(`/user/returns/${returnId}`)
      .then((res) => {
        console.log('상세 조회 결과:', res);
        if (res) {
          setData(res);
        } else {
          alert(res.message || '데이터를 불러올 수 없습니다.');
          navigate('/mypage/returns');
        }
      })
      .catch((err) => {
        console.error('상세 조회 에러:', err);
        alert('정보를 가져오는 중 오류가 발생했습니다.');
      })
      .finally(() => {
        setLoading(false);
      });
  }, [returnId, navigate]);

  if (loading) return <div className="p-5 text-center fw-bold">로딩 중...</div>;
  if (!data) return <div className="p-5 text-center">데이터가 없습니다.</div>;

  const imageList = data.imageUrls
    ? data.imageUrls
        .split(',')
        .map((img) => img.trim())
        .filter((img) => img !== '')
    : [];

  // 상태 배지 색상
  const badgeClass = data.returnStatusName?.includes('완료') ? 'bg-success' : 'bg-danger';

  return (
    <div className="container py-4">
      <h3 className="fw-bold mb-4 pb-2 border-bottom">교환/반품 상세 정보</h3>

      {/* 1. 상단 정보 카드 */}
      <div className="card mb-4 shadow-sm">
        <div className="card-header d-flex justify-content-between align-items-center bg-light">
          <span className="fw-bold">신청번호 #{data.returnId}</span>
          <span className={`badge ${badgeClass}`}>{data.returnStatusName}</span>
        </div>
        <div className="card-body">
          <div className="row mb-2">
            <div className="col-3 text-muted">신청 상품 목록</div>
            <div className="col-9">
              <div className="vstack gap-2">
                {data.products &&
                  data.products.map((p, idx) => (
                    <div key={idx}>
                      <span className="fw-bold">{p.productName}</span>
                      <span className="text-primary ms-2">({p.quantity}개)</span>
                    </div>
                  ))}
              </div>
            </div>
          </div>
          <div className="row mb-2">
            <div className="col-3 text-muted">주문번호</div>
            <div className="col-9 fw-bold text-primary">{data.orderNo}</div>
          </div>
          <div className="row mb-2">
            <div className="col-3 text-muted">신청일시</div>
            <div className="col-9">{data.requestedAt}</div>
          </div>
          <div className="row mb-2">
            <div className="col-3 text-muted">상품정보</div>
            <div className="col-9">{data.productName}</div>
          </div>
        </div>
      </div>

      {/* 2. 상세 사유 (HTML) */}
      <div className="mb-4">
        <label className="form-label fw-bold">상세 사유</label>
        <div
          className="border rounded p-4 bg-white"
          style={{ minHeight: '150px' }}
          dangerouslySetInnerHTML={{ __html: data.reason || '' }}
        />
      </div>

      {imageList.length > 0 && (
        <div className="mb-4">
          <label className="form-label fw-bold">증빙 사진 ({imageList.length}장)</label>
          <div className="d-flex gap-2 overflow-auto p-2 border rounded bg-light">
            {imageList.map((filename, idx) => {
              const fullUrl = buildImageUrl(filename);

              return (
                <a key={idx} href={fullUrl} target="_blank" rel="noreferrer">
                  <img
                    src={fullUrl}
                    alt={`증빙-${idx}`}
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
          목록으로 돌아가기
        </button>
      </div>
    </div>
  );
}
