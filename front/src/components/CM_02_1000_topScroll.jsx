import { useEffect, useState } from 'react';
import '../styles/CM_02_1000_topScroll.css';

const CM_02_1000_topScroll = ({ footerRef }) => {
  const [isVisible, setIsVisible] = useState(false);
  const [isOverlappingFooter, setIsOverlappingFooter] = useState(false);

  useEffect(() => {
    const toggleVisibility = () => {
      setIsVisible(window.scrollY > 300);
    };
    window.addEventListener('scroll', toggleVisibility);
    return () => window.removeEventListener('scroll', toggleVisibility);
  }, []);

  useEffect(() => {
    if (!footerRef || !footerRef.current) return;
    const observer = new window.IntersectionObserver(
      ([entry]) => {
        setIsOverlappingFooter(entry.isIntersecting);
      },
      { root: null, threshold: 0 }
    );
    observer.observe(footerRef.current);
    return () => observer.disconnect();
  }, [footerRef]);

  const scrollToTop = () => {
    window.scrollTo({ top: 0, behavior: 'smooth' });
  };

  return (
    <div
      className="top-scroll-button"
      style={{
        opacity: isVisible ? (isOverlappingFooter ? 0 : 1) : 0,
        transform: isVisible && !isOverlappingFooter ? 'translateY(0)' : 'translateY(20px)',
        transition: 'opacity 0.5s ease, transform 0.5s ease',
        pointerEvents: isVisible && !isOverlappingFooter ? 'auto' : 'none',
      }}
    >
      <button
        onClick={scrollToTop}
        style={{
          width: '50px',
          height: '50px',
          border: 'none',
          padding: 0,
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          cursor: 'pointer',
        }}
      >
        <i className="bi bi-arrow-up-circle-fill" style={{ fontSize: '80px' }}></i>
      </button>
    </div>
  );
};

export default CM_02_1000_topScroll;
