import { useRef } from 'react';
import CM_02_1000_header from './components/CM_02_1000_header';
import CM_02_1000_topScroll from './components/CM_02_1000_topScroll';
import CM_02_1000_footer from './components/CM_02_1000_footer';
import CookieBanner from './components/cookie/CookieBanner';
import Routes from './routes/Routes';
import { AuthProvider } from './contexts/AuthContext';
import { CartProvider } from './contexts/CartContext';
import { ConfigProvider } from './contexts/ConfigContext';
import usePageTracking from './hooks/usePageTracking';

function App() {
  usePageTracking();
  const footerRef = useRef(null);

  return (
    <ConfigProvider>
      <AuthProvider>
        <CartProvider>
          <div className="page-wrapper">
            <CM_02_1000_header />
            <main className="page-content">
              <Routes />
            </main>
            <CM_02_1000_topScroll footerRef={footerRef} />
            <CM_02_1000_footer ref={footerRef} />
            <CookieBanner />
          </div>
        </CartProvider>
      </AuthProvider>
    </ConfigProvider>
  );
}

export default App;
