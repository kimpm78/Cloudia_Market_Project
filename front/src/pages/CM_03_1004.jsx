import { useState, useEffect, useRef, useMemo } from 'react';
import { useParams } from 'react-router-dom';

import CM_03_1004_QnaItem from '../components/CM_03_1004_QnaItem';
import CM_03_1004_ReviewSection from '../components/CM_03_1004_ReviewSection';
import CM_99_1000 from '../components/commonPopup/CM_99_1000';
import CM_99_1004 from '../components/commonPopup/CM_99_1004';

import axiosInstance from '../services/axiosInstance';
import { useAuth } from '../contexts/AuthContext';
import { fetchReviews } from '../services/ReviewService';
import { fetchRecentQna } from '../services/QnaService';

import { useKeenSlider } from 'keen-slider/react';
import Lightbox from 'yet-another-react-lightbox';

import noImage from '../images/common/CM-NoImage.png';
import 'yet-another-react-lightbox/styles.css';
import 'keen-slider/keen-slider.min.css';
import '../components/editor/toolbar.css';
import '../styles/CM_03_1004.css';

import { hasReservationClosed, getReservationDeadlineDate } from '../utils/productStatus';
import { buildProductImages } from '../utils/productImages';
import { formatDateTime, formatYearMonthDot } from '../utils/DateFormat';
import CMMessage from '../constants/CMMessage';

