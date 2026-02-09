import { useCookies } from 'react-cookie';
import { useEffect, useState } from 'react';
import CookieSettingsModal from './CookieSettingsModal';
import '../../styles/CookieBanner.css';

const COOKIE_NAME = 'cookieConsent';

export default function CookieBanner() {
  const [cookies, setCookie] = useCookies([COOKIE_NAME]);
  const [showBanner, setShowBanner] = useState(false);
  const [showModal, setShowModal] = useState(false);

  useEffect(() => {
    if (!cookies[COOKIE_NAME]) setShowBanner(true);
  }, [cookies]);

  const handleAccept = () => {
    setCookie(COOKIE_NAME, 'accepted', { path: '/', maxAge: 60 * 60 * 24 * 365 });
    setShowBanner(false);
  };

  const handleDecline = () => {
    setCookie(COOKIE_NAME, 'declined', { path: '/', maxAge: 60 * 60 * 24 * 365 });
    setShowBanner(false);
  };

  if (!showBanner) return null;

  return (
    <>
      <div className="cookie-banner fixed-bottom">
        <div className="cookie-banner_text">
          당사 웹사이트는 쿠키를 사용하여 장바구니 기능과 맞춤형 서비스를 제공합니다. 계속 이용 시
          쿠키 사용에 동의하신 것으로 간주됩니다.{' '}
          <a href="/cookie" className="text-primary text-decoration-none fw-semibold ms-1">
            자세히 보기
          </a>
        </div>
        <div className="cookie-banner_actions">
          <button className="btn btn-outline-primary btn-sm" onClick={() => setShowModal(true)}>
            쿠키 설정
          </button>
          <button className="btn btn-outline-secondary btn-sm" onClick={handleDecline}>
            모두 거절
          </button>
          <button className="btn btn-primary btn-sm" onClick={handleAccept}>
            승인
          </button>
        </div>
      </div>
      <CookieSettingsModal
        show={showModal}
        onClose={() => setShowModal(false)}
        onSave={(settings) => {
          setCookie('cookieConsent', 'customized', { path: '/', maxAge: 60 * 60 * 24 * 365 });
          setCookie('cookie_performance', settings.performance, {
            path: '/',
            maxAge: 60 * 60 * 24 * 365,
          });
          setCookie('cookie_targeting', settings.targeting, {
            path: '/',
            maxAge: 60 * 60 * 24 * 365,
          });
          setShowBanner(false);
        }}
      />
    </>
  );
}
