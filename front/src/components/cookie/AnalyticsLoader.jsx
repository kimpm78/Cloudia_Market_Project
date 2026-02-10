import { useEffect } from 'react';
import { useCookies } from 'react-cookie';

/**
 * Webサイトのトラフィック／行動分析に利用される統計・マーケティング用Cookieを読み込む
 * @see https://analytics.google.com/analytics/web/provision/#/provision
 */
export default function AnalyticsLoader() {
  const [cookies] = useCookies(['cookie_performance']);

  useEffect(() => {
    if (cookies.cookie_performance !== 'true') return;

    // ダミーID。後で "G-AB12CD34EF" のような実際の値に置き換える。
    const placeholderMeasurementId = 'G-XXXXXXX';
    const measurementId =
      import.meta.env.VITE_GA_MEASUREMENT_ID || placeholderMeasurementId;

    // プレースホルダーが使用されている間は無効のままにする。
    if (!/^G-[A-Z0-9]+$/i.test(measurementId) || measurementId === placeholderMeasurementId) {
      return;
    }

    const script = document.createElement('script');
    script.src = `https://www.googletagmanager.com/gtag/js?id=${measurementId}`;
    script.async = true;
    document.head.appendChild(script);

    window.dataLayer = window.dataLayer || [];
    function gtag() {
      window.dataLayer.push(arguments);
    }
    gtag('js', new Date());
    gtag('config', measurementId);
  }, [cookies]);

  return null;
}
