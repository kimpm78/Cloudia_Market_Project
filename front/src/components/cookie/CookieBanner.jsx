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
          当社ウェブサイトは、クッキーを使用してカート機能およびパーソナライズされたサービスを提供します。引き続きご利用いただくことで、クッキーの使用に同意したものとみなされます。{' '}
          <a href="/cookie" className="text-primary text-decoration-none fw-semibold ms-1">
            詳細を見る
          </a>
        </div>
        <div className="cookie-banner_actions">
          <button className="btn btn-outline-primary btn-sm" onClick={() => setShowModal(true)}>
            クッキー設定
          </button>
          <button className="btn btn-outline-secondary btn-sm" onClick={handleDecline}>
            すべて拒否
          </button>
          <button className="btn btn-primary btn-sm" onClick={handleAccept}>
            同意
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
