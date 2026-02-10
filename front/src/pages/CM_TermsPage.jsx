import React, { useState, useEffect } from 'react';
import { useSearchParams } from 'react-router-dom';
import { termsOfService, privacyPolicy, eCommerceInfo } from '../data/termsContent';
import '../styles/termsPage.css';

const contentMap = {
  'terms-of-service': { label: '利用規約', content: termsOfService },
  'privacy-policy': { label: 'プライバシーポリシー', content: privacyPolicy },
  'e-commerce': { label: '特定商取引法に基づく表記', content: eCommerceInfo },
};

const tabs = ['terms-of-service', 'privacy-policy', 'e-commerce'];

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
      <h2 className="terms-page-title">各種規約</h2>

      <div className="terms-menu-sidebar" role="tablist" aria-label="規約カテゴリ">
        {tabs.map((tab) => (
          <button
            key={tab}
            type="button"
            onClick={() => handleTabClick(tab)}
            className={activeTab === tab ? 'active' : ''}
            role="tab"
            aria-selected={activeTab === tab}
          >
            {contentMap[tab].label}
          </button>
        ))}
      </div>

      <div className="terms-content-display">
        {currentContent ? (
          <div dangerouslySetInnerHTML={{ __html: currentContent.content }} />
        ) : (
          <p>選択された規約内容を読み込めませんでした。</p>
        )}
      </div>
    </div>
  );
}
