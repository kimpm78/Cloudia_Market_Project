package com.cloudia.backend.CM_03_1000.constants;

public class CM031000MessageConstant {
    private CM031000MessageConstant() {
    }
    // 상품 조회
    public static final String PRODUCT_FIND_ALL_START = "상품 전체 리스트 조회 시작";
    public static final String PRODUCT_FIND_ALL_COMPLETE = "상품 전체 리스트 조회 완료, 조회된 상품 수: {}";
    public static final String PRODUCT_FIND_ALL_DB_ERROR = "상품 전체 리스트 조회 중 DB 오류 발생: {}";
    public static final String PRODUCT_FIND_ALL_UNEXPECTED_ERROR = "상품 전체 리스트 조회 중 예상치 못한 오류 발생: {}";

    public static final String PRODUCT_SEARCH_START = "상품 검색 시작, 검색어: {}, 검색 타입: {}";
    public static final String PRODUCT_SEARCH_COMPLETE = "상품 검색 완료, 조회된 상품 수: {}";
    public static final String PRODUCT_SEARCH_FAILED_EMPTY_TERM = "상품 검색 실패: 검색어가 비어있습니다.";
    public static final String PRODUCT_SEARCH_FAILED_INVALID_TYPE = "상품 검색 실패: 유효하지 않은 검색 타입입니다. 검색 타입: {}";
    public static final String PRODUCT_SEARCH_DB_ERROR = "상품 검색 중 DB 오류 발생, 검색어: {}, 검색 타입: {}, 오류: {}";
    public static final String PRODUCT_SEARCH_UNEXPECTED_ERROR = "상품 검색 중 예상치 못한 오류 발생, 검색어: {}, 검색 타입: {}, 오류: {}";

    public static final String PRODUCT_FIND_BY_ID_START = "상품 상세 조회 시작, 상품 ID: {}";
    public static final String PRODUCT_FIND_BY_ID_COMPLETE = "상품 상세 조회 완료, 상품 ID: {}, 조회된 상품 수: {}";
    public static final String PRODUCT_FIND_BY_ID_FAILED_INVALID_ID = "상품 상세 조회 실패: 유효하지 않은 상품 ID입니다. 상품 ID: {}";
    public static final String PRODUCT_FIND_BY_ID_DB_ERROR = "상품 상세 조회 중 DB 오류 발생, 상품 ID: {}, 오류: {}";
    public static final String PRODUCT_FIND_BY_ID_UNEXPECTED_ERROR = "상품 상세 조회 중 예상치 못한 오류 발생, 상품 ID: {}, 오류: {}";

    // 카테고리 조회
    public static final String CATEGORY_GROUP_FETCH_START = "카테고리 그룹 조회 시작";
    public static final String CATEGORY_GROUP_FETCH_SUCCESS = "카테고리 그룹 조회 성공";
    public static final String CATEGORY_GROUP_FETCH_DB_ERROR = "카테고리 그룹 조회 중 DB 오류 발생: {}";
    public static final String CATEGORY_GROUP_FETCH_NULL = "카테고리 그룹 조회 실패: 결과가 null입니다.";
    public static final String CATEGORY_GROUP_FETCH_ERROR = "카테고리 그룹 조회 중 예상치 못한 오류 발생: {}";

    public static final String CATEGORY_DETAIL_FETCH_START = "하위 카테고리 조회 시작, 그룹 코드: {}";
    public static final String CATEGORY_DETAIL_FETCH_SUCCESS = "하위 카테고리 정보 조회 성공";
    public static final String CATEGORY_DETAIL_FETCH_DB_ERROR = "하위 카테고리 조회 중 DB 오류 발생: {}";
    public static final String CATEGORY_DETAIL_FETCH_NULL = "하위 카테고리 조회 실패: 결과가 null입니다.";
    public static final String CATEGORY_DETAIL_FETCH_ERROR = "하위 카테고리 조회 중 예상치 못한 오류 발생: {}";

