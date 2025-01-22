// 카테고리 선택 처리 함수
function handleCategorySelect() {
    const categorySelect = document.getElementById('categorySelect');
    
    if (categorySelect) {
        categorySelect.addEventListener('change', function () {
            const selectedCategory = categorySelect.value;

            if (selectedCategory == "전체") {
                window.location.href = '/goods/product-list';
            } else {
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