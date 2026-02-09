import { Routes as ReactRoutes, Route } from 'react-router-dom';

import ProtectedRoute from './ProtectedRoute';
import CM_01_1000 from '../pages/CM_01_1000';
import CM_01_1001 from '../pages/CM_01_1001';
import CM_01_1002 from '../pages/CM_01_1002';
import CM_01_1003 from '../pages/CM_01_1003';
import CM_01_1004 from '../pages/CM_01_1004';
import CM_01_1005 from '../pages/CM_01_1005';
import CM_01_1006 from '../pages/CM_01_1006';
import CM_01_1007 from '../pages/CM_01_1007';
import CM_01_1008 from '../pages/CM_01_1008';
import CM_01_1009 from '../pages/CM_01_1009';
import CM_01_1010 from '../pages/CM_01_1010';
import CM_01_1011 from '../pages/CM_01_1011';
import CM_01_1012 from '../pages/CM_01_1012';
import CM_01_1013 from '../pages/CM_01_1013';
import CM_01_1014 from '../pages/CM_01_1014';
import CM_01_1015 from '../pages/CM_01_1015';
import CM_01_1016 from '../pages/CM_01_1016';
import CM_01_1017 from '../pages/CM_01_1017';
import CM_01_9999 from '../pages/CM_01_9999';
import CM_02_1000 from '../pages/CM_02_1000';
import CM_03_1000 from '../pages/CM_03_1000';
import CM_03_1001 from '../pages/CM_03_1001';
import CM_03_1002 from '../pages/CM_03_1002';
import CM_03_1003 from '../pages/CM_03_1003';
import CM_03_1004 from '../pages/CM_03_1004';
import CM_04_1000 from '../pages/CM_04_1000';
import CM_04_1001 from '../pages/CM_04_1001';
import CM_04_1002 from '../pages/CM_04_1002';
import CM_04_1003 from '../pages/CM_04_1003';
import CM_04_1004 from '../pages/CM_04_1004';
import CM_04_1005 from '../pages/CM_04_1005';
import CM_05_1000 from '../pages/CM_05_1000';
import CM_05_1001 from '../pages/CM_05_1001';
import CM_06_1000 from '../pages/CM_06_1000';
import CM_06_1001 from '../pages/CM_06_1001';
import CM_07_1000 from '../pages/CM_07_1000';
import CM_company from '../pages/CM_Company';

{
  /* 결제 라우팅 */
}
import OrderPaymentResult from '../components/payment/OrderPaymentResult';

{
  /* 관리자 라우팅 */
}
import CM_90_1000 from '../pages/CM_90_1000';
import CM_90_1010 from '../pages/CM_90_1010';
import CM_90_1020 from '../pages/CM_90_1020';
import CM_90_1021 from '../pages/CM_90_1021';
import CM_90_1030 from '../pages/CM_90_1030';
import CM_90_1031 from '../pages/CM_90_1031';
import CM_90_1040 from '../pages/CM_90_1040';
import CM_90_1041 from '../pages/CM_90_1041';
import CM_90_1042 from '../pages/CM_90_1042';
import CM_90_1043 from '../pages/CM_90_1043';
import CM_90_1044 from '../pages/CM_90_1044';
import CM_90_1045 from '../pages/CM_90_1045';
import CM_90_1046 from '../pages/CM_90_1046';
import CM_90_1050 from '../pages/CM_90_1050';
import CM_90_1051 from '../pages/CM_90_1051';
import CM_90_1052 from '../pages/CM_90_1052';
import CM_90_1053 from '../pages/CM_90_1053';
import CM_90_1054 from '../pages/CM_90_1054';
import CM_90_1060 from '../pages/CM_90_1060';
import CM_90_1061 from '../pages/CM_90_1061';
import CM_90_1062 from '../pages/CM_90_1062';
import CM_90_1063 from '../pages/CM_90_1063';
import CM_90_1064 from '../pages/CM_90_1064';
import CM_90_1065 from '../pages/CM_90_1065';

{
  /* 에러 페이지 */
}
import Unauthorized from '../pages/errors/Unauthorized';
import Forbidden from '../pages/errors/Forbidden';
import NotFound from '../pages/errors/NotFound';
import ServerError from '../pages/errors/ServerError';

{
  /* 쿠키 페이지 */
}
import CM_CookiePage from '../pages/CM_CookiePage';

// 약관 페이지 추가
import TermsPage from '../pages/CM_TermsPage';
import GuestRoute from './GuestRoutes';

