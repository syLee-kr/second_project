var productAlert = {
    // 공통 알림 메시지 표시 (alert 사용)
    showAlert: function (message, redirectUrl) {
        if (message) {
            alert(message);  // alert을 사용하여 메시지를 팝업으로 표시

            // 리다이렉트가 필요한 경우
            if (redirectUrl) {
                window.location.href = redirectUrl;  // 리다이렉트
            }
        }
    },

	// 상품 등록 처리
	handleRegisterMessage: function () {
	    const registerButton = document.querySelector('.product-submit-button');
	    const form = document.querySelector('form');  // 폼을 가져옴
	    if (registerButton && form) {
	        form.addEventListener('submit', function (event) {
	            const productName = form.querySelector('input[name="name"]').value;  // 상품명 가져오기

	            if (!productName) {
	                // 상품 이름이 비어 있으면 실패 메시지
	                productAlert.showAlert("상품 등록에 실패했습니다. 상품 이름을 입력하세요.");
	                event.preventDefault();  // 폼 제출 막기
	            } else {
	                // 상품 등록 성공 메시지
	                productAlert.showAlert("상품이 성공적으로 등록되었습니다!", '/goods/product-list');
	            }
	        });
	    }
	},

    // 상품 삭제 처리
    handleDeleteMessage: function () {
        const deleteButton = document.querySelector('.product-delete-button');
        const deleteForm = document.querySelector("form[action='/goods/delete-products']");

        if (deleteButton && deleteForm) {
            deleteForm.addEventListener('submit', function (event) {
                const selectedProducts = document.querySelectorAll('input[name="productIds"]:checked');
                if (selectedProducts.length === 0) {
                    // 삭제할 상품이 선택되지 않으면 실패 메시지
                    productAlert.showAlert("삭제할 상품을 선택해주세요.");
                    event.preventDefault();  // 폼 제출 막기
                } else {
                    // 삭제 성공 메시지
                    productAlert.showAlert("상품이 성공적으로 삭제되었습니다!", '/goods/product-list');
                }
            });
        }
    },

    // 페이지 로드 시 메시지 처리
    handleMessagesBeforeLoad: function () {
        this.handleRegisterMessage();
        this.handleDeleteMessage();
    }
};

// 페이지 로드 후 메시지 처리
window.onload = function () {
    productAlert.handleMessagesBeforeLoad();
};



/* 이벤트 리스터로 하나의 코드로 만들었을때는 기능들이 충동을 일으켜서 적용이 안되었던거 같음
그래서 해당 메서드들을 분리해서 적용함.
*/
 
