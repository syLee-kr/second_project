document.addEventListener("DOMContentLoaded", () => {
    // HTML body 태그의 data-user-id 속성에서 사용자 ID 가져오기
    const userId = document.body.getAttribute("data-user-id");

    if (!userId) {
        console.error("사용자 ID를 찾을 수 없습니다. 로그인 후 이용해 주세요.");
        document.getElementById("error-message").textContent = "사용자 정보를 가져올 수 없습니다.";
        return;
    }

    fetch(`/recommend?userId=${userId}`)
        .then(response => {
            if (!response.ok) {
                throw new Error("서버 응답 오류");
            }
            return response.json();
        })
        .then(data => {
            const recommendations = document.getElementById("recommendations");
            recommendations.innerHTML = "";  // 기존 리스트 초기화

            data.forEach(camp => {
                const li = document.createElement("li");
                li.textContent = `${camp.name} - ${camp.address} (${camp.distance.toFixed(2)} km)`;
                recommendations.appendChild(li);
            });
        })
        .catch(error => {
            document.getElementById("error-message").textContent = error.message;
            console.error("추천 결과 가져오기 중 오류:", error);
        });
});
