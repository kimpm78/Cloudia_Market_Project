import { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import CM_90_1011_grid from '../components/CM_90_1000_grid';
import axiosInstance from '../services/axiosInstance';
import CM_99_1001 from '../components/commonPopup/CM_99_1001';
import CM_99_1003 from '../components/commonPopup/CM_99_1003';
import CM_99_1004 from '../components/commonPopup/CM_99_1004';

export default function CM_90_1040() {
  const navigate = useNavigate();
  const [selectedRows, setSelectedRows] = useState([]);
  const [searchTerm, setSearchTerm] = useState('');
  const [rowData, setRowData] = useState([]);
  const [open1001, setOpen1001] = useState(false);
  const [open1003, setOpen1003] = useState(false);
  const [open1004, setOpen1004] = useState(false);
  const [message, setMessage] = useState('');

  const columnDefs = [
    {
      headerName: 'バナー名',
      field: 'bannerName',
      minWidth: 100,
      flex: 1,
    },
    {
      headerName: 'リンク',
      field: 'urlLink',
      minWidth: 300,
      flex: 2,
    },
    {
      headerName: '順序',
      field: 'displayOrder',
      minWidth: 30,
      maxWidth: 50,
      flex: 1,
    },
    {
      headerName: '登録日',
      field: 'createdAt',
      minWidth: 120,
      maxWidth: 180,
      flex: 1,
      valueFormatter: (params) => {
        const value = params.value;
        if (!value) return '';
        const date = new Date(value);
        if (isNaN(date.getTime())) return '';
        const year = date.getFullYear();
        const month = String(date.getMonth() + 1).padStart(2, '0');
        const day = String(date.getDate()).padStart(2, '0');
        return `${year}-${month}-${day}`;
      },
    },
    {
      headerName: '画像',
      field: 'imageLink',
      cellRenderer: (params) => {
        const imageUrl = params.value;
        if (!imageUrl) return '';
        const fullImageUrl = `${import.meta.env.VITE_API_BASE_IMAGE_URL}${imageUrl}`;
        return (
          <img
            src={fullImageUrl}
            style={{ width: '300px', height: '100px', objectFit: 'fill' }}
            alt="商品画像"
          />
        );
      },
      minWidth: 120,
      flex: 1,
    },
    {
      headerName: 'ステータス',
      field: 'isDisplay',
      minWidth: 120,
      maxWidth: 180,
      flex: 1,
      valueFormatter: (params) => {
        const value = params.value;
        if (value === 1) return '表示中';
        else return '非表示';
      },
    },
  ];

  useEffect(() => {
    stockAll();
  }, []);

  const stockAll = async () => {
    const result = await axiosInstance.get('/admin/menu/banner/findAll');
    if (result.data.result) {
      setRowData(result.data.resultList);
    }
  };

  const onSelectionChanged = (e) => {
    if (e.api) {
      const selectedNodes = e.api.getSelectedNodes();
      const selectedData = selectedNodes.map((node) => node.data);
      setSelectedRows(selectedData);
    }
  };

  const handleEditing = () => {
    const bannerId = selectedRows[0].bannerId;
    navigate(`/admin/menu/banner/edit/${bannerId}`);
  };

  const handleDelete = async () => {
    const result = await axiosInstance.delete('/admin/menu/banner/del', {
      data: selectedRows,
    });
    if (result.data.result) {
      setMessage(result.data.message);
      setOpen1004(true);
      stockAll();
    } else {
      setOpen1003(true);
    }

    setOpen1001(false);
  };

  const handleAlert = (field) => (e) => {
    if (selectedRows.length === 0 && (field === 'edit' || field === 'del')) {
      setMessage('項目を選択してください。');
      setOpen1004(true);
      return;
    } else if (selectedRows.length > 1 && field === 'edit') {
      setMessage('項目は1件のみ選択してください。');
      setOpen1004(true);
      return;
    }
    if (field === 'edit') {
      handleEditing();
    } else {
      setMessage('本当に削除しますか？');
      setOpen1001(true);
    }
  };

  const handleSearch = async () => {
    if (!searchTerm.trim()) {
      stockAll();
    } else {
      const result = await axiosInstance.get('/admin/menu/banner/findBanner', {
        params: {
          searchTerm: searchTerm,
        },
      });
      if (result.data.result) {
        setRowData(result.data.resultList);
      }
    }
  };

  return (
    <div className="d-flex flex-grow-1">
      <div className="content-wrapper p-3">
        <h5 className="border-bottom pb-2 mb-3">バナー管理</h5>
        <div className="row mb-3">
          <div className="col-12">
            <div className="d-flex align-items-center w-100">
              <div className="d-flex align-items-center">
                <div className="input-group input-group-custom">
                  <input
                    type="text"
                    className="form-control"
                    placeholder="バナー名を入力してください"
                    value={searchTerm}
                    onChange={(e) => setSearchTerm(e.target.value)}
                    onKeyDown={(e) => e.key === 'Enter' && handleSearch()}
                    onClick={handleSearch}
                  />
                </div>

                <button className="btn btn-primary ms-2 btn-min-width" onClick={handleSearch}>
                  検索
                </button>
              </div>
              <div className="ms-auto d-flex align-items-center">
                <button className="btn btn-primary me-2 btn-min-width" onClick={handleAlert('del')}>
                  削除
                </button>
                <button
                  className="btn btn-primary me-2 btn-min-width"
                  onClick={handleAlert('edit')}
                >
                  修正
                </button>
                <Link to="/admin/menu/banner/register" className="btn btn-primary btn-min-width">
                  登録
                </Link>
              </div>
            </div>
          </div>
        </div>
        <CM_90_1011_grid
          itemsPerPage={10}
          pagesPerGroup={5}
          rowData={rowData}
          columnDefs={columnDefs}
          rowSelection="multiRow"
          onSelectionChanged={onSelectionChanged}
        />
        <CM_99_1001
          isOpen={open1001}
          onClose={() => setOpen1001(false)}
          onConfirm={() => {
            handleDelete();
          }}
          Message={message}
        />
        <CM_99_1003 isOpen={open1003} onClose={() => setOpen1003(false)} />
        <CM_99_1004 isOpen={open1004} onClose={() => setOpen1004(false)} Message={message} />
      </div>
    </div>
  );
}
