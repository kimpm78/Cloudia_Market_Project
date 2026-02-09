import { useEffect, useState } from 'react';
import CM_90_1011_grid from '../components/CM_90_1000_grid';
import axiosInstance from '../services/axiosInstance';
import CM_99_1001 from '../components/commonPopup/CM_99_1001';
import CM_99_1003 from '../components/commonPopup/CM_99_1003';
import CM_99_1004 from '../components/commonPopup/CM_99_1004';
import CM_90_1065_RegisterModal from '../components/CM_90_1065_RegisterModal';

export default function CM_90_1065() {
  const [openRegisterModal, setOpenRegisterModal] = useState(false);
  const [selectedRows, setSelectedRows] = useState([]);
  const [searchType, setSearchType] = useState('1');
  const [searchTerm, setSearchTerm] = useState('');
  const [rowData, setRowData] = useState([]);
  const [open1001, setOpen1001] = useState(false);
  const [open1003, setOpen1003] = useState(false);
  const [open1004, setOpen1004] = useState(false);
  const [message, setMessage] = useState('');

  const columnDefs = [
    {
      headerName: '상품 코드',
      field: 'productCode',
      maxWidth: 100,
      flex: 1,
    },
    {
      headerName: '상품명',
      field: 'productName',
      minWidth: 300,
      flex: 2,
    },
    {
      headerName: '상품 분류',
      field: 'productCategory',
      minWidth: 100,
      maxWidth: 100,
      flex: 1,
      valueFormatter: (params) => {
        const value = params.value;
        if (value === '1') return '상시 판매';
        else return '예약 판매';
      },
    },
    {
      headerName: '등록일',
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
      headerName: '등록자',
      field: 'createdBy',
      minWidth: 120,
      maxWidth: 180,
      flex: 1,
    },
  ];

  useEffect(() => {
    codeAll();
  }, []);

  const codeAll = async () => {
    const result = await axiosInstance.get('/admin/productCode/getCode');
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

  const handleDelete = async () => {
    try {
      const result = await axiosInstance.post('/admin/productCode/uptCode', selectedRows);

      if (result.data.result) {
        setMessage(result.data.message);
        setOpen1004(true);
        codeAll();
      } else {
        setOpen1003(true);
      }
    } catch (error) {
      setMessage(error.response.data.message);
      setOpen1003(true);
    } finally {
      setOpen1001(false);
    }
  };

  const handleAlert = (field) => (e) => {
    if (selectedRows.length === 0 && field === 'del') {
      setMessage('항목을 선택해주세요.');
      setOpen1004(true);
      return;
    }
    setMessage('정말로 삭제 하시겠습니까?');
    setOpen1001(true);
  };

  const handleSearch = async () => {
    if (!searchTerm.trim()) {
      codeAll();
    } else {
      const result = await axiosInstance.get('/admin/productCode/getFindCode', {
        params: {
          searchType: searchType,
          searchTerm: searchTerm,
        },
      });
      if (result.data.result) {
        setRowData(result.data.resultList);
      }
    }
  };

  const handleRegister = async (params) => {
    try {
      const result = await axiosInstance.post('/admin/productCode/insCode', params);

      if (result.data.result) {
        setMessage('등록이 완료되었습니다.');
        setOpen1004(true);
        codeAll();
      } else {
        setOpen1003(true);
      }
    } catch (error) {
      setMessage(error.response.data.message);
      setOpen1003(true);
    } finally {
      setOpenRegisterModal(false);
    }
  };

  return (
    <div className="d-flex flex-grow-1">
      <div className="content-wrapper p-3">
        <h5 className="border-bottom pb-2 mb-3">상품 코드 관리</h5>
        <div className="row mb-3">
          <div className="col-12">
            <div className="d-flex align-items-center w-100">
              <div className="d-flex align-items-center">
                <div className="input-group input-group-custom">
                  <select
                    className="form-select select-custom"
                    value={searchType}
                    onChange={(e) => setSearchType(e.target.value)}
                  >
                    <option value="1">상품 코드</option>
                    <option value="2">상품 명</option>
                  </select>
                  <input
                    type="text"
                    className="form-control"
                    placeholder="상품코드 / 상품명을 입력하세요"
                    value={searchTerm}
                    onChange={(e) => setSearchTerm(e.target.value)}
                    onKeyDown={(e) => e.key === 'Enter' && handleSearch()}
                  />
                </div>

                <button className="btn btn-primary ms-2 btn-min-width" onClick={handleSearch}>
                  검색
                </button>
                <button
                  className="btn btn-primary ms-2 btn-min-width"
                  onClick={() => setOpenRegisterModal(true)}
                >
                  등록
                </button>
              </div>
              {/* <div className="ms-auto d-flex align-items-center">
                <button className="btn btn-primary me-2 btn-min-width" onClick={handleAlert('del')}>
                  삭제
                </button>
              </div> */}
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
        <CM_99_1003 isOpen={open1003} onClose={() => setOpen1003(false)} message={message} />
        <CM_99_1004 isOpen={open1004} onClose={() => setOpen1004(false)} Message={message} />
        <CM_90_1065_RegisterModal
          show={openRegisterModal}
          onHide={() => setOpenRegisterModal(false)}
          onConfirm={handleRegister}
        />
      </div>
    </div>
  );
}
