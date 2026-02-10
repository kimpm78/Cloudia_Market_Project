import { Outlet } from 'react-router-dom';
import { MenuProvider } from '../contexts/CM_90_1000_SideMenuContext';
import CM_90_1000_SideBar from '../components/CM_90_1000_SideBar';

export default function CM_90_1000() {
  return (
    <MenuProvider>
      <div className="d-flex">
        <CM_90_1000_SideBar />
        <div className="content-wrapper flex-grow-1 p-3">
          <Outlet />
        </div>
      </div>
    </MenuProvider>
  );
}
