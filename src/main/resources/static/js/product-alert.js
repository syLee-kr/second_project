var productAlert = {
    // 공통 토스트 메시지 표시
    showToast: function (message, backgroundColor) {
        if (message) {
            const toast = document.createElement('div');
            toast.className = 'toast';
            toast.innerText = message;

            // 스타일을 동적으로 설정
            toast.style.position = 'fixed';
            toast.style.bottom = '20px';
            toast.style.right = '20px';
            toast.style.backgroundColor = backgroundColor;  // 색상 설정
            toast.style.color = 'white';
            toast.style.padding = '10px';
            toast.style.borderRadius = '5px';
            toast.style.zIndex = '9999';
            toast.style.display = 'inline-block';
            toast.style.opacity = '1';

            // 페이지에 추가
            document.body.appendChild(toast);

            // 2초 후 자동으로 사라지게 설정
            setTimeout(function () {
                toast.style.opacity = '0';
                setTimeout(function () {
                    toast.remove();  // DOM에서 제거
                }, 500);
            }, 2000);
        }
    },

    // 상품 등록 후 알림 메시지
    showProductRegisterSuccess: function () {
        var message = "상품이 성공적으로 등록되었습니다!";
        this.showToast(message, '#28a745');  // 성공 메시지 (녹색)
    },

    // 상품 삭제 후 알림 메시지
    showProductDeleteSuccess: function () {
        var message = "상품이 성공적으로 삭제되었습니다!";
        this.showToast(message, '#dc3545');  // 삭제 메시지 (빨간색)
    },

    // 페이지 로드 후 메시지 처리
    handleMessages: function () {
        // 상품 등록 성공 알림
        if (window.location.search.includes('register=success')) {
            this.showProductRegisterSuccess();
        }

        // 상품 삭제 성공 알림
        if (window.location.search.includes('delete=success')) {
            this.showProductDeleteSuccess();
        }
    }
};

// 상품 등록 후, 페이지 리다이렉트 시 메시지 설정
function registerProduct() {
    // 상품 등록 코드...
    
    // 등록 성공 후, URL에 쿼리 파라미터 추가하여 리다이렉트
    window.location.href = '/product-list?register=success';  // 상품 목록 페이지로 이동
}

// 상품 삭제 후, 페이지 리다이렉트 시 메시지 설정
function deleteProduct(productId) {
    // 상품 삭제 코드...

    // 삭제 성공 후, URL에 쿼리 파라미터 추가하여 리다이렉트
    window.location.href = '/product-list?delete=success';  // 상품 목록 페이지로 이동
}

// 페이지 로드 후 메시지 처리
document.addEventListener('DOMContentLoaded', function () {
    productAlert.handleMessages();  // 메시지 처리 함수 호출
});
