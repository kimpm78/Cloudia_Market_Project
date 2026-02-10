import { useState, useEffect } from 'react';
import { useParams, useNavigate, useLocation } from 'react-router-dom';

import CM_99_1001 from '../components/commonPopup/CM_99_1001.jsx';
import CM_99_1002 from '../components/commonPopup/CM_99_1002.jsx';
import CM_99_1003 from '../components/commonPopup/CM_99_1003.jsx';
import CM_99_1004 from '../components/commonPopup/CM_99_1004.jsx';

import { CATEGORIES } from '../constants/constants.js';
import axiosInstance from '../services/axiosInstance.js';
import { useAuth } from '../contexts/AuthContext.jsx';
import { formatYearMonthDot } from '../utils/DateFormat.js';
import CMMessage from '../constants/CMMessage';

export default function CM_05_1001() {
  const { user } = useAuth();
  const { id } = useParams();
  const navigate = useNavigate();
  const location = useLocation();
  const fromAdmin =
    location?.state?.from === 'admin' ||
    new URLSearchParams(location.search).get('from') === 'admin' ||
    location.pathname.startsWith('/admin');

  const backTo = fromAdmin ? '/admin/menu/notice' : '/notice';
  const [open1001, setOpen1001] = useState(false); // 確認ポップアップ
  const [open1002, setOpen1002] = useState(false); // ローディングポップアップ
  const [open1003, setOpen1003] = useState(false); // エラーポップアップ
  const [open1004, setOpen1004] = useState(false); // アラートポップアップ
  const [notice, setNotice] = useState(null);
  const [prevNext, setPrevNext] = useState({ prev: null, next: null });
  const formatAuthor = (roleId, userId) => {
    if (Number(roleId) === 1) return '管理者';
    if (Number(roleId) === 2) return 'マネージャー';
    const raw = String(userId ?? '').trim();
    if (!raw) return '-';
    return `user${raw}`;
  };

  useEffect(() => {
    axiosInstance
      .get(`${import.meta.env.VITE_API_BASE_URL}/guest/notice/${id}`, {
        params: { noticeId: id },
      })
      .then((res) => {
        const data = res.data?.resultList;
        if (data) {
          setNotice(data.current);
          setPrevNext({ prev: data.prev, next: data.next });
        }
      })
      .catch((err) => {
        console.error('お知らせ詳細ロード失敗:', err?.response?.data?.message || err.message);
      });
  }, [id]);

  useEffect(() => {
    if (!id) return;
    axiosInstance
      .post(`${import.meta.env.VITE_API_BASE_URL}/guest/notice/${id}/view`)
      .catch((err) => {
        console.error('お知らせ閲覧数増加失敗:', err?.response?.data?.message || err.message);
      });
  }, [id]);

  const { prev: prevNotice, next: nextNotice } = prevNext;

  if (!notice) {
    return (
      <div className="container-fluid px-5 py-5">
        <h2 className="fw-bold mb-4 border-bottom pb-3">お知らせ</h2>
        <div className="text-center py-5 text-danger">{CMMessage.MSG_EMPTY_012}</div>
      </div>
    );
  }

  return (
    <div className="container-fluid px-3 px-md-5">
      <h1 className='m-0 notice-title'>お知らせ</h1>
      <div className="container-fluid px-3 py-4">
        <div className="border-top border-bottom py-3 px-2 d-flex justify-content-between fs-6 fs-sm-5">
          <div className="flex-grow-1">
            {(() => {
              const value = notice.codeValue;
              const category = CATEGORIES.find((cat) => cat.value === value);
              const label = category ? category.label : value;

              let className = 'default';
              if (value === 1 || value === 2) className = 'urgent';
              else if (value === 3) className = 'payment';

              return <span className={`badge-category ${className} fw-bold me-2`}>{label}</span>;
            })()}
            <span className="fw-bold">{notice.title}</span>
            <div className="d-flex d-sm-none justify-content-end text-muted small mt-2">
              <span className="me-2">{formatAuthor(notice.roleId, notice.userId)}</span>
              <span className="me-2">/ {formatYearMonthDot(notice.publishedAt)}</span>
              <span> 閲覧数: {notice.viewCount}</span>
            </div>
          </div>
          <div className="d-none d-sm-flex small text-muted align-self-center">
            <span className="me-3">{formatAuthor(notice.roleId, notice.userId)}</span>
            <span className="me-3">{formatYearMonthDot(notice.publishedAt)}</span>
            <span>閲覧数: {notice.viewCount}</span>
          </div>
        </div>
        <div className="p-3" style={{ whiteSpace: 'pre-line' }}>
          {notice.content}
        </div>
        <div className="border-top fs-5">
          <div className="d-flex text-muted small border-bottom py-2">
            <span className="d-flex align-items-center gap-1">
              以前の記事<i className="bi bi-caret-up-fill"></i>
            </span>
            {prevNotice && prevNotice.isDisplay === 1 ? (
              <span
                className="fs-6 ms-3 text-primary hover-underline text-decoration-underline-hover"
                style={{ cursor: 'pointer' }}
                onClick={() =>
                  navigate(`/notice/${prevNotice.noticeId}`, {
                    state: { from: fromAdmin ? 'admin' : 'main' },
                  })
                }
              >
                {prevNotice.title}
              </span>
            ) : (
              <span className="fs-6 ms-3 text-muted">前の記事の内容はありません。</span>
            )}
          </div>
          <div className="d-flex small py-2">
            <span className="d-flex align-items-center gap-1">
              次の記事<i className="bi bi-caret-down-fill"></i>
            </span>
            {nextNotice && nextNotice.isDisplay === 1 ? (
              <span
                className="fs-6 ms-3 text-primary hover-underline text-decoration-underline-hover"
                style={{ cursor: 'pointer' }}
                onClick={() =>
                  navigate(`/notice/${nextNotice.noticeId}`, {
                    state: { from: fromAdmin ? 'admin' : 'main' },
                  })
                }
              >
                {nextNotice.title}
              </span>
            ) : (
              <span className="fs-6 ms-3 text-muted">次の記事の内容はありません。</span>
            )}
          </div>
        </div>

        <div className="d-flex flex-column justify-content-center align-items-center mt-4 gap-2">
          {user?.roleId === 1 || user?.roleId === 2 ? (
            <div className="d-flex flex-column flex-sm-row flex-wrap gap-2 justify-content-center">
              <button
                className="btn btn-primary px-5"
                onClick={() => navigate(`/admin/menu/notice/edit/${notice.noticeId}`)}
              >
                編集
              </button>
              <button className="btn btn-secondary px-5" onClick={() => navigate(backTo)}>
                一覧
              </button>
              <button className="btn btn-danger px-5" onClick={() => setOpen1001(true)}>
                削除
              </button>
            </div>
          ) : (
            <button className="btn btn-secondary px-5" onClick={() => navigate('/notice')}>
              一覧
            </button>
          )}
        </div>
      </div>
      <CM_99_1001
        isOpen={open1001}
        onClose={() => setOpen1001(false)}
        onConfirm={() => {
          if (notice.userId !== '1') {
            alert(CMMessage.MSG_ERR_021);
            setOpen1001(false);
            return;
          }
          setOpen1001(false);
          setOpen1002(true); // ローディング開始

          axiosInstance
            .delete(`/guest/notice/${notice.noticeId}`, {
              params: { userId: '1' },
            })
            .then(() => {
              setTimeout(() => {
                setOpen1002(false);
                setOpen1004(true); // アラートポップアップ表示
              }, 1000);
            })
            .catch((err) => {
              setOpen1002(false);
              setOpen1003(true); // エラーポップアップ表示
            });
        }}
        Message={CMMessage.MSG_CON_003}
      />
      <CM_99_1002
        isOpen={open1002}
        onClose={() => setOpen1002(false)}
        duration={3000}
        hideClose={true}
      />
      <CM_99_1003
        isOpen={open1003}
        onClose={() => setOpen1003(false)}
        errorMessage={CMMessage.MSG_ERR_017}
      />
      <CM_99_1004
        isOpen={open1004}
        onClose={() => {
          setOpen1004(false);
          navigate(backTo);
        }}
        Message={CMMessage.MSG_INF_005}
      />
    </div>
  );
}
