import React, { useState, useEffect } from 'react';
import { useSearchParams } from 'react-router-dom';
import { termsOfService, privacyPolicy, eCommerceInfo } from '../data/termsContent';
import '../styles/termsPage.css';
const contentMap = {
  'terms-of-service': { title: '이용약관', content: termsOfService },
  'privacy-policy': { title: '개인 정보 보호 정책', content: privacyPolicy },
  'e-commerce': { title: '전자상거래법에 의한 표기', content: eCommerceInfo },
};

export default function CM_TermsPage() {
  const [searchParams, setSearchParams] = useSearchParams();
  const [activeTab, setActiveTab] = useState('terms-of-service');

  useEffect(() => {
    const type = searchParams.get('type');
    if (type && contentMap[type]) {
      setActiveTab(type);
    } else {
      setSearchParams({ type: 'terms-of-service' });
      setActiveTab('terms-of-service');
    }
  }, [searchParams, setSearchParams]);

  const handleTabClick = (tabName) => {
    setSearchParams({ type: tabName });
  };

  const currentContent = contentMap[activeTab];

  return (
    <div className="terms-page-container">
      <div className="terms-menu-sidebar">
        <button
          onClick={() => handleTabClick('terms-of-service')}
          className={activeTab === 'terms-of-service' ? 'active' : ''}
        >
          이용약관
        </button>
        <button
          onClick={() => handleTabClick('privacy-policy')}
          className={activeTab === 'privacy-policy' ? 'active' : ''}
        >
          개인 정보 보호 정책
        </button>
        <button
          onClick={() => handleTabClick('e-commerce')}
          className={activeTab === 'e-commerce' ? 'active' : ''}
        >
          전자상거래법에 의한 표기
        </button>
      </div>

      <div className="terms-content-display">
        {currentContent ? (
          <div dangerouslySetInnerHTML={{ __html: currentContent.content }} />
        ) : (
          <p>선택된 약관 내용을 불러올 수 없습니다.</p>
        )}
      </div>
    </div>
  );
}
