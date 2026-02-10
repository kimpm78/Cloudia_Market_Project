import { useSearchParams } from 'react-router-dom';
import CM_01_1008_grid from '../components/CM_01_1008_grid';
import CM_01_1008_AddressActionRenderer from '../components/CM_01_1008_AddressActionRenderer';
import '../styles/CM_01_1008.css';

export default function CM_01_1008() {
  const [searchParams] = useSearchParams();
  const mode = searchParams.get('mode');
  const isFormMode = mode === 'new' || mode === 'edit';

  return (
    <div className="container mt-2">
      <h2 className="border-bottom fw-bolder pb-3 mb-4 mt-4">住所録管理</h2>
      <div className="address-guide-panel mb-4">
        {isFormMode ? (
          <>
            <div className="address-guide-badge">新規登録 / 編集</div>
            <p className="address-guide-text mb-0">
              注文時に利用する配送先を登録できます。
              <br />
              必須項目（*）を入力して保存してください。
            </p>
          </>
        ) : (
          <>
            <div className="address-guide-badge">使い方ガイド</div>
            <p className="address-guide-text mb-2">
              配送先の新規登録・変更・削除は、この一覧から操作できます。
            </p>
            <p className="address-guide-text mb-1">
              <span className="address-guide-title">自宅情報を変更する場合</span>
            </p>
            <p className="address-guide-text mb-0">
              自宅情報は［プロフィール］ページから修正してください。
              <br />
              <span className="address-guide-note">
                ※ 住所録は最大3件まで登録できます。
              </span>
            </p>
          </>
        )}
      </div>

      {isFormMode ? <CM_01_1008_AddressActionRenderer /> : <CM_01_1008_grid />}
    </div>
  );
}
