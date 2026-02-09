import { useCallback, useEffect, useMemo, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';

import CM_99_1001 from '../components/commonPopup/CM_99_1001.jsx';
import CM_99_1002 from '../components/commonPopup/CM_99_1002.jsx';
import CM_99_1003 from '../components/commonPopup/CM_99_1003.jsx';
import CM_99_1004 from '../components/commonPopup/CM_99_1004.jsx';

import { resolveQnaStatus } from '../constants/Qna.js';
import { fetchQnaDetail, answerQna, deleteQna } from '../services/QnaService.js';
import { useAuth } from '../contexts/AuthContext.jsx';
import CMMessage from '../constants/CMMessage';

export default function CM_04_1004() {
  const params = useParams();
  const qnaId = params.qnaId ?? params.id;
  const navigate = useNavigate();
  const { isLoggedIn, user } = useAuth();

  const [errorMessage, setErrorMessage] = useState('');
  const [openError, setOpenError] = useState(false);
  const [qna, setQna] = useState(null);
  const [prevNext, setPrevNext] = useState({ prev: null, next: null });
  const [loading, setLoading] = useState(true);
  const [answerDraft, setAnswerDraft] = useState('');
  const [editingAnswer, setEditingAnswer] = useState(false);
  const [submittingAnswer, setSubmittingAnswer] = useState(false);
  const [answerError, setAnswerError] = useState('');
  const [deleting, setDeleting] = useState(false);
  const [deleteError, setDeleteError] = useState('');
  const [openConfirmDelete, setOpenConfirmDelete] = useState(false);
  const [open1002, setOpen1002] = useState(false);
  const [open1004, setOpen1004] = useState(false);
  const [popupMessage] = useState('');

  const isAdmin = useMemo(() => {
    if (!user) return false;
    if (user.roleId === 1) return true;
    const roleValue = (user.role || user.roleName || user.roleCode || '').toString().toUpperCase();
    if (roleValue.includes('ADMIN') || roleValue.includes('MANAGER')) return true;
    if (Array.isArray(user.roles)) {
      return user.roles.some(
        (role) =>
          typeof role === 'string' &&
          (role.toUpperCase().includes('ADMIN') || role.toUpperCase().includes('MANAGER'))
      );
    }
    if (Array.isArray(user.authorities)) {
      return user.authorities.some(
        (role) =>
          typeof role === 'string' &&
          (role.toUpperCase().includes('ADMIN') || role.toUpperCase().includes('MANAGER'))
      );
    }
    return false;
  }, [user]);

  const loadQnaDetail = useCallback(
    async (options = {}) => {
      const { signal } = options;
      if (!qnaId) return false;
      if (!options.silent) setLoading(true);
      try {
        const detail = await fetchQnaDetail(qnaId, { signal });
        if (signal?.aborted) return false;
        if (detail?.current) {
          setQna(detail.current);
          setPrevNext({ prev: detail.prev, next: detail.next });
          setAnswerDraft(detail.current.answerContent || '');
          setEditingAnswer(false);
          setAnswerError('');
        } else {
          setQna({ notFound: true });
        }
        return true;
      } catch (err) {
        if (err.name === 'CanceledError' || err.name === 'AbortError') return false;
        console.error('Q&A 상세 로드 실패:', err?.message || err);
        if (!options.silent) {
          setErrorMessage(err?.message || CMMessage.MSG_ERR_005('Q&A'));
          setOpenError(true);
          setQna({ notFound: true });
        }
        return false;
      } finally {
        if (!options.silent && !signal?.aborted) {
          setLoading(false);
        }
      }
    },
    [qnaId]
  );

  useEffect(() => {
    if (!qnaId) {
      setErrorMessage(CMMessage.MSG_ERR_016);
      setOpenError(true);
      setQna({ notFound: true });
      setLoading(false);
      return undefined;
    }

    const controller = new AbortController();
    loadQnaDetail({ signal: controller.signal });

    return () => controller.abort();
  }, [qnaId, loadQnaDetail]);

  const statusInfo = useMemo(() => resolveQnaStatus(qna?.statusCode, qna?.statusValue), [qna]);

  const writerDisplay = useMemo(() => {
    if (!qna) return '';
    return qna.loginId || qna.writerName || (qna.userId ? `user-${qna.userId}` : '익명');
  }, [qna]);

  const canDelete = useMemo(() => {
    if (!qna || !user) return false;
    if (isAdmin) return true;
    return Number(user.userId) === Number(qna.userId);
  }, [isAdmin, qna, user]);

  if (loading) {
    return (
      <div className="container-fluid px-5 py-5">
        <h1 className="m-0">Q & A</h1>
        <div className="text-center py-5">불러오는 중입니다...</div>
      </div>
    );
  }

  if (!qna || qna.notFound) {
    return (
      <div className="container-fluid px-5 py-5">
        <h1 className="m-0">Q & A</h1>
        <div className="text-center py-5 text-danger">{CMMessage.MSG_EMPTY_010}</div>
        <CM_99_1003
          isOpen={openError}
          onClose={() => setOpenError(false)}
          errorMessage={errorMessage || CMMessage.MSG_ERR_017}
        />
      </div>
    );
  }

  const hasAnswer = Boolean(qna?.answerContent && qna.answerContent.trim());
  const canManageAnswer = isLoggedIn && isAdmin;
  const { prev, next } = prevNext;
  const createdDate = qna.createdAt?.split(' ')[0]?.replace(/-/g, '.');

  const handleAnswerEditStart = () => {
    setAnswerDraft(qna.answerContent || '');
    setEditingAnswer(true);
    setAnswerError('');
  };

  const handleAnswerCancel = () => {
    setAnswerDraft(qna.answerContent || '');
    setEditingAnswer(false);
    setAnswerError('');
  };

  const handleAnswerSubmit = async () => {
    const trimmed = answerDraft.trim();
      if (!trimmed) {
        setAnswerError(CMMessage.MSG_VAL_009);
        return;
      }
      if (!qna?.qnaId) {
        setAnswerError(CMMessage.MSG_ERR_006('Q&A'));
        return;
      }
    setSubmittingAnswer(true);
    setAnswerError('');
    try {
      await answerQna(qna.qnaId, trimmed);
      const refreshed = await loadQnaDetail({ silent: true });
      if (!refreshed) {
        setAnswerError(CMMessage.MSG_INF_012);
      } else {
        setEditingAnswer(false);
      }
    } catch (err) {
      const message =
        err?.response?.data?.message || err?.message || CMMessage.MSG_ERR_007('답변 저장');
      setAnswerError(message);
    } finally {
      setSubmittingAnswer(false);
    }
  };

  const renderAnswerContent = () => (
    <>
      <div className="text-muted small mb-2">
        {qna.answererLoginId || qna.answererName || '관리자'} /{' '}
        {qna.answerCreatedAt?.replace(/-/g, '.')}에 작성됨
      </div>
      <div className="text-primary mb-0 fs-6" style={{ whiteSpace: 'pre-line' }}>
        {qna.answerContent}
      </div>
    </>
  );

  const renderAnswerSection = () => {
    if (canManageAnswer) {
      return (
        <div className="border rounded p-3 bg-light">
          <div className="fw-semibold mb-2">답변</div>
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
                  {submittingAnswer ? '저장 중...' : '저장'}
                </button>
                <button
                  type="button"
                  className="btn btn-outline-secondary btn-sm"
                  onClick={handleAnswerCancel}
                  disabled={submittingAnswer}
                >
                  취소
                </button>
              </div>
            </>
          ) : hasAnswer ? (
            <>
              {renderAnswerContent()}
              <div className="d-flex justify-content-end gap-2 mt-3">
                <button
                  type="button"
                  className="btn btn-outline-primary btn-sm"
                  onClick={handleAnswerEditStart}
                >
                  답변 수정
                </button>
              </div>
            </>
          ) : (
            <div className="d-flex justify-content-between align-items-center">
              <span className="text-muted small">등록된 답변이 없습니다.</span>
              <button
                type="button"
                className="btn btn-outline-primary btn-sm"
                onClick={handleAnswerEditStart}
              >
                답변 작성
              </button>
            </div>
          )}
        </div>
      );
    }

    if (hasAnswer) {
      return (
        <div className="border rounded p-3 bg-light">
          <div className="fw-semibold mb-2">답변</div>
          {renderAnswerContent()}
        </div>
      );
    }

    return null;
  };

  const handleDelete = async () => {
    if (!qna?.qnaId) {
      setDeleteError(CMMessage.MSG_ERR_006('Q&A'));
      return;
    }
    setDeleting(true);
    setOpen1002(true);
    setDeleteError('');
    try {
      await deleteQna(qna.qnaId);
      if (typeof window !== 'undefined' && typeof window.CM_showToast === 'function') {
        window.CM_showToast('Q&A가 삭제되었습니다.');
      }
      setOpen1004(true);
    } catch (err) {
      const message = err?.response?.data?.message || err?.message || '삭제에 실패했습니다.';
      setDeleteError(message);
      setOpenError(true);
      setErrorMessage(message);
    } finally {
      setDeleting(false);
      setOpen1002(false);
    }
  };

  return (
    <>
      <h1>Q & A</h1>
      <div className="container-fluid px-3 px-md-5 py-4">
        <div className="border-top border-bottom py-3 px-2 d-flex justify-content-between fs-6 fs-sm-5">
          <div className="flex-grow-1">
            <span className={`badge ${statusInfo.badgeClass} fw-semibold me-2`}>
              {statusInfo.label}
            </span>
            {Number(qna.isPrivate) === 1 && <i className="bi bi-file-lock2 me-2"></i>}
            <span className="fw-bold">{qna.title}</span>
            <div className="d-flex d-sm-none justify-content-end text-muted small mt-2">
              <span className="me-2">{writerDisplay}</span>
              <span className="me-2">/ {createdDate ?? qna.createdAt}</span>
            </div>
          </div>
          <div className="d-none d-sm-flex small text-muted align-self-center">
            <span className="me-3">{writerDisplay}</span>
            <span className="me-3">{createdDate ?? qna.createdAt}</span>
          </div>
        </div>
        {deleteError && <div className="text-danger small mt-2">{deleteError}</div>}
        <div className="p-3" style={{ whiteSpace: 'pre-line' }}>
          {qna.content}
        </div>
        {renderAnswerSection()}
        <div className="border-top fs-5 mt-4">
          <div className="d-flex text-muted small border-bottom py-2">
            <span className="d-flex align-items-center gap-1">
              이전글<i className="bi bi-caret-up-fill"></i>
            </span>
            {prev ? (
              <span
                className="fs-6 ms-3 text-primary hover-underline text-decoration-underline-hover"
                style={{ cursor: 'pointer' }}
                onClick={() => navigate(`/qna/${prev.qnaId}`)}
              >
                {prev.title}
              </span>
            ) : (
              <span className="fs-6 ms-3 text-muted">이전글 내용이 없습니다.</span>
            )}
          </div>
          <div className="d-flex small py-2">
            <span className="d-flex align-items-center gap-1">
              다음글<i className="bi bi-caret-down-fill"></i>
            </span>
            {next ? (
              <span
                className="fs-6 ms-3 text-primary hover-underline text-decoration-underline-hover"
                style={{ cursor: 'pointer' }}
                onClick={() => navigate(`/qna/${next.qnaId}`)}
              >
                {next.title}
              </span>
            ) : (
              <span className="fs-6 ms-3 text-muted">다음글 내용이 없습니다.</span>
            )}
          </div>
        </div>

        <div className="d-flex flex-column justify-content-center align-items-center mt-4 gap-2">
          <div className="d-flex justify-content-center">
            <button className="btn btn-secondary px-5" onClick={() => navigate('/qna')}>
              목록
            </button>
            {canDelete && (
              <div className="ms-3 d-flex align-items-center">
                <button
                  type="button"
                  className="btn btn-outline-danger px-5"
                  onClick={() => setOpenConfirmDelete(true)}
                  disabled={deleting}
                >
                  {deleting ? '삭제 중...' : '삭제'}
                </button>
              </div>
            )}
          </div>
        </div>
      </div>
      <CM_99_1001
        isOpen={openConfirmDelete}
        onClose={() => setOpenConfirmDelete(false)}
        onConfirm={() => {
          setOpenConfirmDelete(false);
          handleDelete();
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
        isOpen={openError}
        onClose={() => setOpenError(false)}
        errorMessage={errorMessage || CMMessage.MSG_ERR_017}
      />
      <CM_99_1004
        isOpen={open1004}
        onClose={() => {
          setOpen1004(false);
          navigate('/qna');
        }}
        Message={popupMessage || CMMessage.MSG_INF_005}
      />
    </>
  );
}
