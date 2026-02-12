import { useCallback, useEffect, useMemo, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';

import { fetchInquiryDetail, answerInquiry, deleteInquiry } from '../services/InquiryService.js';
import { useAuth } from '../contexts/AuthContext.jsx';

import CM_99_1001 from '../components/commonPopup/CM_99_1001.jsx';
import CM_99_1003 from '../components/commonPopup/CM_99_1003.jsx';
import CM_99_1004 from '../components/commonPopup/CM_99_1004.jsx';

const formatDate = (dateString) => {
  if (!dateString) return '';
  return dateString.split('T')[0];
};

const resolveInquiryStatus = (statusCode, statusValue) => {
  if (statusValue === 2 || statusCode === 'ANSWERED') {
    return { label: '回答済み', badgeClass: 'bg-success text-white' };
  }
  return { label: '回答待ち', badgeClass: 'bg-secondary text-white' };
};

export default function CM_01_1013() {
  const params = useParams();
  const inquiryId = params.inquiryId ?? params.id;
  const navigate = useNavigate();
  const { isLoggedIn, user } = useAuth();

  // --- 상태 관리 ---
  const [inquiry, setInquiry] = useState(null);
  const [prevNext, setPrevNext] = useState({ prev: null, next: null });
  const [loading, setLoading] = useState(true);

  // 답변 관련
  const [answerDraft, setAnswerDraft] = useState('');
  const [editingAnswer, setEditingAnswer] = useState(false);
  const [submittingAnswer, setSubmittingAnswer] = useState(false);
  const [answerError, setAnswerError] = useState('');

  // 권한 및 삭제 상태
  const [isAuthor, setIsAuthor] = useState(false);
  const [isDeleting, setIsDeleting] = useState(false);

  // --- 팝업 상태 ---
  const [errorPopup, setErrorPopup] = useState({ open: false, message: '' });
  const [confirmPopup, setConfirmPopup] = useState({ open: false, message: '' });
  const [successPopup, setSuccessPopup] = useState({ open: false, message: '' });

  const isAdmin = useMemo(() => {
    if (!user) return false;
    if (user.roleId === 1) return true;
    if (Array.isArray(user.authorities)) {
      return user.authorities.some(
        (auth) =>
          (auth && auth.authority === 'ROLE_ADMIN') || (auth && auth.authority === 'ROLE_MANAGER')
      );
    }
    return false;
  }, [user]);

  // 문의 상세 조회
  const loadInquiryDetail = useCallback(
    async (options = {}) => {
      if (!inquiryId) return false;
      if (!options.silent) setLoading(true);
      try {
        const detail = await fetchInquiryDetail({ inquiryId });

        if (detail?.current) {
          setInquiry(detail.current);
          setPrevNext({ prev: detail.prev, next: detail.next });
          setAnswerDraft(detail.current.answerContent || '');
          setEditingAnswer(false);
          setAnswerError('');

          // 본인 확인
          if (user) {
            const isLoginIdMatch =
              detail.current.loginId && user.loginId === detail.current.loginId;
            const isUserIdMatch = detail.current.userId && user.userId === detail.current.userId;
            setIsAuthor(isLoginIdMatch || isUserIdMatch);
          } else {
            setIsAuthor(false);
          }
        } else {
          setInquiry({ notFound: true });
        }
        return true;
      } catch (err) {
        console.error('1:1お問い合わせ 詳細の読み込みに失敗:', err?.message || err);
        if (!options.silent) {
          const message =
            err?.response?.status === 403
              ? '非公開の投稿です。アクセス権限がありません。'
              : err?.response?.data?.message || err?.message || 'お問い合わせを読み込めませんでした。';

          setErrorPopup({ open: true, message: message });
          setInquiry({ notFound: true });
        }
        return false;
      } finally {
        if (!options.silent) {
          setLoading(false);
        }
      }
    },
    [inquiryId, user]
  );

  useEffect(() => {
    if (!inquiryId) {
      setErrorPopup({ open: true, message: 'お問い合わせIDが必要です。' });
      setInquiry({ notFound: true });
      setLoading(false);
      return undefined;
    }
    loadInquiryDetail();
  }, [inquiryId, loadInquiryDetail]);

  const statusInfo = useMemo(
    () => resolveInquiryStatus(inquiry?.statusCode, inquiry?.statusValue),
    [inquiry]
  );

  const maskId = (id) => {
    if (id.length <= 2) return id.length === 1 ? '*' : '**';
    return id.slice(0, -2) + '**';
  };

  const writerDisplay = useMemo(() => {
    if (!inquiry) return '';

    const idToMask = inquiry.loginId;
    return maskId(idToMask);
  }, [inquiry]);

  if (loading) {
    return (
      <div className="container-fluid px-5 py-5">
        <h1 className="m-2">1:1お問い合わせ</h1>
        <div className="text-center py-5">読み込み中です...</div>
      </div>
    );
  }

  if (!inquiry || inquiry.notFound) {
    return (
      <>
        <div className="container-fluid px-5 py-5">
          <h1 className="m-2">1:1お問い合わせ</h1>
          <div className="text-center py-5 text-danger">該当のお問い合わせが見つかりません。</div>
        </div>
        <CM_99_1003
          isOpen={errorPopup.open}
          onClose={() => setErrorPopup({ ...errorPopup, open: false })}
          message={errorPopup.message}
        />
      </>
    );
  }

  const hasAnswer = Boolean(inquiry?.answerContent && inquiry.answerContent.trim());
  const canManageAnswer = isLoggedIn && isAdmin && !isAuthor;
  const { prev, next } = prevNext;

  // 날짜 포맷팅 적용
  const createdDate = formatDate(inquiry.createdAt);
  const answerDate = formatDate(inquiry.answerCreatedAt);

  const handleAnswerEditStart = () => {
    setAnswerDraft(inquiry.answerContent || '');
    setEditingAnswer(true);
    setAnswerError('');
  };

  const handleAnswerCancel = () => {
    setAnswerDraft(inquiry.answerContent || '');
    setEditingAnswer(false);
    setAnswerError('');
  };

  const handleAnswerSubmit = async () => {
    const trimmed = answerDraft.trim();
    if (!trimmed) {
      setAnswerError('回答内容を入力してください。');
      return;
    }
    setSubmittingAnswer(true);
    try {
      await answerInquiry({ inquiryId: inquiry.inquiryId, answerContent: trimmed });
      const refreshed = await loadInquiryDetail({ silent: true });
      if (!refreshed) setAnswerError('最新データを取得できませんでした。');
      else setEditingAnswer(false);
    } catch (err) {
      setAnswerError(err?.response?.data?.message || '回答の保存に失敗しました。');
    } finally {
      setSubmittingAnswer(false);
    }
  };

  const handleDeleteClick = () => {
    if (hasAnswer) {
      setErrorPopup({ open: true, message: '回答済みのお問い合わせは削除できません。' });
      return;
    }
    setConfirmPopup({
      open: true,
      message: 'このお問い合わせを削除してもよろしいですか？',
    });
  };

  const handleDeleteConfirm = async () => {
    setConfirmPopup({ ...confirmPopup, open: false });
    setIsDeleting(true);
    try {
      await deleteInquiry({ inquiryId });

      setSuccessPopup({ open: true, message: 'お問い合わせを削除しました。' });
    } catch (err) {
      const msg =
        err?.response?.headers?.['x-error-message'] ||
        err?.response?.data?.message ||
        '削除中にエラーが発生しました。';
      setErrorPopup({ open: true, message: msg });
    } finally {
      setIsDeleting(false);
    }
  };

  const handleSuccessClose = () => {
    setSuccessPopup({ ...successPopup, open: false });
    navigate('/mypage/inquiries');
  };

  const renderAnswerSection = () => {
    if (canManageAnswer) {
      return (
        <div className="border rounded p-3 bg-light">
          <div className="fw-semibold mb-2">回答</div>
          {answerError && <div className="text-danger small mb-2">{answerError}</div>}
          {editingAnswer ? (
            <>
              <textarea
                className="form-control"
                rows={6}
                value={answerDraft}
                onChange={(e) => setAnswerDraft(e.target.value)}
                disabled={submittingAnswer}
              />
              <div className="d-flex justify-content-end gap-2 mt-3">
                <button
                  type="button"
                  className="btn btn-primary btn-sm"
                  onClick={handleAnswerSubmit}
                  disabled={submittingAnswer}
                >
                  {submittingAnswer ? '保存中...' : '保存'}
                </button>
                <button
                  type="button"
                  className="btn btn-outline-secondary btn-sm"
                  onClick={handleAnswerCancel}
                  disabled={submittingAnswer}
                >
                  キャンセル
                </button>
              </div>
            </>
          ) : hasAnswer ? (
            <>
              <div className="text-muted small mb-2">
                {inquiry.answererName || inquiry.answererLoginId || '管理者'} / {answerDate}に
                作成
              </div>
              <div style={{ whiteSpace: 'pre-line' }}>{inquiry.answerContent}</div>
              <div className="d-flex justify-content-end gap-2 mt-3">
                <button
                  type="button"
                  className="btn btn-outline-primary btn-sm"
                  onClick={handleAnswerEditStart}
                >
                  回答を編集
                </button>
              </div>
            </>
          ) : (
            <div className="d-flex justify-content-between align-items-center">
              <span className="text-muted small">登録された回答がありません。</span>
              <button
                type="button"
                className="btn btn-outline-primary btn-sm"
                onClick={handleAnswerEditStart}
              >
                回答を作成
              </button>
            </div>
          )}
        </div>
      );
    }
    console.log(inquiry);
    return (
      <div className="border rounded p-3 bg-light">
        {hasAnswer ? (
          <>
            <div className="d-flex justify-content-between align-items-center mb-2">
              <div className="fw-semibold">回答</div>
              <div className="text-muted small">
                {inquiry.answererRoleId === 1 ? '運営者' : inquiry.answererLoginId} / {answerDate}に
                作成
              </div>
            </div>
            <div style={{ whiteSpace: 'pre-line' }}>{inquiry.answerContent}</div>
          </>
        ) : (
          <>
            <div className="fw-semibold mb-2">回答</div>
            <span className="text-muted small">登録された回答がありません。</span>
          </>
        )}
      </div>
    );
  };

  return (
    <>
      <h2 className="fw-bolder mt-3">1:1お問い合わせ</h2>
      <div className="container-fluid px-3 px-md-5 py-4">
        <div className="border-top border-bottom py-3 px-2 d-flex justify-content-between fs-6 fs-sm-5">
          <div className="flex-grow-1">
            <span className={`badge ${statusInfo.badgeClass} fw-semibold me-2`}>
              {statusInfo.label}
            </span>
            {Number(inquiry.isPrivate) === 1 && <i className="bi bi-file-lock2 me-2"></i>}
            <span className="fw-bold">{inquiry.title}</span>
            <div className="d-flex d-sm-none justify-content-end text-muted small mt-2">
              <span className="me-2">{writerDisplay}</span>
              <span className="me-2">/ {createdDate}</span>
            </div>
          </div>
          <div className="d-none d-sm-flex small text-muted align-self-center">
            <span className="me-3">{writerDisplay}</span>
            <span className="me-3">{createdDate}</span>
          </div>
        </div>

        {/* 본문 */}
        <div className="p-3" style={{ whiteSpace: 'pre-line', minHeight: '150px' }}>
          {inquiry.content}
        </div>

        {/* 답변 */}
        {renderAnswerSection()}

        {/* 이전/다음 글 */}
        <div className="border-top fs-5 mt-4">
          <div className="d-flex text-muted small border-bottom py-2">
            <span className="d-flex align-items-center gap-1">
              前の記事<i className="bi bi-caret-up-fill"></i>
            </span>
            {prev ? (
              <span
                className="fs-6 ms-3 text-primary hover-underline text-decoration-underline-hover"
                style={{ cursor: 'pointer' }}
                onClick={() => navigate(`/mypage/inquiries/${prev.inquiryId}`)}
              >
                {prev.title}
              </span>
            ) : (
              <span className="fs-6 ms-3 text-muted">前の記事はありません。</span>
            )}
          </div>
          <div className="d-flex small py-2">
            <span className="d-flex align-items-center gap-1">
              次の記事<i className="bi bi-caret-down-fill"></i>
            </span>
            {next ? (
              <span
                className="fs-6 ms-3 text-primary hover-underline text-decoration-underline-hover"
                style={{ cursor: 'pointer' }}
                onClick={() => navigate(`/mypage/inquiries/${next.inquiryId}`)}
              >
                {next.title}
              </span>
            ) : (
              <span className="fs-6 ms-3 text-muted">次の記事はありません。</span>
            )}
          </div>
        </div>

        {/* 버튼 */}
        <div className="d-flex justify-content-center align-items-center mt-4 gap-2">
          <button className="btn btn-secondary px-5" onClick={() => navigate('/mypage/inquiries')}>
            一覧
          </button>
          {isAuthor && !hasAnswer && (
            <button
              className="btn btn-danger px-5"
              onClick={handleDeleteClick}
              disabled={isDeleting}
            >
              {isDeleting ? '削除中...' : '削除'}
            </button>
          )}
        </div>
      </div>

      <CM_99_1003
        isOpen={errorPopup.open}
        onClose={() => setErrorPopup({ ...errorPopup, open: false })}
        message={errorPopup.message}
      />
      <CM_99_1001
        isOpen={confirmPopup.open}
        onClose={() => setConfirmPopup({ ...confirmPopup, open: false })}
        onConfirm={handleDeleteConfirm}
        Message={confirmPopup.message}
      />
      <CM_99_1004
        isOpen={successPopup.open}
        onClose={handleSuccessClose}
        Message={successPopup.message}
      />
    </>
  );
}