    // 상품 등록 / 업데이트
    public static final String PRODUCT_UPLOAD_START = "상품 등록 시작, 상품명: {}";
    public static final String PRODUCT_UPLOAD_COMPLETE = "상품 등록 완료, 상품명: {}, 등록 결과: {}";
    public static final String PRODUCT_UPLOAD_DB_ERROR = "상품 등록 중 DB 오류 발생: {}";
    public static final String PRODUCT_UPLOAD_UNEXPECTED_ERROR = "상품 등록 중 예상치 못한 오류 발생: {}";
    public static final String PRODUCT_UPLOAD_FAILED_EMPTY_LIST = "상품 등록 실패: 등록할 상품 목록이 비어있습니다.";

    public static final String PRODUCT_UPDATE_START = "상품 업데이트 시작, 상품 ID: {}, 상품명: {}";
    public static final String PRODUCT_UPDATE_COMPLETE = "상품 업데이트 완료, 상품 ID: {}, 업데이트 결과: {}";
    public static final String PRODUCT_UPDATE_FAILED_NOT_EXISTS = "상품 업데이트 실패: 존재하지 않는 상품 ID입니다. 상품 ID: {}";
    public static final String PRODUCT_UPDATE_DB_ERROR = "상품 업데이트 중 DB 오류 발생: {}";
    public static final String PRODUCT_UPDATE_UNEXPECTED_ERROR = "상품 업데이트 중 예상치 못한 오류 발생: {}";
    public static final String PRODUCT_UPDATE_FAILED_EMPTY_LIST = "상품 업데이트 실패: 업데이트할 상품 목록이 비어있습니다.";

    // 상품 삭제
    public static final String PRODUCT_DELETE_START = "상품 삭제 시작, 상품 ID: {}";
    public static final String PRODUCT_DELETE_COMPLETE = "상품 삭제 완료, 상품 ID: {}, 삭제 결과: {}";
    public static final String PRODUCT_DELETE_FAILED_INVALID_ID = "상품 삭제 실패: 유효하지 않은 상품 ID입니다. 상품 ID: {}";
    public static final String PRODUCT_DELETE_FAILED_NOT_EXISTS = "상품 삭제 실패: 존재하지 않는 상품입니다. 상품 ID: {}";
    public static final String PRODUCT_DELETE_DB_ERROR = "상품 삭제 중 DB 오류 발생, 상품 ID: {}, 오류: {}";
    public static final String PRODUCT_DELETE_UNEXPECTED_ERROR = "상품 삭제 중 예상치 못한 오류 발생, 상품 ID: {}, 오류: {}";

    // 성공・실패 메시지
    public static final String SUCCESS_PRODUCT_FIND = "상품 조회 성공";
    public static final String SUCCESS_PRODUCT_UPLOAD = "상품 등록 성공";
    public static final String SUCCESS_PRODUCT_UPDATE = "상품 업데이트 성공";
    public static final String SUCCESS_PRODUCT_DELETE = "상품 삭제 성공";

    public static final String FAIL_PRODUCT_NOT_SELECTED = "삭제할 상품이 선택되지 않았습니다.";
    public static final String FAIL_PRODUCT_NOT_EXISTS = "존재하지 않는 상품입니다.";
    public static final String FAIL_PRODUCT_SEARCH_TERM_REQUIRED = "검색어를 입력해주세요.";
    public static final String FAIL_PRODUCT_SEARCH_TYPE_INVALID = "유효하지 않은 검색 타입입니다.";
    public static final String FAIL_PRODUCT_INVALID_ID = "유효하지 않은 상품 ID입니다.";
    public static final String FAIL_PRODUCT_DUPLICATE_NAME = "이미 존재하는 상품명입니다.";
    public static final String FAIL_PRODUCT_VALIDATION = "상품 등록 검증 실패: {}";

    public static final String FAIL_PRODUCT_LIST_EMPTY = "상품 목록이 존재하지 않습니다.";
    public static final String FAIL_PRODUCT_LIST_FETCH_NULL = "상품 목록 조회 실패: 응답 또는 본문이 null 입니다.";

