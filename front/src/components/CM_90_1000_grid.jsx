import { useRef, useMemo, useEffect, useState } from 'react';
import usePagination from '../hooks/usePagination';
import Pagination from './CM_90_1000_gridPagination';
import { AgGridReact } from 'ag-grid-react';
import {
  ModuleRegistry,
  ClientSideRowModelModule,
  CellStyleModule,
  ValidationModule,
  RowSelectionModule,
} from 'ag-grid-community';
import 'ag-grid-community/styles/ag-grid.css';
import 'ag-grid-community/styles/ag-theme-balham.css';
import '../styles/CM_90_1000_grid.css';

ModuleRegistry.registerModules([
  ClientSideRowModelModule,
  CellStyleModule,
  ValidationModule,
  RowSelectionModule,
]);

const CM_90_1011_grid = ({
  rowData,
  columnDefs,
  itemsPerPage,
  pagesPerGroup,
  rowSelection,
  onSelectionChanged,
  onRowClicked,
  wrapperClass,
  hidePagination = false,
}) => {
  const gridContainerRef = useRef(null);
  const [rowHeight, setRowHeight] = useState(0);
  const {
    currentPage,
    totalPages,
    pageGroup,
    displayedData,
    goToPage,
    goToPrevGroup,
    goToNextGroup,
  } = usePagination(rowData, itemsPerPage, pagesPerGroup);

  const defaultColDef = useMemo(() => ({ resizable: true }), []);

  useEffect(() => {
    const updateRowHeight = () => {
      if (gridContainerRef.current) {
        const height = gridContainerRef.current.clientHeight;
        setRowHeight(hidePagination ? 35 : height / itemsPerPage);
      }
    };

    updateRowHeight();
    window.addEventListener('resize', updateRowHeight);
    return () => window.removeEventListener('resize', updateRowHeight);
  }, [itemsPerPage, hidePagination]);
  return (
    <>
      <div className={`ag-theme-balham ${wrapperClass}`} ref={gridContainerRef}>
        <div className="ag-dom">
          <AgGridReact
            className="custom-ag-grid"
            theme="legacy"
            columnDefs={columnDefs}
            rowData={hidePagination ? rowData : displayedData}
            defaultColDef={defaultColDef}
            domLayout="normal"
            rowHeight={rowHeight}
            suppressColumnVirtualisation
            suppressMovableColumns
            {...(rowSelection ? { rowSelection: { mode: rowSelection } } : {})}
            {...(onSelectionChanged ? { onSelectionChanged } : {})}
            {...(onRowClicked ? { onRowClicked } : {})}
          />
        </div>
      </div>
      {!hidePagination && (
        <Pagination
          currentPage={currentPage}
          totalPages={totalPages}
          pageGroup={pageGroup}
          pagesPerGroup={pagesPerGroup}
          goToPage={goToPage}
          goToPrevGroup={goToPrevGroup}
          goToNextGroup={goToNextGroup}
        />
      )}
    </>
  );
};

export default CM_90_1011_grid;
