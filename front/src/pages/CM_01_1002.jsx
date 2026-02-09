import { useState } from 'react';
import FindIdProcess from '../components/userAuth/FindIdProcess';
import FindIdResult from '../components/userAuth/FindIdResult';
import UserAuthLayout from '../components/layouts/UserAuthLayout';
import '../styles/CM_01_1002.css';

export default function CM_01_1002() {
  const [foundId, setFoundId] = useState(null);

  // FindIdProcess から見つかったIDを受け取る関数
  const handleIdFound = (id) => {
    setFoundId(id);
  };

  return (
    <UserAuthLayout>
      {foundId ? (
        // 見つかったIDがある場合は結果コンポーネントをレンダリング
        <FindIdResult id={foundId} />
      ) : (
        // 見つかったIDがない場合は処理コンポーネントをレンダリング
        <FindIdProcess onIdFound={handleIdFound} />
      )}
    </UserAuthLayout>
  );
}