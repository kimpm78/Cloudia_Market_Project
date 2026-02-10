import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';

import CM_99_1000 from '../components/commonPopup/CM_99_1000.jsx';
import CM_99_1001 from '../components/commonPopup/CM_99_1001.jsx';
import CM_99_1002 from '../components/commonPopup/CM_99_1002.jsx';
import CM_99_1004 from '../components/commonPopup/CM_99_1004.jsx';
import '../styles/CM_04_1001.css';
import '../components/editor/toolbar.css';

import { useAuth } from '../contexts/AuthContext.jsx';
import { fetchReviewDetail, deleteReview, increaseReviewView } from '../services/ReviewService.js';
import {
  fetchComments,
  createComment,
  deleteComment,
  updateComment,
} from '../services/CommentService.js';
import { formatYearMonthDot } from '../utils/DateFormat.js';
import CMMessage from '../constants/CMMessage';

const findCommentById = (list = [], targetId) => {
  for (const item of list) {
    if (item.commentId === targetId) {
      return item;
    }
    if (item.children && item.children.length > 0) {
      const found = findCommentById(item.children, targetId);
      if (found) {
        return found;
      }
    }
  }
  return null;
};

const getTotalCommentCount = (list = []) =>
  list.reduce((sum, item) => {
    const childrenCount =
      item.children && item.children.length > 0 ? getTotalCommentCount(item.children) : 0;
    return sum + 1 + childrenCount;
  }, 0);

