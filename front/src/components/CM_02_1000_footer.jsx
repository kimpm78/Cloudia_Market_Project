import { forwardRef } from 'react';
import { Link } from 'react-router-dom';
import kakaoTalk from '../images/kakao-icon.svg';

import '../styles/CM_02_1000_footer.css';

const CM_02_1000_footer = forwardRef((props, ref) => {
  const scrollToTop = () => {
    window.scrollTo({ top: 0, behavior: 'smooth' });
  };

  return (
    <footer
      ref={ref}
      className="bg-dark text-light pt-5 border-top"
      style={{ paddingBottom: '75px' }}
    >
      <div className="container">
        <div className="row text-start mb-4">
          <div className="col-12 col-md-3 mb-3">
            <h6 className="mb-3">クイックリンク</h6>
            <ul className="list-unstyled">
              <li className="mb-2">
                <Link
                  to="/new-product"
                  className="text-light text-decoration-none"
                  onClick={scrollToTop}
                >
                  ・新商品
                </Link>
              </li>
              <li className="mb-2">
                <Link
                  to="/pre-order"
                  className="text-light text-decoration-none"
                  onClick={scrollToTop}
                >
                  ・予約商品
                </Link>
              </li>
              <li className="mb-2">
                <Link
                  to="/characters"
                  className="text-light text-decoration-none"
                  onClick={scrollToTop}
                >
                  ・キャラクター
                </Link>
              </li>
              <li>
                <Link
                  to="/genres"
                  className="text-light text-decoration-none"
                  onClick={scrollToTop}
                >
                  ・ジャンル
                </Link>
              </li>
            </ul>
          </div>

          <div className="col-12 col-md-3 mb-3">
            <h6 className="mb-3">掲示板</h6>
            <ul className="list-unstyled">
              <li className="mb-2">
                <Link
                  to="/review"
                  className="text-light text-decoration-none"
                  onClick={scrollToTop}
                >
                  ・レビュー／口コミ
                </Link>
              </li>
              <li className="mb-2">
                <Link to="/qna" className="text-light text-decoration-none" onClick={scrollToTop}>
                  ・Q＆A
                </Link>
              </li>
              <li className="mb-2">
                <Link
                  to="/notice"
                  className="text-light text-decoration-none"
                  onClick={scrollToTop}
                >
                  ・お知らせ
                </Link>
              </li>
            </ul>
          </div>

          <div className="col-12 col-md-3 mb-3">
            <h6 className="mb-3">会社情報</h6>
            <ul className="list-unstyled">
              <li className="mb-2">
                <Link
                  to="/company"
                  className="text-light text-decoration-none"
                  onClick={scrollToTop}
                >
                  ・会社概要
                </Link>
              </li>
            </ul>
          </div>

          <div className="col-12 col-md-3 mb-3 text-md-end text-center">
            <div
              className="d-flex justify-content-md-end justify-content-center"
              style={{ gap: '20px', alignItems: 'center' }}
            >
              <a href="https://x.com" target="_blank" rel="noopener noreferrer">
                <i
                  className="bi bi-twitter-x text-light social-icon"
                  style={{ fontSize: '30px', cursor: 'pointer' }}
                ></i>
              </a>
              <a href="https://instagram.com" target="_blank" rel="noopener noreferrer">
                <i
                  className="bi bi-instagram text-light social-icon"
                  style={{ fontSize: '30px', cursor: 'pointer' }}
                ></i>
              </a>
              <a href="https://youtube.com" target="_blank" rel="noopener noreferrer">
                <i
                  className="bi bi-youtube text-light social-icon"
                  style={{ fontSize: '30px', cursor: 'pointer' }}
                ></i>
              </a>
              <a
                href="https://pf.kakao.com/_your_channel_id"
                target="_blank"
                rel="noopener noreferrer"
              >
                <img
                  src={kakaoTalk}
                  alt="KakaoTalk"
                  style={{ width: '30px', height: '30px', cursor: 'pointer' }}
                  className="social-icon"
                />
              </a>
            </div>
          </div>
        </div>

        <div className="row border-top pt-4">
          <div className="col-md-6 text-light small">
            法人名（商号）：クラウディア・マーケット株式会社 ｜ 代表者：JINKYU LIM ｜
            事業者登録番号：6467-03-256420
            <br />
            住所：403 Tomintawa- 2-7-5 Shinonome, Kotoku Tokyo, Japan
            <br />
            個人情報保護管理責任者：イム・ジンギュ
          </div>
          <div className="col-md-6 text-end text-light small footer-phone-number">
            <div style={{ fontSize: '18px' }}>電話番号：080-1527-0716</div>
            <div>電話受付：平日 09:00 ～ 18:00 ／ 昼休み 12:30 ～ 13:30</div>
            <div>（土・日・祝日は休業）</div>
          </div>
        </div>

        <div className="text-center small text-muted mt-4">
          <Link
            to="/terms?type=terms-of-service"
            className="text-light text-decoration-none"
            onClick={scrollToTop}
          >
            利用規約
          </Link>
          &nbsp;|&nbsp;
          <Link
            to="/terms?type=privacy-policy"
            className="text-light text-decoration-none"
            onClick={scrollToTop}
          >
            プライバシーポリシー
          </Link>
          &nbsp;|&nbsp;
          <Link
            to="/terms?type=e-commerce"
            className="text-light text-decoration-none"
            onClick={scrollToTop}
          >
            特定商取引法に基づく表記
          </Link>
        </div>

        <div className="text-center mt-2 pb-4 small text-light">
          <strong>
            Copyright © {new Date().getFullYear()} Cloudia Market. All rights reserved.
          </strong>
        </div>
      </div>
    </footer>
  );
});

export default CM_02_1000_footer;