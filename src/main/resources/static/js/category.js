// 카테고리 선택 처리 함수
function handleCategorySelect() {
    const categorySelect = document.getElementById('categorySelect');
    
    if (categorySelect) {
        categorySelect.addEventListener('change', function () {
            const selectedCategory = categorySelect.value;
			
			// "전체"를 선택한 경우, 모든 상품을 표시하도록 페이지 리로드
            if (selectedCategory == "전체") {
                window.location.href = '/goods/product-list';
			
			// "카테고리 선택" 상태일 경우 아무 동작도 하지 않음
			} else if (selectedCategory === '') {
			    // "카테고리 선택" 상태에서는 아무 동작도 하지 않음
			    return;
				
			} else {
			// 특정 카테고리를 선택한 경우, 해당 카테고리 상품만 필터링해서 보기
            window.location.href = `/goods/product-list?category=${selectedCategory}`;
            }
        });
    }
}

// 카테고리 추가 버튼 처리 함수
function handleAddCategoryBtn() {
    const addCategoryBtn = document.getElementById('addCategoryBtn');

    if (addCategoryBtn) {
        addCategoryBtn.addEventListener('click', function () {
            console.log("카테고리 추가 버튼 클릭됨");
            window.location.href = '/category/list';
        });
    }
}

// DOMContentLoaded 이벤트를 사용해 두 기능을 호출
document.addEventListener('DOMContentLoaded', function () {
    handleCategorySelect();  // 카테고리 선택 처리
    handleAddCategoryBtn();  // 카테고리 추가 버튼 처리
});



/* 참고 : 
하나의 파일에 이벤트 리스너를 하나의 코드로 만들면 이벤트 리스터 기능이 충돌이 일어나서 기능을 못할수있으니
각 기능에 대한 이벤트 리스너를 독립적으로 분리하는게 좋음.
*/