export default function CM_03_1004() {
  const { type, id } = useParams();
  const { isLoggedIn, user } = useAuth();
  const userId = user?.userId ?? null;
  const [mainImage, setMainImage] = useState('');
  const [lightboxOpen, setLightboxOpen] = useState(false);
  const [lightboxIndex, setLightboxIndex] = useState(0);
  const [product, setProduct] = useState(null);
  const [selectedQuantity, setSelectedQuantity] = useState(1);
  const [qnaOpen, setQnaOpen] = useState(null);
  const [open1000, setOpen1000] = useState(false);
  const [openCartModal, setOpenCartModal] = useState(false);
  const [cartMessage, setCartMessage] = useState('');
  const [reviews, setReviews] = useState([]);
  const [qnaList, setQnaList] = useState([]);
  const [qnaLoading, setQnaLoading] = useState(false);
  const [qnaError, setQnaError] = useState('');
  const toggleQna = (index) => setQnaOpen(qnaOpen === index ? null : index);

  const showRestrictionMessage = (message) => {
    setCartMessage(message);
    setOpenCartModal(true);
  };

  const isReservationClosed = useMemo(() => hasReservationClosed(product), [product]);
  const reservationDeadlineDate = useMemo(() => getReservationDeadlineDate(product), [product]);
  const reservationDeadlineValue =
    product?.reservationDeadline ?? product?.reservation_deadline ?? null;

  const isReservationSale = useMemo(() => {
    const codeValue = product?.codeValue ?? product?.code_value;
    const code = codeValue !== undefined && codeValue !== null ? String(codeValue).trim() : '';
    if (code === '3' || code === '4') return true;
    return Boolean(product?.reservationDeadline ?? product?.reservation_deadline);
  }, [product]);

  const formatDateShort = formatYearMonthDot;

  const canViewQnaItem = (item) => {
    const isPrivate = Number(item?.isPrivate ?? 0) === 1;
    if (!isPrivate) return true;
    if (!isLoggedIn) return false;
    if (user?.roleId && user.roleId <= 2) return true;
    return Number(user?.userId) === Number(item?.userId);
  };

  const { remainingQty, soldOut, soldOutReason } = useMemo(() => {
    if (!product) {
      return { remainingQty: null, soldOut: false, soldOutReason: null };
    }

    if (isReservationClosed) {
      return {
        remainingQty: 0,
        soldOut: true,
        soldOutReason: CMMessage.MSG_NOTICE_001,
      };
    }
    if (product?.isSoldOut === true) {
      return { remainingQty: 0, soldOut: true, soldOutReason: CMMessage.MSG_ERR_032 };
    }

    const codeValue = product?.codeValue ?? product?.code_value;
    const code = codeValue !== undefined && codeValue !== null ? String(codeValue).trim() : '';

    // 코드일람(003) 기준: 2(품절), 4(예약 마감)만 SOLD OUT 처리
    if (code === '2') {
      return { remainingQty: 0, soldOut: true, soldOutReason: CMMessage.MSG_ERR_032 };
    }
    if (code === '4') {
      return { remainingQty: 0, soldOut: true, soldOutReason: CMMessage.MSG_NOTICE_002 };
    }

    const availableQty = typeof product?.availableQty === 'number' ? product.availableQty : null;
    if (availableQty === null) {
      return { remainingQty: null, soldOut: false, soldOutReason: null };
    }

    if (availableQty <= 0) {
      return { remainingQty: 0, soldOut: true, soldOutReason: CMMessage.MSG_ERR_032 };
    }

    return { remainingQty: availableQty, soldOut: false, soldOutReason: null };
  }, [product, isReservationClosed]);

  const maxPurchaseLimit = useMemo(() => {
    const raw = product?.purchaseLimit ?? product?.purchase_limit;
    const parsed = Number(raw);
    if (!Number.isFinite(parsed) || parsed <= 0) return 1;
    return Math.max(1, Math.floor(parsed));
  }, [product]);

  useEffect(() => {
    if (remainingQty !== null) {
      if (remainingQty <= 0) {
        if (selectedQuantity !== 0) {
          setSelectedQuantity(0);
        }
      } else {
        const limit = Math.min(remainingQty, maxPurchaseLimit);
        if (selectedQuantity > limit || selectedQuantity <= 0) {
          setSelectedQuantity(limit);
        }
      }
    } else if (selectedQuantity > maxPurchaseLimit) {
      setSelectedQuantity(maxPurchaseLimit);
    }
  }, [remainingQty, selectedQuantity, maxPurchaseLimit]);

  const quantityOptions = useMemo(() => {
    if (remainingQty !== null) {
      const limit = Math.min(Math.max(remainingQty, 0), maxPurchaseLimit);
      if (limit > 0) {
        return Array.from({ length: limit }, (_, idx) => idx + 1);
      }
      return [];
    }
    return Array.from({ length: maxPurchaseLimit }, (_, idx) => idx + 1);
  }, [remainingQty, maxPurchaseLimit]);

  const statusOverlayLabel = useMemo(() => {
    if (isReservationClosed) return '마감';
    if (soldOut) return 'SOLD OUT';
    return null;
  }, [isReservationClosed, soldOut]);

  const purchaseDeadlineLabel = useMemo(() => {
    if (!isReservationSale) {
      return '상시판매';
    }
    if (!reservationDeadlineValue) {
      return '-';
    }
    const raw =
      typeof reservationDeadlineValue === 'string'
        ? reservationDeadlineValue
        : reservationDeadlineValue instanceof Date
          ? reservationDeadlineValue.toISOString()
          : String(reservationDeadlineValue);

    if (!reservationDeadlineDate) {
      const formatted = formatDateTime(raw) || raw;
      return isReservationClosed ? `마감 (${formatted})` : formatted;
    }

    const formatted = formatDateTime(reservationDeadlineDate);
    return isReservationClosed ? `마감 (${formatted})` : formatted;
  }, [isReservationSale, reservationDeadlineValue, reservationDeadlineDate, isReservationClosed]);

  const estimatedDeliveryLabel = useMemo(() => {
    if (!product) return '-';
    if (isReservationSale) {
      return formatYearMonthDot(product?.releaseDate) || '-';
    }
    return '5일 내 배송';
  }, [product, isReservationSale]);

  const descRef = useRef(null);
  const detailRef = useRef(null);
  const reviewRef = useRef(null);
  const qnaRef = useRef(null);

  const [sliderRef, instanceRef] = useKeenSlider({
    slides: {
      perView: 'auto',
      spacing: 16,
    },
    drag: true,
    mode: 'free',
  });

  const scrollPrev = () => instanceRef.current?.prev();
  const scrollNext = () => instanceRef.current?.next();

  useEffect(() => {
    let cancelled = false;

    window.scrollTo({ top: 0, behavior: 'smooth' });

    const extractProductItem = (data) => {
      if (!data || typeof data !== 'object') return null;

      if (data.resultList) {
        if (Array.isArray(data.resultList)) return data.resultList[0] || null;
        return data.resultList;
      }

      if (data.data) return data.data;
      if (data.product) return data.product;

      if (data.productId || data.productCode) return data;
      return null;
    };

    const load = async () => {
      try {
        const res = await axiosInstance.get(
          type === 'pre-order' ? `/guest/pre-order/${id}` : `/guest/new-product/${id}`
        );

        const item = extractProductItem(res?.data);

        const baseUrl = import.meta.env.VITE_API_BASE_IMAGE_URL;
        const thumbs = buildProductImages(item, { baseUrl, limit: 20 });
        const thumbnails = thumbs && thumbs.length ? thumbs : [{ thumb: noImage, full: noImage }];
        const mainImg = (thumbnails[0] && thumbnails[0].full) || noImage;

        const normalizedProductId =
          item.productId ?? item.product_id ?? item.productCode ?? item.product_code ?? null;
        const productIdForUse =
          normalizedProductId !== null && normalizedProductId !== undefined
            ? String(normalizedProductId).trim()
            : null;

        const availableQtyRaw =
          item.availableQty ?? item.available_quantity ?? item.stock ?? item.quantity;
        const availableQty =
          typeof availableQtyRaw === 'number' && !Number.isNaN(availableQtyRaw)
            ? availableQtyRaw
            : null;

        const enriched = {
          ...item,
          productId: productIdForUse ?? item.productId,
          availableQty,
          thumbnails,
        };

        if (!cancelled) {
          setProduct(enriched);
          setMainImage(mainImg);
        }

        // Reviews
        if (enriched?.productId) {
          try {
            const allReviews = await fetchReviews();
            const filtered = Array.isArray(allReviews)
              ? allReviews.filter((r) => String(r.productId) === String(enriched.productId))
              : [];
            if (!cancelled) setReviews(filtered);
          } catch (err) {
            console.error('리뷰 조회 실패:', err);
            if (!cancelled) setReviews([]);
          }
        } else if (!cancelled) {
          setReviews([]);
        }

        // Q&A
        if (!enriched?.productId) {
          if (!cancelled) {
            setQnaList([]);
            setQnaError('');
            setQnaLoading(false);
          }
        } else {
          if (!cancelled) {
            setQnaLoading(true);
            setQnaError('');
          }

          try {
            const list = await fetchRecentQna({ size: 5, productId: enriched.productId });
            const safeList = Array.isArray(list) ? list : [];

            const mapped = safeList.map((item) => {
              const visible = canViewQnaItem(item);
              const statusLabel =
                item?.statusLabel || (item?.statusValue === 2 ? '답변 완료' : '질문');
              const baseName = item?.loginId || item?.writerName || `user-${item?.userId ?? ''}`;
              return {
                qnaId: item?.qnaId,
                status: statusLabel,
                question: visible ? item?.title : '비밀글 입니다.',
                detail: visible ? item?.content : '',
                answer: visible ? item?.answerContent || '' : '',
                answerDate: visible ? formatDateShort(item?.answerCreatedAt) : '',
                name: visible ? baseName : '비공개',
                date: formatDateShort(item?.createdAt),
                secret: !visible,
              };
            });

            if (!cancelled) setQnaList(mapped);
          } catch (err) {
            console.error('상품 Q&A 조회 실패:', err);
            if (!cancelled) {
              setQnaError(CMMessage.MSG_ERR_005('상품 Q&A'));
              setQnaList([]);
            }
          } finally {
            if (!cancelled) setQnaLoading(false);
          }
        }

        requestAnimationFrame(() => {
          if (!cancelled) instanceRef.current?.update();
        });
      } catch (err) {
        console.error('상품 상세 정보 불러오기 실패:', err?.response?.data?.message || err.message);
        if (!cancelled) {
          setProduct({ thumbnails: [{ thumb: noImage, full: noImage }] });
          setMainImage(noImage);
          setReviews([]);
          setQnaList([]);
          setQnaError('');
          setQnaLoading(false);
        }
      }
    };

    load();

    return () => {
      cancelled = true;
    };
  }, [type, id, isLoggedIn, userId]);
  const refreshCartCount = async ({ source } = {}) => {
    if (!userId) return;
    try {
      const [countResResult, createdAtResResult] = await Promise.allSettled([
        axiosInstance.get('/guest/menus/cart/count', { params: { userId } }),
        axiosInstance.get('/guest/cart/created-at', { params: { userId } }),
      ]);
      const countRes = countResResult.status === 'fulfilled' ? countResResult.value : null;
      if (!countRes) return;
      const parsedCount = Number(countRes.data);
      if (!Number.isFinite(parsedCount)) return;

      const createdAtRes =
        createdAtResResult.status === 'fulfilled' ? createdAtResResult.value : null;
      const createdAtEpoch =
        createdAtRes?.data?.resultList?.createdAtEpoch ??
        createdAtRes?.data?.data?.createdAtEpoch ??
        createdAtRes?.data?.createdAtEpoch ??
        null;
      const createdAtEpochValue =
        createdAtEpoch === null || createdAtEpoch === undefined ? null : Number(createdAtEpoch);
      const resolvedCreatedAtEpoch = Number.isFinite(createdAtEpochValue)
        ? createdAtEpochValue
        : null;
      const now = Date.now();
      window.dispatchEvent(
        new CustomEvent('cart:updated', {
          detail: {
            count: parsedCount,
            createdAtEpoch: resolvedCreatedAtEpoch,
            lastUpdated: now,
            source: source || 'add-to-cart',
          },
        })
      );
    } catch (err) {
      console.error('장바구니 개수 갱신 실패:', err?.response?.data || err.message);
    }
  };

  const addToCart = async ({ reservation }) => {
    if (!isLoggedIn) {
      setOpen1000(true);
      return;
    }

    if (!userId) {
      showRestrictionMessage(CMMessage.MSG_ERR_011);
      return;
    }

    const productId = product?.productId ?? null;
    if (!productId) {
      showRestrictionMessage(CMMessage.MSG_ERR_008);
      return;
    }

    if (selectedQuantity > maxPurchaseLimit) {
      showRestrictionMessage(CMMessage.MSG_LIMIT_001(maxPurchaseLimit));
      return;
    }

    if (selectedQuantity <= 0) {
      showRestrictionMessage(CMMessage.MSG_ERR_009);
      return;
    }

    try {
      const res = await axiosInstance.post('/guest/cart/add', {
        userId,
        productId,
        quantity: selectedQuantity,
        reservation,
      });

      const ok =
        res.status === 200 && (res.data?.result === undefined ? true : Boolean(res.data?.result));

      if (!ok) {
        showRestrictionMessage(res.data?.message || CMMessage.MSG_ERR_010);
        return;
      }

      setCartMessage(reservation ? CMMessage.MSG_INF_010 : CMMessage.MSG_INF_011);
      setOpenCartModal(true);
      await refreshCartCount({ source: 'add-to-cart' });
    } catch (err) {
      const status = err?.response?.status;
      const message = err?.response?.data?.message || err.message;
      if (status === 400 || status === 409) {
        showRestrictionMessage(message || CMMessage.MSG_LIMIT_001(10));
        return;
      }
      showRestrictionMessage(message || CMMessage.MSG_ERR_010);
    }
  };

  const handleReserveClick = () => addToCart({ reservation: true });

  const lightboxSlides = useMemo(() => {
    const thumbs = Array.isArray(product?.thumbnails) ? product.thumbnails : [];
    const slides = thumbs.map((t) => ({ src: t?.full || t?.thumb })).filter((s) => Boolean(s?.src));

    if (!slides.length) {
      return [{ src: mainImage || noImage }];
    }
    return slides;
  }, [product?.thumbnails, mainImage]);

  // 이미지 라이트박스 열기
  const openLightboxAt = (url) => {
    const idx = lightboxSlides.findIndex((s) => s.src === url);
    setLightboxIndex(Math.max(0, idx));
    setLightboxOpen(true);
  };

  return (
    <>
      <section className="product-detail">
        {/* 왼쪽: 큰 이미지 + 썸네일 */}
        <div className="product-detail-media">
          <figure className="product-detail-image">
            {statusOverlayLabel && (
              <div
                className={`product-detail-status ${isReservationClosed ? 'closed' : 'sold-out'}`}
                aria-hidden="true"
              >
                <span>{statusOverlayLabel}</span>
              </div>
            )}
            <img
              src={mainImage || noImage}
              className="product-detail-image-main"
              alt="디테일 이미지"
              role="button"
              tabIndex={0}
              onClick={() => openLightboxAt(mainImage || noImage)}
              onKeyDown={(e) => {
                if (e.key === 'Enter' || e.key === ' ') {
                  e.preventDefault();
                  openLightboxAt(mainImage || noImage);
                }
              }}
              onContextMenu={(e) => e.preventDefault()}
              onError={(e) => {
                e.currentTarget.src = noImage;
              }}
            />
            <button
              type="button"
              className="product-detail-zoom"
              aria-label="이미지 확대 보기"
              onClick={(e) => {
                e.stopPropagation();
                openLightboxAt(mainImage || noImage);
              }}
            >
              <i className="bi bi-zoom-in" aria-hidden="true"></i>
            </button>
          </figure>

          <div className="product-detail-thumbs">
            <button className="product-detail-thumb-nav left" onClick={scrollPrev}>
              <i className="bi bi-chevron-left"></i>
            </button>
            <button className="product-detail-thumb-nav right" onClick={scrollNext}>
              <i className="bi bi-chevron-right"></i>
            </button>
            <section ref={sliderRef} className="product-detail-thumb-list keen-slider">
              {product?.thumbnails && product.thumbnails.length > 0 ? (
                product.thumbnails.map(({ thumb, full }, index) => (
                  <div className="product-detail-thumb-slide keen-slider__slide" key={index}>
                    <div className="product-detail-thumb">
                      <img
                        src={thumb}
                        alt={`썸네일 ${index}`}
                        className={`product-detail-thumb-img ${mainImage === full ? 'is-active' : ''}`}
                        draggable={false}
                        onDragStart={(e) => e.preventDefault()}
                        onClick={() => setMainImage(full)}
                        onContextMenu={(e) => e.preventDefault()}
                        onError={(e) => {
                          e.currentTarget.src = noImage;
                        }}
                      />
                    </div>
                  </div>
                ))
              ) : mainImage ? (
                <div className="product-detail-thumb-slide keen-slider__slide">
                  <div className="product-detail-thumb">
                    <img
                      src={mainImage}
                      alt="썸네일"
                      className="product-detail-thumb-img is-active"
                      draggable={false}
                      onDragStart={(e) => e.preventDefault()}
                      onClick={() => setMainImage(mainImage)}
                      onContextMenu={(e) => e.preventDefault()}
                      onError={(e) => {
                        e.currentTarget.src = noImage;
                      }}
                    />
                  </div>
                </div>
              ) : (
                Array(5)
                  .fill()
                  .map((_, i) => (
                    <div className="product-detail-thumb-slide keen-slider__slide" key={i}>
                      <div className="product-detail-thumb">
                        <img
                          src={noImage}
                          alt="placeholder"
                          className="product-detail-thumb-img"
                          draggable={false}
                          onDragStart={(e) => e.preventDefault()}
                          onContextMenu={(e) => e.preventDefault()}
                          onError={(e) => {
                            e.currentTarget.src = noImage;
                          }}
                        />
                      </div>
                    </div>
                  ))
              )}
            </section>
          </div>
        </div>
        {/* 오른쪽: 상품 정보 */}
        <article className="product-detail-info">
          <h2 className="product-detail-title">
            {product?.name || product?.productName || '상품 상세 정보'}
          </h2>
          <hr className="product-detail-divider" />

          <div className="product-detail-row">
            <span className="product-detail-label">구매마감</span>
            <span
              className={`purchase-deadline product-detail-value ${isReservationClosed ? 'closed' : ''}`}
            >
              {purchaseDeadlineLabel}
            </span>
          </div>

          <div className="product-detail-row">
            <span className="product-detail-label">발송예정</span>
            <span className="product-detail-value">{estimatedDeliveryLabel}</span>
          </div>
          <hr className="product-detail-divider" />
          <div className="product-detail-row">
            <span className="product-detail-label">카테고리</span>
            {product?.categoryGroupName ? (
              <span className="product-detail-value d-flex flex-wrap gap-2 justify-content-end">
                {product.categoryGroupName
                  .split(',')
                  .map((value) => value.trim())
                  .filter((value) => value.length > 0)
                  .map((label) => (
                    <span key={label} className="badge text-bg-secondary fs-6">
                      {label}
                    </span>
                  ))}
              </span>
            ) : (
              <span className="product-detail-value">카테고리 미지정</span>
            )}
          </div>
          <hr className="product-detail-divider" />
          <div className="product-detail-price">
            <span className="product-detail-label fw-semibold">상품 금액 (배송비 별도)</span>
            <p className="product-detail-price-value">
              {typeof product?.price === 'number'
                ? product.price.toLocaleString()
                : product?.price || '-'}
              원
            </p>
          </div>
          <div className="product-detail-row product-detail-row--tight">
            <label htmlFor="quantitySelect" className="product-detail-label">
              수량
            </label>
            <select
              id="quantitySelect"
              className="form-select product-detail-select"
              value={selectedQuantity}
              onChange={(e) => setSelectedQuantity(e.target.value)}
              disabled={soldOut}
            >
              {quantityOptions.length > 0 ? (
                quantityOptions.map((value) => (
                  <option key={value} value={value}>
                    {value}
                  </option>
                ))
              ) : (
                <option value={selectedQuantity || 0}>{selectedQuantity || 0}</option>
              )}
            </select>
          </div>
          {soldOut && (
            <div className="text-danger fw-semibold mt-2" role="alert">
              {soldOutReason}
            </div>
          )}
          {soldOut ? (
            <button className="btn btn-outline-secondary product-detail-cta" disabled>
              품절
            </button>
          ) : isReservationSale ? (
            <button className="btn btn-primary product-detail-cta" onClick={handleReserveClick}>
              예약 구매하기
            </button>
          ) : (
            <button
              className="btn btn-outline-primary product-detail-cta"
              onClick={() => addToCart({ reservation: false })}
            >
              장바구니에 담기
            </button>
          )}
          <div className="product-detail-warning text-secondary">
            <div>
              <i className="bi bi-exclamation-circle me-1"></i>
              상품 불량의 경우를 제외하고는, 반품은 불가능합니다.
            </div>
            <div className="mt-2 ms-3">취소는 결제 수단에 따라 다릅니다.</div>
            <div className="mt-2 ms-3">
              자세한 내용 <i className="bi bi-caret-right-fill mx-1"></i>
              <a
                href="/terms?type=e-commerce"
                className="product-detail-warning-link"
                target="_blank"
                rel="noopener noreferrer"
              >
                [가이드 - 반품・교환・취소]
              </a>
            </div>
          </div>
        </article>
      </section>
      {/* 상세보기/후기/Q&A */}
      <div className="tab-switch d-flex justify-content-center bg-light py-3 mb-4">
        <button
          id="detailBtn"
          className="btn bg-transparent border-0 fw-bold mx-4"
          onClick={() => {
            detailRef.current?.scrollIntoView({ behavior: 'smooth', block: 'start' });
          }}
        >
          상세보기
        </button>
        <button
          id="reviewBtn"
          className="btn bg-transparent border-0 fw-bold mx-4"
          onClick={() => {
            reviewRef.current?.scrollIntoView({ behavior: 'smooth', block: 'start' });
          }}
        >
          리뷰/후기
        </button>
        <button
          id="qnaBtn"
          className="btn bg-transparent border-0 fw-bold mx-4"
          onClick={() => {
            qnaRef.current?.scrollIntoView({ behavior: 'smooth', block: 'start' });
          }}
        >
          Q&A
        </button>
      </div>
      <section className="product-subsection w-100 px-3">
        {/* 상세보기 추가 */}
        <div id="detail-section" className="product-description-section mb-5">
          <h5 className="fw-bold mb-3" ref={detailRef}>
            상세보기
          </h5>{' '}
          {product?.description ? (
            <div
              ref={descRef}
              className="product-description-content tiptap"
              onContextMenu={(e) => e.preventDefault()}
              onDragStart={(e) => e.preventDefault()}
              dangerouslySetInnerHTML={{ __html: product.description }}
            />
          ) : (
            <p>상품 설명이 준비중입니다.</p>
          )}
        </div>
        <CM_03_1004_ReviewSection reviews={reviews} reviewRef={reviewRef} />
        {/* Q&A (접이식) */}
        <div id="qna-section" className="product-qna-section">
          <h5 className="fw-bold mb-3" ref={qnaRef}>
            Q&A
          </h5>
          
          {!qnaLoading && qnaError && <p className="text-danger small">{qnaError}</p>}
          {!qnaLoading && !qnaError && qnaList.length === 0 && (
            <p className="text-muted">{CMMessage.MSG_EMPTY_007}</p>
          )}
          {!qnaLoading &&
            qnaList.map((item, index) => (
              <CM_03_1004_QnaItem
                key={`${item?.qnaId || ''}-${index}`}
                item={item}
                index={index}
                open={qnaOpen}
                toggle={toggleQna}
              />
            ))}
        </div>
        {/* 배송안내 및 반품/교환/AS (접이식) */}
        <div className="product-faq-section py-5">
          <details className="mb-3 border rounded">
            <summary className="p-3 fw-bold bg-light d-flex justify-content-between align-items-center">
              배송안내
              <i className="bi bi-chevron-down"></i>
            </summary>
            <div className="p-3">
              <p>주문 확인 후 주말, 공휴일을 제외한 영업일 기준으로 평균 1~5일정도 소요됩니다.</p>
              <ul>
                <li>예약 상품은 상품이 입고된 후에 출고됩니다.</li>
                <li>도서산간 지역은 추가 배송 기간이 필요할 수 있습니다.</li>
                <li>
                  연말, 연초, 명절 등 택배사에 물류가 집중되는 시기에는 평균적인 배송 시기보다 늦을
                  수 있습니다.
                </li>
              </ul>
            </div>
          </details>
          <details className="border rounded">
            <summary className="p-3 fw-bold bg-light d-flex justify-content-between align-items-center">
              반품/교환/AS
              <i className="bi bi-chevron-down"></i>
            </summary>
            <div className="p-3">
              <h6 className="fw-bold mb-2">반품안내</h6>
              <ul className="mb-3">
                <li>
                  제품 불량, 오배송 등 당사의 과실로 인한 반품은 당사가 배송비를 부담합니다. 단,
                  단순 변심 및 고객님의 사정으로 반품하실 경우 배송비는 고객님께서 부담하셔서
                  보내주시면 됩니다.
                </li>
              </ul>

              <h6 className="fw-bold mb-2">교환안내</h6>
              <ul className="mb-3">
                <li>
                  제품 불량, 오배송 등 당사 과실로 인한 반품은 당사가 배송비를 부담합니다. 다른
                  상품으로는 교환되지 않습니다. (1:1 문의 부탁드립니다)
                </li>
              </ul>

              <h6 className="fw-bold mb-2">반품 및 교환 가능 기준</h6>
              <ul className="mb-3">
                <li>상품의 포장 등을 훼손, 개봉하지 않은 상태로 반품 접수 후 처리됩니다.</li>
                <li>구매 후 7일 이내에 반품 의사를 접수해주시기 바랍니다.</li>
              </ul>

              <h6 className="fw-bold mb-2">반품 및 교환 불가능 기준</h6>
              <ul className="mb-0">
                <li>상품 패키지는 상품을 포장하는 포장재이며 상품의 일부분이 아닙니다.</li>
                <li>
                  제품 내용물이 훼손되지 않은 유통 과정에서 발생한 오염이나 패키지 손상은 교환 및
                  반품 대상이 되지 않습니다.
                </li>
                <li>
                  모니터 해상도 설정 차이로 인하여 상품 및 이미지의 색상이 실제와 다를 수 있으며
                  이는 교환 및 반품 사유가 되지 않습니다.
                </li>
              </ul>
            </div>
          </details>
        </div>
      </section>
      <Lightbox
        open={lightboxOpen}
        close={() => setLightboxOpen(false)}
        slides={lightboxSlides}
        index={lightboxIndex}
        on={{ view: ({ index }) => setLightboxIndex(index) }}
      />
      {/* 모달 컴포넌트 */}
      <CM_99_1000 isOpen={open1000} onClose={() => setOpen1000(false)} />
      <CM_99_1004
        isOpen={openCartModal}
        onClose={() => setOpenCartModal(false)}
        Message={cartMessage}
      />
    </>
  );
}
