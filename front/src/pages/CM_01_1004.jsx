import { Outlet } from 'react-router-dom';
import CM_01_1004_sideBar from '../components/CM_01_1004_SideBar';
import '../styles/CM_01_1004.css';

export default function CM_01_1004() {
  return (
    <div className="container-fluid ">
      <div className="row flex-grow-1">
        <div className="col-md-3 col-lg-2 mypage-sidebar-column">
          <CM_01_1004_sideBar />
        </div>
        <main className="col-md-9 col-lg-10">
          <Outlet />
        </main>
      </div>
    </div>
  );
}
