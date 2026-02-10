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
    if (isReservationClosed) return '締切';
    if (soldOut) return 'SOLD OUT';
    return null;
  }, [isReservationClosed, soldOut]);

  const purchaseDeadlineLabel = useMemo(() => {
    if (!isReservationSale) {
      return '通常販売';
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
      return isReservationClosed ? `締切（${formatted}）` : formatted;
    }

    const formatted = formatDateTime(reservationDeadlineDate);
    return isReservationClosed ? `締切（${formatted}）` : formatted;
  }, [isReservationSale, reservationDeadlineValue, reservationDeadlineDate, isReservationClosed]);

  const estimatedDeliveryLabel = useMemo(() => {
    if (!product) return '-';
    if (isReservationSale) {
      return formatYearMonthDot(product?.releaseDate) || '-';
    }
    return '5日以内に発送';
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
            console.error('レビュー取得失敗:', err);
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
                item?.statusLabel || (item?.statusValue === 2 ? '回答済み' : '質問');
              const baseName = item?.loginId || item?.writerName || `user-${item?.userId ?? ''}`;
              return {
                qnaId: item?.qnaId,
                status: statusLabel,
                question: visible ? item?.title : '非公開の投稿です。',
                detail: visible ? item?.content : '',
                answer: visible ? item?.answerContent || '' : '',
                answerDate: visible ? formatDateShort(item?.answerCreatedAt) : '',
                name: visible ? baseName : '非公開',
                date: formatDateShort(item?.createdAt),
                secret: !visible,
              };
            });

            if (!cancelled) setQnaList(mapped);
          } catch (err) {
            console.error('商品Q&A取得失敗:', err);
            if (!cancelled) {
              setQnaError(CMMessage.MSG_ERR_005('商品Q&A'));
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
        console.error('商品詳細情報の取得に失敗:', err?.response?.data?.message || err.message);
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
      console.error('カート件数の更新に失敗:', err?.response?.data || err.message);
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

  // 画像ライトボックスを開く
  const openLightboxAt = (url) => {
    const idx = lightboxSlides.findIndex((s) => s.src === url);
    setLightboxIndex(Math.max(0, idx));
    setLightboxOpen(true);
  };

  return (
    <>
      <section className="product-detail">
        {/* 左側: メイン画像 + サムネイル */}
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
              alt="詳細画像"
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
              aria-label="画像を拡大"
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
                        alt={`サムネイル ${index}`}
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
                      alt="サムネイル"
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
                          alt="プレースホルダー"
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
        {/* 右側: 商品情報 */}
        <article className="product-detail-info">
          <h2 className="product-detail-title">
            {product?.name || product?.productName || '商品詳細情報'}
          </h2>
          <hr className="product-detail-divider" />

          <div className="product-detail-row">
            <span className="product-detail-label">購入締切</span>
            <span
              className={`purchase-deadline product-detail-value ${isReservationClosed ? 'closed' : ''}`}
            >
              {purchaseDeadlineLabel}
            </span>
          </div>

          <div className="product-detail-row">
            <span className="product-detail-label">発送予定</span>
            <span className="product-detail-value">{estimatedDeliveryLabel}</span>
          </div>
          <hr className="product-detail-divider" />
          <div className="product-detail-row">
            <span className="product-detail-label">カテゴリー</span>
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
              <span className="product-detail-value">カテゴリー未設定</span>
            )}
          </div>
          <hr className="product-detail-divider" />
          <div className="product-detail-price">
            <span className="product-detail-label fw-semibold">商品価格（送料別）</span>
            <p className="product-detail-price-value">
              {typeof product?.price === 'number'
                ? product.price.toLocaleString()
                : product?.price || '-'}
              円
            </p>
          </div>
          <div className="product-detail-row product-detail-row--tight">
            <label htmlFor="quantitySelect" className="product-detail-label">
              数量
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
              在庫切れ
            </button>
          ) : isReservationSale ? (
            <button className="btn btn-primary product-detail-cta" onClick={handleReserveClick}>
              予約購入する
            </button>
          ) : (
            <button
              className="btn btn-outline-primary product-detail-cta"
              onClick={() => addToCart({ reservation: false })}
            >
              カートに入れる
            </button>
          )}
          <div className="product-detail-warning text-secondary">
            <div>
              <i className="bi bi-exclamation-circle me-1"></i>
              商品不良の場合を除き、返品はできません。
            </div>
            <div className="mt-2 ms-3">キャンセルは決済方法により異なります。</div>
            <div className="mt-2 ms-3">
              詳細は <i className="bi bi-caret-right-fill mx-1"></i>
              <a
                href="/terms?type=e-commerce"
                className="product-detail-warning-link"
                target="_blank"
                rel="noopener noreferrer"
              >
                [ガイド - 返品・交換・キャンセル]
              </a>
            </div>
          </div>
        </article>
      </section>
      {/* 商品詳細/レビュー/Q&A */}
      <div className="tab-switch d-flex justify-content-center bg-light py-3 mb-4">
        <button
          id="detailBtn"
          className="btn bg-transparent border-0 fw-bold mx-4"
          onClick={() => {
            detailRef.current?.scrollIntoView({ behavior: 'smooth', block: 'start' });
          }}
        >
          商品詳細
        </button>
        <button
          id="reviewBtn"
          className="btn bg-transparent border-0 fw-bold mx-4"
          onClick={() => {
            reviewRef.current?.scrollIntoView({ behavior: 'smooth', block: 'start' });
          }}
        >
          レビュー/口コミ
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
        {/* 商品詳細 */}
        <div id="detail-section" className="product-description-section mb-5">
          <h5 className="fw-bold mb-3" ref={detailRef}>
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
            <p>商品説明を準備中です。</p>
          )}
        </div>
        <CM_03_1004_ReviewSection reviews={reviews} reviewRef={reviewRef} />
        {/* Q&A（アコーディオン） */}
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
        {/* 配送案内・返品/交換/AS（アコーディオン） */}
        <div className="product-faq-section py-5">
          <details className="mb-3 border rounded">
            <summary className="p-3 fw-bold bg-light d-flex justify-content-between align-items-center">
              配送案内
              <i className="bi bi-chevron-down"></i>
            </summary>
            <div className="p-3">
              <p>ご注文確認後、土日祝を除く営業日 기준で平均1〜5日ほどかかります。</p>
              <ul>
                <li>予約商品は入荷後に発送されます。</li>
                <li>離島・山間部は追加の配送日数が必要になる場合があります。</li>
                <li>
                  年末年始や連休、繁忙期など配送会社の物量が集中する時期は、通常より遅れる場合があります。
                </li>
              </ul>
            </div>
          </details>
          <details className="border rounded">
            <summary className="p-3 fw-bold bg-light d-flex justify-content-between align-items-center">
              返品・交換・アフターサービス
              <i className="bi bi-chevron-down"></i>
            </summary>
            <div className="p-3">
              <h6 className="fw-bold mb-2">返品のご案内</h6>
              <ul className="mb-3">
                <li>
                  商品不良・誤配送など当社都合による返品は当社が送料を負担します。なお、お客様都合（イメージ違い等）の場合は、お客様負担でご返送ください。
                </li>
              </ul>

              <h6 className="fw-bold mb-2">交換のご案内</h6>
              <ul className="mb-3">
                <li>
                  商品不良・誤配送など当社都合による返品は当社が送料を負担します。別商品への交換はできません。（1:1お問い合わせをお願いします）
                </li>
              </ul>

              <h6 className="fw-bold mb-2">返品・交換が可能な条件</h6>
              <ul className="mb-3">
                <li>商品パッケージ等を破損・開封していない状態で返品受付後に対応します。</li>
                <li>ご購入後7日以内に返品の意思をご連絡ください。</li>
              </ul>

              <h6 className="fw-bold mb-2">返品・交換ができない条件</h6>
              <ul className="mb-0">
                <li>商品パッケージは梱包材であり、商品本体の一部ではありません。</li>
                <li>
                  流通過程で発生した汚れやパッケージの傷み（中身に破損がない場合）は返品・交換の対象外です。
                </li>
                <li>
                  モニター設定等により商品画像の色味が実物と異なる場合がありますが、返品・交換の理由にはなりません。
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
      {/* モーダル */}
      <CM_99_1000 isOpen={open1000} onClose={() => setOpen1000(false)} />
      <CM_99_1004
        isOpen={openCartModal}
        onClose={() => setOpenCartModal(false)}
        Message={cartMessage}
      />
    </>
  );
}
