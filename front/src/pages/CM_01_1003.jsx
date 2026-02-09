import { useState } from 'react';
import UserAuthLayout from '../components/layouts/UserAuthLayout';
import ResetPasswordResult from '../components/userAuth/ResetPasswordResult';
import ResetPasswordProcess from '../components/userAuth/ResetPasswordProcess';
import ResetPasswordSetting from '../components/userAuth/ResetPasswordSetting';
import '../styles/CM_01_1003.css';

export default function CM_01_1003() {
  const [verifiedEmail, setVerifiedEmail] = useState(null);
  const [isResetComplete, setIsResetComplete] = useState(false);

  // ステップ1（メール認証）成功時にメールアドレスを受け取り、状態を更新
  const handleVerificationSuccess = (email) => {
    setVerifiedEmail(email);
  };

  // ステップ2（パスワード変更）成功時に完了状態を更新
  const handleResetSuccess = () => {
    setIsResetComplete(true);
  };

  return (
    <UserAuthLayout>
      {/* 状態に応じて画面を条件付きでレンダリング */}
      {isResetComplete ? (
        <ResetPasswordResult />
      ) : verifiedEmail ? (
        <ResetPasswordSetting email={verifiedEmail} onSuccess={handleResetSuccess} />
      ) : (
        <ResetPasswordProcess onSuccess={handleVerificationSuccess} />
      )}
    </UserAuthLayout>
  );
}