    // 장바구니 관련
    public static final String SUCCESS_CART_ADD = "장바구니에 상품을 담았습니다.";
    public static final String SUCCESS_CART_UPDATE = "장바구니 수량이 수정되었습니다.";
    public static final String FAIL_CART_ADD = "장바구니 담기 실패: 오류가 발생했습니다.";
    public static final String FAIL_CART_UPDATE = "장바구니 수량 수정 실패: 오류가 발생했습니다.";

    // 파일 관련
    public static final String FILE_UPLOAD_PATH_DEBUG = "파일 업로드 경로: {}";
    public static final String FILE_UPLOAD_DIR_CREATED = "업로드 디렉토리 생성: {}";
    public static final String FILE_UPLOAD_DIR_CREATE_FAILED = "업로드 디렉토리 생성 실패: {}";
    public static final String FILE_SAVE_COMPLETE = "파일 저장 완료: {}";
    public static final String FILE_SAVE_FAILED = "파일 저장 실패: {}";
    public static final String FILE_DELETE_FAILED_CLEANUP = "실패한 파일 삭제 중 오류 발생: {}";

    public static final String IMAGE_DELETE_NO_LINK = "삭제할 이미지 링크가 없습니다.";
    public static final String IMAGE_DELETE_COMPLETE = "이미지 파일 삭제 완료: {}";
    public static final String IMAGE_DELETE_NOT_EXISTS = "삭제할 이미지 파일이 존재하지 않습니다: {}";
    public static final String IMAGE_DELETE_FILE_NOT_FOUND = "삭제할 파일이 존재하지 않습니다: {}";
    public static final String IMAGE_DELETE_FAILED = "이미지 파일 삭제 실패: {}, 오류: {}";
    public static final String IMAGE_DELETE_UNEXPECTED_ERROR = "이미지 파일 삭제 중 예상치 못한 오류 발생: {}, 오류: {}";
    
    // ========================================
    // 응답 메시지
    // ========================================

    // 성공 메시지
    public static final String SUCCESS_BANNER_DELETE = "배너 삭제 성공";
    public static final String SUCCESS_BANNER_FIND = "배너 조회 성공";
    public static final String SUCCESS_BANNER_UPLOAD = "배너 등록 성공";
    public static final String SUCCESS_BANNER_UPDATE = "배너 업데이트 성공";
    public static final String SUCCESS_DISPLAY_ORDER_FIND = "디스플레이 번호 조회 성공";
    public static final String SUCCESS_CATEGORY_GROUP_FOR_CHECKBOX = "카테고리 그룹 코드 조회 성공";

    // 실패 메시지
    public static final String FAIL_NO_BANNER_SELECTED = "삭제할 배너가 선택되지 않았습니다.";
    public static final String FAIL_REFERENCED_BANNER = "다른 데이터에서 참조 중인 배너는 삭제할 수 없습니다.";
    public static final String FAIL_BANNER_NOT_FOUND = "삭제할 배너를 찾을 수 없습니다.";
    public static final String FAIL_BANNER_NOT_EXISTS = "존재하지 않는 배너입니다.";
    public static final String FAIL_SEARCH_TERM_REQUIRED = "검색어를 입력해주세요.";
    public static final String FAIL_INVALID_BANNER_ID = "유효하지 않은 배너 ID입니다.";
    public static final String FAIL_MAX_ACTIVE_BANNERS = "활성화된 배너의 수량이 최대 10개를 초과했습니다.";
    public static final String FAIL_DUPLICATE_DISPLAY_ORDER = "배너 순서가 중복되었습니다.";
    public static final String FAIL_INVALID_FILE_TYPE = "허용되지 않는 파일 형식입니다.";
    public static final String FAIL_DUPLICATE_BANNER_INFO = "이미 존재하는 배너 정보입니다.";
    public static final String FAIL_DUPLICATE_BANNER_UPDATE = "중복된 배너 정보입니다.";
    public static final String FAIL_BANNER_VAL = "배너 등록 검증 실패: {}";
    public static final String FAIL_BANNER_UPDATE = "해당 배너는 다른 사용자가 수정하였습니다. 다시 조회 후 수정해주세요.";
}