export default function CM_04_1001() {
  const { id: reviewId } = useParams();
  const navigate = useNavigate();
  const { isLoggedIn, user } = useAuth();
  const [review, setReview] = useState(null);
  const [comments, setComments] = useState([]);
  const [open1000, setOpen1000] = useState(false);
  const [open1002, setOpen1002] = useState(false);
  const [open1004, setOpen1004] = useState(false);
  const [popupMessage, setPopupMessage] = useState('');
  const [newComment, setNewComment] = useState('');

  const [editing, setEditing] = useState({});
  const [editDrafts, setEditDrafts] = useState({});
  const [replyDrafts, setReplyDrafts] = useState({});
  const [openReplyBox, setOpenReplyBox] = useState({});
  const [openConfirmDelete, setOpenConfirmDelete] = useState(false);
  const [openConfirmCommentDelete, setOpenConfirmCommentDelete] = useState(false);
  const [targetCommentId, setTargetCommentId] = useState(null);

  const isReviewOwner = user?.userId === review?.userId;

  useEffect(() => {
    const controller = new AbortController();
    let isMounted = true;
    fetchReviewDetail(reviewId, { signal: controller.signal })
      .then((data) => {
        if (!isMounted) return;
        if (data?.resultList) {
          setReview(data.resultList);

          increaseReviewView(reviewId)
            .then((res) => {
              if (!isMounted) return;
              if (res?.result && res.resultList === true) {
                setReview((prev) =>
                  prev ? { ...prev, viewCount: (prev.viewCount ?? 0) + 1 } : prev
                );
              }
            })
            .catch((err) => {
              console.error('閲覧数増加失敗:', err?.response?.data?.message || err.message);
            });

          fetchComments(reviewId)
            .then((list) => {
              if (!isMounted) return;
              if (Array.isArray(list)) {
                setComments(list);
              } else {
                setComments([]);
              }
            })
            .catch((err) => {
              console.error('コメント取得失敗:', err?.response?.data?.message || err.message);
              if (isMounted) {
                setComments([]);
              }
            });
        }
      })
      .catch((err) => {
        if (err.name !== 'CanceledError') {
          console.error('レビュー詳細読み込み失敗:', err?.response?.data?.message || err.message);
        }
      });
    return () => {
      isMounted = false;
      controller.abort();
    };
  }, [reviewId]);

  useEffect(() => {
    const container = document.querySelector('.review-content');
    if (container && review?.content) {
      const images = container.querySelectorAll('img');
      images.forEach((img) => {
        img.classList.add('w-100');
        img.style.height = 'auto';
      });
    }
  }, [review?.content]);

  useEffect(() => {
    const checkboxes = document.querySelectorAll('input[type="checkbox"]');
    checkboxes.forEach((cb) => {
      cb.setAttribute('onclick', 'return false;');
      cb.setAttribute('tabindex', '-1');
      cb.style.pointerEvents = 'none';
    });
  }, []);

  if (!review) {
    return <div className="text-center mt-5">{CMMessage.MSG_EMPTY_009}</div>;
  }

  const clearEditing = (commentId) => {
    setEditing((prev) => {
      const copy = { ...prev };
      delete copy[commentId];
      return copy;
    });
    setEditDrafts((prev) => {
      const copy = { ...prev };
      delete copy[commentId];
      return copy;
    });
  };

  const startEdit = (comment) => {
    setEditing((prev) => ({ ...prev, [comment.commentId]: true }));
    setEditDrafts((prev) => ({ ...prev, [comment.commentId]: comment.content ?? '' }));
  };

  const cancelEdit = (commentId) => {
    clearEditing(commentId);
  };

  const saveEdit = (commentId) => {
    const content = (editDrafts[commentId] || '').trim();
    if (!content) {
      alert(CMMessage.MSG_VAL_001);
      return;
    }
    const body = {
      reviewId: Number(reviewId),
      commentId: Number(commentId),
      userId: Number(user?.userId),
      content: content,
    };
    updateComment(reviewId, commentId, body)
      .then(() => {
        fetchComments(reviewId)
          .then((list) => {
            if (Array.isArray(list)) {
              setComments(list);
            } else {
              setComments([]);
            }
            clearEditing(commentId);
          })
          .catch((err) => {
            console.error('コメント一覧読み込み失敗:', err?.response?.data?.message || err.message);
          });
      })
      .catch((err) => {
        console.error('コメント編集失敗:', err?.response?.data?.message || err.message);
      });
  };

  const handleDeleteComment = (commentId) => {
    if (!user?.userId) {
      console.error('ログインが必要です');
      return;
    }

    const body = { userId: Number(user.userId) };

    deleteComment(reviewId, commentId, body)
      .then(() => {
        fetchComments(reviewId)
          .then((list) => setComments(Array.isArray(list) ? list : []))
          .catch((err) => console.error('コメント一覧更新失敗:', err));
      })
      .catch((err) => {
        console.error('コメント削除失敗:', err?.response?.data?.message || err.message);
      });
  };

  // 返信コメントを投稿
  const handleCreateReply = (parentId) => {
    if (!isLoggedIn) {
      setOpen1000(true);
      return;
    }

    const parentComment = findCommentById(comments, parentId);
    if (parentComment && Number(parentComment.userId) === Number(user?.userId)) {
      return;
    }

    const content = (replyDrafts[parentId] || '').trim();
    if (!content) {
      alert(CMMessage.MSG_VAL_001);
      return;
    }

    const body = {
      reviewId: Number(reviewId),
      userId: Number(user?.userId),
      parentCommentId: parentId ?? null,
      content: content,
      createdBy: user?.loginId ?? user?.userName ?? user?.name ?? 'user',
    };

    createComment(reviewId, body)
      .then(() => {
        fetchComments(reviewId)
          .then((list) => {
            if (Array.isArray(list)) {
              setComments(list);
            } else {
              setComments([]);
            }
            setReplyDrafts((prev) => {
              const copy = { ...prev };
              delete copy[parentId];
              return copy;
            });
            setOpenReplyBox((prev) => {
              const copy = { ...prev };
              copy[parentId] = false;
              return copy;
            });
          })
          .catch((err) => {
            console.error('コメント一覧読み込み失敗:', err?.response?.data?.message || err.message);
          });
      })
      .catch((err) => {
        console.error('コメント登録失敗:', err?.response?.data?.message || err.message);
        if (err?.response?.status === 401) setOpen1000(true);
      });
  };

  // 最上位コメントを投稿
  const handleCreateRootComment = () => {
    if (!isLoggedIn) {
      setOpen1000(true);
      return;
    }
    const content = (newComment || '').trim();
    if (!content) return;

    const body = {
      reviewId: Number(reviewId),
      userId: Number(user?.userId),
      parentCommentId: null,
      content: content,
      createdBy: user?.loginId ?? user?.userName ?? user?.name ?? 'user',
    };

    createComment(reviewId, body)
      .then(() => {
        fetchComments(reviewId)
          .then((list) => {
            if (Array.isArray(list)) {
              setComments(list);
            } else {
              setComments([]);
            }
            setNewComment('');
          })
          .catch((err) => {
            console.error('댓글 목록 로드 실패:', err?.response?.data?.message || err.message);
          });
      })
      .catch((err) => {
        console.error('댓글 등록 실패:', err?.response?.data?.message || err.message);
        if (err?.response?.status === 401) setOpen1000(true);
      });
  };

  const renderComment = (comment, depth = 0, parent = null) => {
    const isOwner =
      !!user && (user.userId === comment.userId || user.loginId === comment.createdBy);
    const isEdit = !!editing[comment.commentId];
    const isSelfComment = Number(user?.userId) === Number(comment.userId);
    const canReply = isLoggedIn && depth < 2 && !isSelfComment;
    const parentAuthorLabel =
      parent && parent.createdBy
        ? parent.userId === review?.userId
          ? '作成者'
          : parent.createdBy
        : null;

    return (
      <div key={comment.commentId}>
        <div className={`comment-box ${comment.parentCommentId ? 'comment-reply' : ''}`}>
          <div className="d-flex justify-content-between align-items-center">
            <div>
              <strong>{comment.userId === review?.userId ? '作成者' : comment.createdBy}</strong>
              <span className="text-muted ps-2">{formatYearMonthDot(comment.createdAt)}</span>
            </div>
            <div className="d-flex gap-2">
              {canReply && (
                <button
                  className="btn btn-link btn-sm p-0"
                  onClick={() => {
                    if (!canReply) return;
                    setOpenReplyBox((prev) => ({
                      ...prev,
                      [comment.commentId]: !prev[comment.commentId],
                    }));
                  }}
                >
                  返信
                </button>
              )}
              {isOwner && !isEdit && (
                <button className="btn btn-link btn-sm p-0" onClick={() => startEdit(comment)}>
                  修正
                </button>
              )}
              {isOwner && (
                <button
                  className="btn btn-link btn-sm text-danger p-0"
                  onClick={() => {
                    setTargetCommentId(comment.commentId);
                    setOpenConfirmCommentDelete(true);
                  }}
                >
                  削除
                </button>
              )}
            </div>
          </div>

          {!isEdit ? (
            <div className="mt-1">
              {parentAuthorLabel && <span className="text-primary me-2">@{parentAuthorLabel}</span>}
              {comment.content}
            </div>
          ) : (
            <div className="mt-2">
              <textarea
                className="form-control mb-2"
                rows={3}
                value={editDrafts[comment.commentId] ?? ''}
                onChange={(e) =>
                  setEditDrafts((prev) => ({ ...prev, [comment.commentId]: e.target.value }))
                }
              />
              <div className="d-flex gap-2">
                <button
                  className="btn btn-primary btn-sm"
                  onClick={() => saveEdit(comment.commentId)}
                >
                  格納
                </button>
                <button
                  className="btn btn-secondary btn-sm"
                  onClick={() => cancelEdit(comment.commentId)}
                >
                  取消
                </button>
              </div>
            </div>
          )}

          {canReply && openReplyBox[comment.commentId] && (
            <div className="mt-3">
              <textarea
                className="form-control mb-2"
                rows={3}
                placeholder="답글을 입력하세요."
                value={replyDrafts[comment.commentId] ?? ''}
                onChange={(e) =>
                  setReplyDrafts((prev) => ({ ...prev, [comment.commentId]: e.target.value }))
                }
              />
              <div className="d-flex gap-2">
                <button
                  className="btn btn-outline-primary btn-sm"
                  onClick={() => handleCreateReply(comment.commentId)}
                >
                  등록
                </button>
                <button
                  className="btn btn-outline-secondary btn-sm"
                  onClick={() =>
                    setOpenReplyBox((prev) => ({ ...prev, [comment.commentId]: false }))
                  }
                >
                  닫기
                </button>
              </div>
            </div>
          )}
        </div>
        {comment.children && comment.children.length > 0 && (
          <div className="ms-3">
            {comment.children.map((child) => renderComment(child, depth + 1, comment))}
          </div>
        )}
      </div>
    );
  };

  return (
    <div className="container mb-5">
      <h1 style={{ margin: '50px 0' }}>レビュー/口コミ</h1>
      <div className="border-top border-dark border-2 pt-1 mb-3">
        <div
          className="d-flex justify-content-between align-items-center small px-1"
          style={{ padding: '10px 20px', fontSize: '16px' }}
        >
          <div className="d-flex align-items-center gap-2">
            <span className="text-primary fw-bold">
              {review.reviewType === 0 ? '[レビュー]' : review.reviewType === 1 ? '[口コミ]' : ''}
            </span>
            <span className="fw-bold">{review.title}</span>
          </div>
          <div className="d-none d-sm-flex small text-muted align-self-center">
            <span className="me-2">{review.createdBy}</span>
            <span className="me-2">{formatYearMonthDot(review.createdAt)}</span>
            <span>조회: {review.viewCount}</span>
          </div>
        </div>
        <hr className="mt-1 mb-0" />
      </div>
      <div
        className="border p-4 tiptap review-content"
        dangerouslySetInnerHTML={{ __html: review?.content }}
        style={{ width: '100%' }}
      />
      <hr className="my-3" />
      <h6 className="border-bottom pb-3 mb-3">コメント | {getTotalCommentCount(comments)}個</h6>
      {Array.isArray(comments) && comments.length > 0 ? (
        comments.map((c) => renderComment(c))
      ) : (
        <div className="text-center text-muted my-4">登録されたコメントはありません。</div>
      )}

      <h6 className="mt-5">コメント</h6>
      <textarea
        className="form-control mb-3"
        rows="5"
        placeholder={isLoggedIn ? 'コメントを入力してください。' : 'ログインすると利用できます。'}
        disabled={!isLoggedIn}
        value={newComment}
        onChange={(e) => setNewComment(e.target.value)}
      />
      <div className="d-flex flex-column gap-5 review-action-wrap">
        <div className="d-flex justify-content-end review-action-primary">
          <button
            className="btn btn-primary px-4"
            onClick={() => {
              if (!isLoggedIn) {
                setOpen1000(true);
              } else {
                handleCreateRootComment();
              }
            }}
          >
            作成する
          </button>
        </div>
        <div className="d-flex justify-content-center gap-2 review-action-secondary">
          {isLoggedIn && isReviewOwner && (
            <button
              className="btn btn-warning px-4"
              onClick={() => navigate(`/review/edit/${reviewId}`)}
            >
              編集する
            </button>
          )}
          <button className="btn btn-secondary px-4" onClick={() => navigate('/review')}>
            一覧に戻る
          </button>
          {isLoggedIn && (isReviewOwner || user?.roleId <= 2) && (
            <button
              className="btn btn-outline-danger px-4"
              onClick={() => setOpenConfirmDelete(true)}
            >
              削除する
            </button>
          )}
        </div>
      </div>
      <CM_99_1000 isOpen={open1000} onClose={() => setOpen1000(false)} />
      <CM_99_1001
        isOpen={openConfirmDelete}
        onClose={() => setOpenConfirmDelete(false)}
        onConfirm={() => {
          setOpenConfirmDelete(false);
          setOpen1002(true); // ローディングポップアップ表示

          deleteReview(reviewId)
            .then((res) => {
              if (res?.result) {
                setOpen1002(false); // ローディング終了
                setPopupMessage(CMMessage.MSG_INF_005);
                setOpen1004(true); // 完了ポップアップ
              } else {
                setOpen1002(false);
                alert(res?.message || CMMessage.MSG_ERR_007('削除'));
              }
            })
            .catch((err) => {
              setOpen1002(false);
              console.error('レビュー削除中にエラー:', err?.response?.data?.message || err.message);
            });
        }}
        Message={CMMessage.MSG_CON_003}
      />
      <CM_99_1001
        isOpen={openConfirmCommentDelete}
        onClose={() => setOpenConfirmCommentDelete(false)}
        onConfirm={() => {
          if (targetCommentId) {
            handleDeleteComment(targetCommentId);
          }
          setOpenConfirmCommentDelete(false);
          setTargetCommentId(null);
        }}
        Message={CMMessage.MSG_CON_003}
      />
      <CM_99_1002
        isOpen={open1002}
        onClose={() => setOpen1002(false)}
        duration={3000}
        hideClose={true}
      />

      <CM_99_1004
        isOpen={open1004}
        onClose={() => {
          setOpen1004(false);
          navigate('/review');
        }}
        Message={popupMessage || CMMessage.MSG_INF_005}
      />
    </div>
  );
}
