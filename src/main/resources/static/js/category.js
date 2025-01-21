
document.addEventListener('DOMContentLoaded', function() {
    const categorySelect = document.getElementById('categorySelect'); // 카테고리 선택 드롭다운
    categorySelect.addEventListener('change', function() {
        const selectedCategory = categorySelect.value;  // 선택된 카테고리 값
        
        if (selectedCategory) {
            // 카테고리가 선택되었을 경우, 해당 카테고리에 맞는 상품 리스트 페이지로 이동
            window.location.href = `/goods/category/${selectedCategory}`;
        } else {
            // 카테고리가 선택되지 않으면 기본 상품 목록 페이지로 이동
            window.location.href = '/goods/product-list';
        }
    });
});
