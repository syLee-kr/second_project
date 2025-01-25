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

    // 장바구니에 상품 추가 처리
    handleAddToCartMessage: function () {
        const addToCartButton = document.querySelector('.cart-add-button');
        const addToCartForm = document.querySelector('form#addToCartForm');  // 장바구니 추가 폼

        if (addToCartButton && addToCartForm) {
            addToCartForm.addEventListener('submit', function (event) {
                event.preventDefault();  // 폼 제출 막기

                const quantity = addToCartForm.querySelector('input[name="quantity"]').value;  // 수량 가져오기
                const gseq = addToCartForm.querySelector('input[name="gseq"]').value;  // 상품 ID 가져오기

                if (!gseq) {
                    // 상품 ID가 없으면 실패 메시지
                    productAlert.showAlert("상품이 유효하지 않습니다.");
                    return;
                } else if (quantity <= 0) {
                    // 수량이 0 이하일 경우 실패 메시지
                    productAlert.showAlert("수량은 1 이상이어야 합니다.");
                    return;
                } else {
                    // 장바구니에 상품 추가 성공 메시지
                    productAlert.showAlert("상품이 장바구니에 추가되었습니다!");
                    
                    // AJAX 요청 보내기
                    const formData = new FormData(addToCartForm);
                    
                    fetch(addToCartForm.action, {
                        method: 'POST',
                        body: formData
                    })
                    .then(response => {
                        if (response.ok) {
                            // 성공하면 returnUrl로 리다이렉트
                            const returnUrl = formData.get('returnUrl');
                            window.location.href = returnUrl;
                        } else {
                            alert("장바구니에 추가 실패");
                        }
                    })
                    .catch(error => {
                        console.error("에러 발생:", error);
                        alert("장바구니에 추가 실패");
                    });
                }
            });
        }
    },

    // 장바구니에서 상품 삭제 처리
    handleRemoveFromCartMessage: function () {
        const removeFromCartButton = document.querySelector('.cart-remove-button');
        const removeFromCartForm = document.querySelector("form[action='/cart/remove']");  // 장바구니 삭제 폼

        if (removeFromCartButton && removeFromCartForm) {
            removeFromCartForm.addEventListener('submit', function (event) {
                const selectedCartItems = document.querySelectorAll('input[name="cartItemIds"]:checked');
                if (selectedCartItems.length === 0) {
                    // 삭제할 장바구니 항목이 선택되지 않으면 실패 메시지
                    productAlert.showAlert("삭제할 상품을 선택해주세요.");
                    event.preventDefault();  // 폼 제출 막기
                } else {
                    // 장바구니 상품 삭제 성공 메시지
                    productAlert.showAlert("상품이 장바구니에서 삭제되었습니다!", '/cart/view');
                }
            });
        }
    },

    // 페이지 로드 시 메시지 처리
    handleMessagesBeforeLoad: function () {
        this.handleRegisterMessage();
        this.handleDeleteMessage();
        this.handleAddToCartMessage();
        this.handleRemoveFromCartMessage();
    }
};

// 페이지 로드 후 메시지 처리
window.onload = function () {
    productAlert.handleMessagesBeforeLoad();
};




/* 이벤트 리스터로 하나의 코드로 만들었을때는 기능들이 충동을 일으켜서 적용이 안되었던거 같음
그래서 해당 메서드들을 분리해서 적용함.
*/
 
