/**
 *  클라이언트 Api 호출 및 처리 js 
 */
function fetchRecommendations(userId) {
    fetch(`/recommend?userId=${userId}`)
        .then(response => response.json())
        .then(data => {
            document.getElementById("recommendations").innerHTML = data.map(camp => `<li>${camp}</li>`).join('');
        })
        .catch(error => console.error('Error fetching recommendations:', error));
}
