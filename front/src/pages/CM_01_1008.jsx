import { useSearchParams } from 'react-router-dom';
import CM_01_1008_grid from '../components/CM_01_1008_grid';
import CM_01_1008_AddressActionRenderer from '../components/CM_01_1008_AddressActionRenderer';
import '../styles/CM_01_1008.css';

export default function CM_01_1008() {
  const [searchParams] = useSearchParams();
  const mode = searchParams.get('mode');
  const isFormMode = mode === 'new' || mode === 'edit';

  return (
    <div>
      <h4 className="border-bottom fw-bolder pb-3 mb-4 mt-2">住所録管理</h4>

      {isFormMode ? (
        <p>
          注文時に選択可能な配送先を新規登録できます。
          <br />
          下記の全項目を漏れなく入力してください。
        </p>
      ) : (
        <p>
          配送先の新規登録・変更・削除は、注文時に選択できる一覧から行えます。
          <br />
          &lt;自宅情報を変更する場合&gt;
          <br />
          自宅情報は［プロフィール］ページから修正できます。
          <br />※ 注文ごとに個別入力が必要です。住所録は最大3件まで登録可能です。
        </p>
      )}

      {isFormMode ? <CM_01_1008_AddressActionRenderer /> : <CM_01_1008_grid />}
    </div>
  );
}