const Routes = () => (
  <ReactRoutes>
    {/* --- 게스트 접근 가능 페이지 (권한 4) --- */}
    <Route
      path="/login"
      element={
        <GuestRoute>
          <CM_01_1000 />
        </GuestRoute>
      }
    />

    <Route
      path="/register"
      element={
        <GuestRoute>
          <CM_01_1001 />
        </GuestRoute>
      }
    />
    <Route
      path="/find-id"
      element={
        <GuestRoute>
          <CM_01_1002 />
        </GuestRoute>
      }
    />
    <Route
      path="/reset-password"
      element={
        <GuestRoute>
          <CM_01_1003 />
        </GuestRoute>
      }
    />
    <Route path="/" element={<CM_02_1000 />} />
    <Route path="/new-product" element={<CM_03_1000 />} />
    <Route path="/pre-order" element={<CM_03_1001 />} />
    <Route path="/characters" element={<CM_03_1002 />} />
    <Route path="/genres" element={<CM_03_1003 />} />
    <Route path="/detail/:id" element={<CM_03_1004 />} />
    {/* 리뷰/후기 & 상세 */}
    <Route path="/review" element={<CM_04_1000 />} />
    <Route path="/review/:id" element={<CM_04_1001 />} />
    {/* Q & A & 상세*/}
    <Route path="/qna" element={<CM_04_1003 />} />
    <Route path="/qna/:id" element={<CM_04_1004 />} />
    {/* 공지사항 상세 기능 */}
    <Route path="/notice" element={<CM_05_1000 />} />
    <Route path="/notice/:id" element={<CM_05_1001 />} />
    {/* 결제 기능 */}
    <Route path="/order-payment/result" element={<OrderPaymentResult />} />
    <Route path="/company" element={<CM_company />} />
    {/* 1:1 문의 */}
    <Route path="/contact" element={<CM_07_1000 />} />
    {/* 장바구니 */}
    <Route path="/cart" element={<CM_06_1000 />} />

    {/* --- 로그인 사용자 접근 가능 페이지 (권한 3 이상) --- */}
    <Route
      path="/review/write"
      element={
        <ProtectedRoute requiredRole={3}>
          <CM_04_1002 />
        </ProtectedRoute>
      }
    />
    <Route
      path="/review/edit/:writeId"
      element={
        <ProtectedRoute requiredRole={3}>
          <CM_04_1002 />
        </ProtectedRoute>
      }
    />
    <Route
      path="/qna/edit/:qnaId"
      element={
        <ProtectedRoute requiredRole={3}>
          <CM_04_1005 />
        </ProtectedRoute>
      }
    />
    <Route
      path="/order-payment"
      element={
        <ProtectedRoute requiredRole={3}>
          <CM_06_1001 />
        </ProtectedRoute>
      }
    />

    {/* 메인 */}
    <Route
      path="/mypage"
      element={
        <ProtectedRoute requiredRole={3}>
          <CM_01_1004 />
        </ProtectedRoute>
      }
    >
      <Route index element={<CM_01_9999 />} />
      <Route path="purchases" element={<CM_01_1005 />} />
      <Route path="purchases/:id" element={<CM_01_1011 />} />
      <Route path="inquiries" element={<CM_01_1006 />} />
      <Route path="inquiries/write" element={<CM_01_1012 />} />
      <Route path="inquiries/edit/:inquiryId" element={<CM_01_1012 />} />
      <Route path="inquiries/:inquiryId" element={<CM_01_1013 />} />
      <Route path="returns" element={<CM_01_1015 />} />
      <Route path="returns/write" element={<CM_01_1016 />} />
      <Route path="returns/:returnId" element={<CM_01_1017 />} />
      <Route path="profile" element={<CM_01_1007 />} />
      <Route path="address-book" element={<CM_01_1008 />} />
      <Route path="change-password" element={<CM_01_1009 />} />
      <Route path="unsubscribe" element={<CM_01_1010 />} />
      <Route path="account" element={<CM_01_1014 />} />
    </Route>

    {/* --- 관리자/매니저 접근 가능 페이지 (권한 2 이상) --- */}
    <Route
      path="/admin"
      element={
        <ProtectedRoute requiredRole={2}>
          <CM_90_1000 />
        </ProtectedRoute>
      }
    >
      <Route index element={<CM_90_1010 />} />
      {/* 유저 관리 */}
      <Route path="users" element={<CM_90_1020 />} />
      <Route path="users/:memberId" element={<CM_90_1021 />} />
      {/* 방문 데이터 */}
      <Route path="visits" element={<CM_90_1030 />} />
      <Route path="visits/analysis" element={<CM_90_1031 />} />
      {/* 메뉴 관리 */}
      <Route path="menu/banner" element={<CM_90_1040 />} />
      <Route path="menu/banner/register" element={<CM_90_1041 />} />
      <Route path="menu/banner/edit/:bannerId" element={<CM_90_1042 />} />
      <Route path="menu/category" element={<CM_90_1043 />} />
      <Route path="menu/notice" element={<CM_90_1044 />} />
      <Route path="menu/notice/register" element={<CM_90_1045 />} />
      <Route path="menu/notice/edit/:noticeId" element={<CM_90_1046 />} />
      {/* 정산 관리 */}
      <Route path="settlement/sales" element={<CM_90_1050 />} />
      <Route path="settlement/monthSales" element={<CM_90_1054 />} />
      <Route path="settlement/status" element={<CM_90_1051 />} />
      <Route path="settlement/refund" element={<CM_90_1052 />} />
      <Route path="settlement/stock_status" element={<CM_90_1053 />} />
      {/* 상품 등록 */}
      <Route path="products" element={<CM_90_1060 />} />
      <Route path="products/register" element={<CM_90_1061 />} />
      <Route path="products/edit/:productId" element={<CM_90_1062 />} />
      <Route path="products/stock" element={<CM_90_1063 />} />
      <Route path="products/stock/register" element={<CM_90_1064 />} />
      <Route path="products/code" element={<CM_90_1065 />} />
    </Route>

    {/* 쿠키 & 약관 페이지 */}
    <Route path="/cookie" element={<CM_CookiePage />} />
    <Route path="/terms" element={<TermsPage />} />

    {/* 에러 페이지 */}
    <Route path="/401" element={<Unauthorized />} />
    <Route path="/403" element={<Forbidden />} />
    <Route path="/404" element={<NotFound />} />
    <Route path="/500" element={<ServerError />} />

    {/* 잘못된 경로 catch */}
    <Route path="*" element={<NotFound />} />
  </ReactRoutes>
);

export default Routes;
